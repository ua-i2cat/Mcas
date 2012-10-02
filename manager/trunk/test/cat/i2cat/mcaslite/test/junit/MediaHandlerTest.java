package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.MediaHandler;

public class MediaHandlerTest {
	
	private TranscoQueue queue = TranscoQueue.getInstance();
	private TranscoRequest request = TranscoRequest.getEqualRequest(UUID.fromString("04e119ed-8862-42ba-b8ee-22e3d97df550"));
	private MediaHandler mHandler;
	
	@Test
	public void inputHandleTest() throws MCASException{
		request.setSrc("file:///etc/fstab");
		queue.put(request);
		mHandler =  new MediaHandler(queue, request);
		try {
			mHandler.inputHandleTest();
			File file = new File("/home/david/work/input/04e119ed-8862-42ba-b8ee-22e3d97df550");
			assertTrue(file.exists());
			TranscoRequest req = queue.get(State.M_QUEUED);
			assertTrue(req.equals(request));
		} catch (Exception e) {
			Assert.fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void outputHandleTest1() throws MCASException{
		queue.clearQueue();
		request.setSrc("file:///etc/fstab");
		request.setNumOutputs(3);
		for(int i = 0; i < 3; i++){
			request.addTrancoded(new Transco("thisIsCommand" + i, "/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df550" + i, "file:///home/david/prova.mp3" + i, "/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df550"));
		}
		queue.put(request);
		mHandler =  new MediaHandler(queue, request);
		try {
			mHandler.outputHandleTest();
			File file = new File("/home/david/prova.mp30");
			assertTrue(file.exists());
			file = new File("/home/david/prova.mp31");
			assertTrue(file.exists());
			file = new File("/home/david/prova.mp32");
			assertTrue(file.exists());
			file = new File("/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df550");
			assertTrue(! file.exists());
			file = new File("/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df5500");
			assertTrue(! file.exists());
			file = new File("/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df5501");
			assertTrue(! file.exists());
			file = new File("/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df5502");
			assertTrue(! file.exists());
			TranscoRequest req = queue.get(State.M_QUEUED);
			assertTrue(req.equals(request));
		} catch (Exception e) {
			Assert.fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void outputHandleTest2() throws MCASException{
		queue.clearQueue();
		request.setSrc("file:///etc/fstab");
		request.setNumOutputs(3);
		for(int i = 3; i < 5; i++){
			request.addTrancoded(new Transco("thisIsCommand" + i, "/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df550" + i, "file:///home/david/prova.mp3" + i, "/home/david/tmp/04e119ed-8862-42ba-b8ee-22e3d97df5506"));
		}
		queue.put(request);
		mHandler =  new MediaHandler(queue, request);
		try {
			mHandler.outputHandleTest();
			File file = new File("/home/david/prova.mp33");
			assertTrue(file.exists());
			file = new File("/home/david/prova.mp34");
			assertTrue(file.exists());
			TranscoRequest req = queue.get(State.PARTIAL_ERROR);
			assertNotNull(req);
			assertTrue(req.equals(request));
			assertEquals(req.getState(),State.PARTIAL_ERROR);
		} catch (Exception e) {
			Assert.fail();
			e.printStackTrace();
		}
	}
}
