/**************************************
Test disabled as it has become obsolete
***************************************/

/*
package net.i2cat.mcas.junit.management.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Random;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.easymock.PowerMock.createPartialMockAndInvokeDefaultConstructor;
import static org.powermock.api.easymock.PowerMock.expectPrivate;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertTrue;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.ApplicationConfig;
import net.i2cat.mcas.config.model.TranscoRequestV;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.TranscoHandler;
import net.i2cat.mcas.management.TranscoQueue;
import net.i2cat.mcas.utils.DefaultsUtils;
import net.i2cat.mcas.utils.Downloader;
import net.i2cat.mcas.utils.Uploader;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TranscoHandler.class, DefaultsUtils.class})
@SuppressStaticInitializationFor("net.i2cat.mcas.config.dao.DAO")
public class TranscoHandlerTest {
	
	private TranscoHandler transcoHandle;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception{
		DAO<ApplicationConfig> applicactionDaoMock = (DAO<ApplicationConfig>) createMock(DAO.class);
		expectNew(DAO.class, ApplicationConfig.class).andReturn(applicactionDaoMock).anyTimes();
		expect(applicactionDaoMock.findById((Integer) anyObject())).andReturn(DefaultsUtils.applicationGetDefaults()).anyTimes();
		expect(applicactionDaoMock.findByName((String) anyObject())).andReturn(DefaultsUtils.applicationGetDefaults()).anyTimes();
		
		DAO<TranscoRequestV> requestDaoMock = (DAO<TranscoRequestV>) createMock(DAO.class);
		expectNew(DAO.class, TranscoRequestV.class).andReturn(requestDaoMock).anyTimes();
		
		DAO<TranscoderConfig> tConfigDaoMock = (DAO<TranscoderConfig>) createMock(DAO.class);
		expectNew(DAO.class, TranscoderConfig.class).andReturn(tConfigDaoMock).anyTimes();
		expect(tConfigDaoMock.findByName((String) anyObject())).andReturn(DefaultsUtils.tConfigGetDefaults()).anyTimes();
	}
	
	@After
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void handleTest() throws Exception{
		Downloader downloaderMock = createMock(Downloader.class);
		expectNew(Downloader.class, (URI) anyObject(), (File) anyObject()).andReturn(downloaderMock).anyTimes();
		downloaderMock.toWorkingDir();
		expectLastCall().anyTimes();
		
		DefaultExecutor executorMock = createMock(DefaultExecutor.class);
		expectNew(DefaultExecutor.class).andReturn(executorMock).anyTimes();
		executorMock.setWatchdog((ExecuteWatchdog) anyObject());
		expectLastCall().anyTimes();
		executorMock.setProcessDestroyer((ShutdownHookProcessDestroyer) anyObject());
		expectLastCall().anyTimes();
		expect(executorMock.execute((CommandLine) anyObject())).andThrow(new IOException()).anyTimes();
		
		Uploader uploaderMock = createMock(Uploader.class);
		expectNew(Uploader.class, (URI) anyObject(), (File) anyObject()).andReturn(uploaderMock).anyTimes();
		uploaderMock.toDestinationUri();
		expectLastCall().anyTimes();
		
		replayAll();
		
		transcoHandle = new TranscoHandler();
		Thread service = new Thread(transcoHandle);
		service.start();
		PThread requestProducer = new PThread(5, transcoHandle);
		(new Thread(requestProducer)).start();
		while(! TranscoQueue.getInstance().isEmpty()){
			Thread.sleep(1000);
		}
		transcoHandle.stop();
		service.join();
	}
	
//	@Test
//	public void loadConfigTest() throws MCASException {
//			
//		replayAll();
//		
//		transcoHandle = new TranscoHandler();
//		transcoHandle.loadConfig(1);
//	}
}

class PThread implements Runnable {

	
	private int nReq;
	private TranscoHandler transcoHandle;

	public PThread(int nReq, TranscoHandler transcoHandle) {
		this.nReq = nReq;
		this.transcoHandle = transcoHandle;
	}

	@Override
	public void run() {
		Random r = new Random();
		for (int i = 0; i < nReq; i++) {
			try {
				TranscoRequestV req = new TranscoRequestV();
				req.setSrc("file:///" + ((Integer) i).toString());
				req.setDst("file:///" + ((Integer) i).toString());
				req.setConfig(DefaultsUtils.DEFAULT);
				Thread.sleep((r.nextInt(100) + 1) * 5);
				transcoHandle.putRequest(req);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
*/