package cat.i2cat.mcaslite.junit.management.test;

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

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.MediaHandler;
import cat.i2cat.mcaslite.management.TranscoQueue;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.Downloader;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;
import cat.i2cat.mcaslite.utils.Uploader;

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
	private static TranscoRequest requestOut;
	private MediaHandler mediaH;
	private static File fakeFile = new File("fakeFile");
	private TranscoQueue queueMock;
	private DAO<TranscoRequest> requestDaoMock;
	
	@BeforeClass
	public static void setup() throws MCASException{
		requestIn = TranscoRequest.getEqualRequest(UUID.randomUUID());
		requestIn.setState(State.M_PROCESS);
		requestIn.setSrc("file:///this/is/fake/source");
		requestIn.setTConfig(DefaultsUtils.tConfigGetDefaults());
		
		requestOut = TranscoRequest.getEqualRequest(UUID.randomUUID());
		requestOut.setState(State.MOVING);
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
		this.requestDaoMock = (DAO<TranscoRequest>) createMock(DAO.class);
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
		assertTrue(requestIn.getState().equals(State.T_QUEUED));
	}
	
	@Test
	public void testRunBadrequestState() throws URISyntaxException, Exception {
		requestIn.setState(State.CREATED);
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
		assertTrue(requestIn.getState().equals(State.ERROR));
	}
	
	@Test
	public void testRunException() throws URISyntaxException, Exception {
		requestIn.setState(State.M_PROCESS);
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
		assertTrue(requestIn.getState().equals(State.ERROR));
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
		
		requestOut.setTranscoded(TranscoderUtils.transcoBuilder(requestOut.getTConfig(), requestOut.getIdStr(), requestOut.getDst()));
		
		expect(queueMock.removeRequest(requestOut)).andReturn(true).once();
		requestDaoMock.save((TranscoRequest) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(State.DONE));
	}
	
	@Test
	public void testRunOutputError() throws URISyntaxException, Exception {
		requestOut.setState(State.MOVING);
		mockStatic(MediaUtils.class);
		expect(MediaUtils.deleteInputFile(requestOut.getIdStr(), requestOut.getTConfig().getInputWorkingDir())).andReturn(false).once();
		MediaUtils.clean(requestOut);
		expectLastCall().once();
		
		replay(MediaUtils.class);
		List<Transco> transcos = new ArrayList<Transco>();
		requestOut.setTranscoded(transcos);
		
		expect(queueMock.removeRequest(requestOut)).andReturn(true).once();
		requestDaoMock.save((TranscoRequest) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(State.ERROR));
	}
	
	@Test
	public void testRunOutputPartialError() throws URISyntaxException, Exception {
		requestOut.setState(State.MOVING);
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
		requestDaoMock.save((TranscoRequest) anyObject());
		expectLastCall().once();
		queueMock.notifyAll();
		expectLastCall().once();
		
		replayAll();
		
		queue = TranscoQueue.getInstance();
		mediaH = new MediaHandler(queue, requestOut);
		Thread th = new Thread(mediaH);
		th.start();
		th.join();
		assertTrue(requestOut.getState().equals(State.PARTIAL_ERROR));
	}

	//TODO: cancel test
	
}
