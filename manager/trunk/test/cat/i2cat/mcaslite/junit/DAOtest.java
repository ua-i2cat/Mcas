package cat.i2cat.mcaslite.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class DAOtest {

	@Test
	public void getRequestTest(){
		DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
		String id = "75303ccf-48be-43ab-b3e3-adb49e106d69";
		try {
			TranscoRequest request = requestDao.findById(UUID.fromString(id));
			assertNotNull(request);
			assertEquals(request.getIdStr(), id);
		} catch (MCASException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
