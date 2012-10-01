package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.entities.TranscoRequest.State;
import cat.i2cat.mcaslite.management.Transcoder;

public class TranscoderTest {

	@Test
	public void transcoderTest(){
		try {
			TranscoRequest request = TranscoRequest.getEqualRequest(UUID.fromString("04e119ed-8862-42ba-b8ee-22e3d97df550"));
			request.setDst("/home/david/transcoTest");
			request.setConfig("default");
			TranscoQueue queue = TranscoQueue.getInstance();
			queue.put(request);
			Transcoder transcoder = new Transcoder(queue, request);
			Thread transcoTh = new Thread(transcoder);
			transcoTh.setDaemon(true);
			transcoTh.start();
			transcoTh.join();
			TranscoRequest newRequest = queue.get(State.M_QUEUED);
			assertEquals(request, newRequest);
			assertEquals(newRequest.getState(),State.M_QUEUED);
			assertNotNull(newRequest);
			assertEquals(3,newRequest.getNumOutputs());
			assertTrue(! newRequest.isTranscodedEmpty());
			assertEquals(3, newRequest.getTranscoded().size());
			assertTrue((new File("/home/david/work/output/04e119ed-8862-42ba-b8ee-22e3d97df5501.mp4")).exists());
			assertTrue((new File("/home/david/work/output/04e119ed-8862-42ba-b8ee-22e3d97df5501.ogg")).exists());
			assertTrue((new File("/home/david/work/output/04e119ed-8862-42ba-b8ee-22e3d97df5501.webm")).exists());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
