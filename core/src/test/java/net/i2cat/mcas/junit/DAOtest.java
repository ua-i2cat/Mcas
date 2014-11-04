package net.i2cat.mcas.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TranscoRequestV;
import net.i2cat.mcas.exceptions.MCASException;

public class DAOtest {

	@Test
	public void getRequestTest(){
		DAO<TranscoRequestV> requestDao = new DAO<TranscoRequestV>(TranscoRequestV.class);
		String id = "75303ccf-48be-43ab-b3e3-adb49e106d69";
		try {
			TranscoRequestV request = requestDao.findById(UUID.fromString(id));
			assertNotNull(request);
			assertEquals(request.getIdStr(), id);
		} catch (MCASException e) {
			e.printStackTrace();
			fail();
		}
	}
}
