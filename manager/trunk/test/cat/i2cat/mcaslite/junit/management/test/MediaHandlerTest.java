package cat.i2cat.mcaslite.junit.management.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.management.MediaHandler;
import cat.i2cat.mcaslite.management.TranscoQueue;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.Downloader;
import cat.i2cat.mcaslite.utils.MediaUtils;

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
@SuppressStaticInitializationFor("cat.i2cat.mcaslite.config.dao.DAO")
public class MediaHandlerTest {

	private static TranscoQueue queue;
	private static TranscoRequest requestIn;
	private MediaHandler mediaH;
	private static File fakeFile = new File("fakeFile");
	private TranscoQueue queueMock;
	
	@BeforeClass
	public static void setup(){
		requestIn = TranscoRequest.getEqualRequest(UUID.randomUUID());
		requestIn.setState(State.M_PROCESS);
		requestIn.setSrc("file:///this/is/fake/source");
		requestIn.setTConfig(DefaultsUtils.tConfigGetDefaults());
	}
	
	@Before
	public void setUp() throws Exception{
		mockStatic(TranscoQueue.class);
		queueMock = createMock(TranscoQueue.class);
		expect(TranscoQueue.getInstance()).andReturn(queueMock).once();
		@SuppressWarnings("unchecked")
		DAO<TranscoRequest> requestDaoMock = (DAO<TranscoRequest>) createMock(DAO.class);
		expectNew(DAO.class, TranscoRequest.class).andReturn(requestDaoMock).once();
	}
	
	@After()
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void testRunInput() throws URISyntaxException, Exception {
		mockStatic(MediaUtils.class);
		expect(MediaUtils.setInFile(requestIn.getIdStr(), requestIn.getTConfig())).andReturn(fakeFile).anyTimes();
		
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
		while(! mediaH.isDone()){};
		assertTrue(requestIn.getState().equals(State.T_QUEUED));
	}
	
//	@Test
//	public void testRunInputException() throws URISyntaxException, Exception {
//		//TODO
//	}
//	
//	@Test
//	public void testRunOutput() throws URISyntaxException, Exception {
//		//TODO
//	}
//	
//	@Test
//	public void testRunOutputException() throws URISyntaxException, Exception {
//		//TODO
//	}
//	
//	@Test
//	public void testCancel() throws URISyntaxException, Exception {
//		//TODO
//	}
	
}
