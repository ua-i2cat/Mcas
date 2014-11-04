/**************************************
Test disabled as it has become obsolete
***************************************/

/*
package net.i2cat.mcas.junit.management.test;

import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.Transco;
import net.i2cat.mcas.config.model.TranscoRequestV;
import net.i2cat.mcas.config.model.TranscoRequestV.Status;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.TranscoQueue;
import net.i2cat.mcas.management.Transcoder;
import net.i2cat.mcas.utils.DefaultsUtils;
import net.i2cat.mcas.utils.MediaUtils;
import net.i2cat.mcas.utils.TranscoderUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Transcoder.class, TranscoderUtils.class, TranscoQueue.class, MediaUtils.class})
@SuppressStaticInitializationFor("net.i2cat.mcas.config.dao.DAO")
public class TranscoderTest {
	
	private static TranscoRequestV requestOut;
	private List<Transco> transcos = new ArrayList<Transco>();
	private static String FAKEDST = "fakeDst";
	private static TranscoQueue queueMock;
	private DAO<TranscoRequestV> requestDaoMock;
	private DefaultExecutor executorMock;

	@BeforeClass
	public static void setUp() throws Exception{
		System.setProperty("mcas.home", "/This/Is/Fake/Home");
	}
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception{
		File goodFileMock = createMock(File.class);
		expectNew(File.class, new Class<?>[]{ String.class },(String) anyObject()).andReturn(goodFileMock).anyTimes();
		expect(goodFileMock.exists()).andReturn(true).anyTimes();
		expect(goodFileMock.isDirectory()).andReturn(true).anyTimes();
		expect(goodFileMock.canWrite()).andReturn(true).anyTimes();
		expect(goodFileMock.isAbsolute()).andReturn(false).anyTimes();
		
		replay(goodFileMock, File.class);
		
		List<Transco> transcosTmp = TranscoderUtils.transcoBuilder(DefaultsUtils.tConfigGetDefaults(), "notImportantAtAll", FAKEDST);
		Iterator<Transco> it = transcosTmp.iterator();
		while(it.hasNext()){
			Transco transco = it.next();
			it.remove();
			transco.setCommand("sleep 0.25");
			transcos.add(transco);
		}
		
		this.requestDaoMock = (DAO<TranscoRequestV>) createMock(DAO.class);
		expectNew(DAO.class, TranscoRequestV.class).andReturn(requestDaoMock).once();
		
		requestOut = TranscoRequestV.getEqualRequest(UUID.randomUUID());
		requestOut.setState(Status.T_PROCESS);
		requestOut.setSrc("file:///this/is/fake/source");
		requestOut.setDst("file:///this/is/fake/destination");
		requestOut.setTConfig(DefaultsUtils.tConfigGetDefaults());
		
		mockStatic(TranscoQueue.class);
		queueMock = createMock(TranscoQueue.class);
		expect(TranscoQueue.getInstance()).andReturn(queueMock).once();
		
		mockStatic(TranscoderUtils.class);
		expect(TranscoderUtils.transcoBuilder(requestOut.getTConfig(), requestOut.getIdStr(), requestOut.getDst())).andReturn(transcos).once();
	}
	
	
	@Test(timeout = 1500)
	public void transcoderTest() throws MCASException, InterruptedException {
		queueMock.update(requestOut);
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
			
		replayAll();
		
		TranscoQueue queue = TranscoQueue.getInstance();
		Transcoder transcoder = new Transcoder(queue, requestOut);
		Thread th = new Thread(transcoder);
		th.start();
		th.join();
		
		verifyAll();
		resetAll();
		
		assertEquals(Status.T_TRANSCODED, requestOut.getState());
	}
	
	@Test(timeout = 1000)
	public void transcoderTestCancel() throws MCASException, InterruptedException {
		mockStatic(MediaUtils.class);
		MediaUtils.clean(requestOut);
		expectLastCall().once();
		
		replayAll();
		
		TranscoQueue queue = TranscoQueue.getInstance();
		Transcoder transcoder = new Transcoder(queue, requestOut);
		Thread th = new Thread(transcoder);
		th.start();
		transcoder.cancel(false);
		th.join();
		
		verifyAll();
		resetAll();
		
		assertEquals(Status.T_PROCESS, requestOut.getState());
	}
	
	@Test
	public void transcoderTestError() throws Exception {
		expect(queueMock.removeRequest(requestOut)).andReturn(false);
		
		mockStatic(MediaUtils.class);
		expect(MediaUtils.deleteFile((String) anyObject())).andReturn(true).times(3);
		MediaUtils.clean(requestOut);
		expectLastCall().once();
			
		executorMock = createMock(DefaultExecutor.class);
		expectNew(DefaultExecutor.class).andReturn(executorMock).once();
		executorMock.setWatchdog((ExecuteWatchdog) anyObject());
		expectLastCall().times(3);
		executorMock.setProcessDestroyer((ShutdownHookProcessDestroyer) anyObject());
		expectLastCall().times(3);
		expect(executorMock.execute((CommandLine) anyObject())).andThrow(new IOException()).times(3);
		
		replayAll();
		
		TranscoQueue queue = TranscoQueue.getInstance();
		Transcoder transcoder = new Transcoder(queue, requestOut);
		Thread th = new Thread(transcoder);
		th.start();
		th.join();
		
		verifyAll();
		resetAll();
		
		assertEquals(Status.ERROR, requestOut.getState());
	}
	
	@Test
	public void transcoderTestPartialError() throws Exception {
		queueMock.update(requestOut);
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		mockStatic(MediaUtils.class);
		expect(MediaUtils.deleteFile((String) anyObject())).andReturn(true).times(1);
		
		Transco transco = transcos.get(0);
		transco.setCommand("thisWillFail");
		transcos.set(0, transco);
		
		replayAll();
		
		TranscoQueue queue = TranscoQueue.getInstance();
		Transcoder transcoder = new Transcoder(queue, requestOut);
		Thread th = new Thread(transcoder);
		th.start();
		th.join();
		
		verifyAll();
		resetAll();
		
		assertEquals(Status.T_TRANSCODED, requestOut.getState());
		assertEquals(2, requestOut.getTranscoded().size());
	}
}
*/