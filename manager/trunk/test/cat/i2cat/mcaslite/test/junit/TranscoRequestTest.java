package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.entities.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoRequestTest {
	
	@Test
	public void defaultValuesTest(){
		TranscoRequest request = new TranscoRequest();
		assertNull(request.getSrc());
		assertNull(request.getDst());
		assertNull(request.getConfig());
		assertNotNull(request.getId());
		assertNotNull(request.getState());
		assertTrue(request.isTranscodedUriEmpty());
		assertEquals(State.CREATED,request.getState());
		boolean fail = false;
		try {
			request.increaseState();
			assertEquals(State.M_QUEUED,request.getState());
			for (int i = 0; i < 10 ; i++) request.increaseState();
		} catch (MCASException e) {
			fail = true;
		}
		if (! fail){
			Assert.fail();
		}
	}

}
