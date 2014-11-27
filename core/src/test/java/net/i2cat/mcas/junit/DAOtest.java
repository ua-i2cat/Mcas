package net.i2cat.mcas.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.exceptions.MCASException;

public class DAOtest {

	@Test
	public void getRequestTest(){
		DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
		String id = "75303ccf-48be-43ab-b3e3-adb49e106d69";
		try {
			TRequest request = requestDao.findById(UUID.fromString(id));
			assertNotNull(request);
			assertEquals(request.getId().toString(), id);
		} catch (MCASException e) {
			e.printStackTrace();
			fail();
		}
	}
}
