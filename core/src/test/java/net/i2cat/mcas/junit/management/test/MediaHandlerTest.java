/**************************************
Test disabled as it has become obsolete
***************************************/

/*
package net.i2cat.mcas.junit.management.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
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
import net.i2cat.mcas.management.MediaHandler;
import net.i2cat.mcas.management.TranscoQueue;
import net.i2cat.mcas.utils.DefaultsUtils;
import net.i2cat.mcas.utils.Downloader;
import net.i2cat.mcas.utils.MediaUtils;
import net.i2cat.mcas.utils.TranscoderUtils;
import net.i2cat.mcas.utils.Uploader;

import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MediaHandler.class, TranscoQueue.class, MediaUtils.class})
@SuppressStaticInitializationFor("net.i2cat.mcas.config.dao.DAO")
public class MediaHandlerTest {

	private static TranscoQueue queue;
	private static TranscoRequestV requestIn;
	private static TranscoRequestV requestOut;
	private MediaHandler mediaH;
	private static File fakeFile = new File("fakeFile");
	private TranscoQueue queueMock;
	private DAO<TranscoRequestV> requestDaoMock;
	
	@BeforeClass
	public static void setup() throws MCASException{
		requestIn = TranscoRequestV.getEqualRequest(UUID.randomUUID());
		requestIn.setState(Status.M_PROCESS);
		requestIn.setSrc("file:///this/is/fake/source");
		requestIn.setTConfig(DefaultsUtils.tConfigGetDefaults());
		
		requestOut = TranscoRequestV.getEqualRequest(UUID.randomUUID());
		requestOut.setState(Status.MOVING);
		requestOut.setSrc("file:///this/is/fake/source");
		requestOut.setDst("file:///this/is/fake/destination");
		requestOut.setTConfig(DefaultsUtils.tConfigGetDefaults());
	}
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception{
		mockStatic(TranscoQueue.class);
		queueMock = createMock(TranscoQueue.class);
		expect(TranscoQueue.getInstance()).andReturn(queueMock).once();
		this.requestDaoMock = (DAO<TranscoRequestV>) createMock(DAO.class);
		expectNew(DAO.class, TranscoRequestV.class).andReturn(requestDaoMock).once();
	}
	
	@After()
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void testRunInput() throws URISyntaxException, Exception {
		mockStatic(MediaUtils.class);
		expect(MediaUtils.setInFile(requestIn.getIdStr(), requestIn.getTConfig())).andReturn(fakeFile).times(2);
		
		replay(MediaUtils.class);
		
		Downloader downloaderMock = createMock(Downloader.class);
		expectNew(Downloader.class, new URI(requestIn.getSrc()), MediaUtils.setInFile(requestIn.getIdStr(), requestIn.getTConfig())).andReturn(downloaderMock).once();
		downloaderMock.toWorkingDir();
		expectLastCall().once();
		queueMock.update(requestIn);
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestIn);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestIn.getState().equals(Status.T_QUEUED));
	}
	
	@Test
	public void testRunBadrequestState() throws URISyntaxException, Exception {
		requestIn.setState(Status.CREATED);
		mockStatic(MediaUtils.class);
		MediaUtils.clean(requestIn);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		
		expect(queueMock.removeRequest(requestIn)).andReturn(false).once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestIn);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestIn.getState().equals(Status.ERROR));
	}
	
	@Test
	public void testRunException() throws URISyntaxException, Exception {
		requestIn.setState(Status.M_PROCESS);
		mockStatic(MediaUtils.class);
		expect(MediaUtils.setInFile(requestIn.getIdStr(), requestIn.getTConfig())).andReturn(fakeFile).times(2);
		expect(MediaUtils.deleteInputFile(requestIn.getIdStr(), requestIn.getTConfig().getInputWorkingDir())).andReturn(false).once();
		MediaUtils.clean(requestIn);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		
		Downloader downloaderMock = createMock(Downloader.class);
		expectNew(Downloader.class, new URI(requestIn.getSrc()), MediaUtils.setInFile(requestIn.getIdStr(), requestIn.getTConfig())).andReturn(downloaderMock).once();
		downloaderMock.toWorkingDir();
		expectLastCall().andThrow(new MCASException());
		expect(queueMock.removeRequest(requestIn)).andReturn(false).once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestIn);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestIn.getState().equals(Status.ERROR));
	}
	
	@Test
	public void testRunOutput() throws URISyntaxException, Exception {
		Uploader uploaderMock = createMock(Uploader.class);
		expectNew(Uploader.class, (URI) anyObject(), (File) anyObject()).andReturn(uploaderMock).times(3);
		uploaderMock.toDestinationUri();
		expectLastCall().times(3);
		
		mockStatic(MediaUtils.class);
		expect(MediaUtils.getWorkDir(requestOut.getTConfig().getInputWorkingDir())).andReturn("file:///fake/input").times(6);
		expect(MediaUtils.getWorkDir(requestOut.getTConfig().getOutputWorkingDir())).andReturn("file:///fake/output").times(6);
		MediaUtils.clean(requestOut);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		
		File goodFileMock = createMock(File.class);
		expectNew(File.class, (new URI(requestOut.getDst())).getPath()).andReturn(goodFileMock).times(3);
		expect(goodFileMock.exists()).andReturn(true).times(3);
		expect(goodFileMock.isDirectory()).andReturn(true).times(3);
		expect(goodFileMock.canWrite()).andReturn(true).times(3);
		
		replay(goodFileMock, File.class);
		
		requestOut.setTranscoded(TranscoderUtils.transcoBuilder(requestOut.getTConfig(), requestOut.getIdStr(), requestOut.getDst()));
		
		expect(queueMock.removeRequest(requestOut)).andReturn(true).once();
		requestDaoMock.save((TranscoRequestV) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(Status.DONE));
	}
	
	@Test
	public void testRunOutputError() throws URISyntaxException, Exception {
		requestOut.setState(Status.MOVING);
		mockStatic(MediaUtils.class);
		expect(MediaUtils.deleteInputFile(requestOut.getIdStr(), requestOut.getTConfig().getInputWorkingDir())).andReturn(false).once();
		MediaUtils.clean(requestOut);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		List<Transco> transcos = new ArrayList<Transco>();
		requestOut.setTranscoded(transcos);
		
		expect(queueMock.removeRequest(requestOut)).andReturn(true).once();
		requestDaoMock.save((TranscoRequestV) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(Status.ERROR));
	}
	
	@Test
	public void testRunOutputPartialError() throws URISyntaxException, Exception {
		requestOut.setState(Status.MOVING);
		mockStatic(MediaUtils.class);
		expect(MediaUtils.getWorkDir(requestOut.getTConfig().getInputWorkingDir())).andReturn("file:///fake/input").times(6);
		expect(MediaUtils.getWorkDir(requestOut.getTConfig().getOutputWorkingDir())).andReturn("file:///fake/output").times(6);
		MediaUtils.clean(requestOut);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		List<Transco> transcos = TranscoderUtils.transcoBuilder(requestOut.getTConfig(), requestOut.getIdStr(), requestOut.getDst());
		transcos.remove(0);
		requestOut.setTranscoded(transcos);
		
		expect(queueMock.removeRequest(requestOut)).andReturn(true).once();
		requestDaoMock.save((TranscoRequestV) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(Status.PARTIAL_ERROR));
	}

	//TODO: cancel test
	
}
*/