/**************************************
Test disabled as it has become obsolete
***************************************/

/*
package net.i2cat.mcas.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TLevel;
import net.i2cat.mcas.config.model.TProfile;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;

public class TranscoderConfigTest {
	
	@Test
	public void getConfigTest(){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			TranscoderConfig config = tConfigDao.findById(1);
			assertNotNull(config);
			List<TProfile> profiles = config.getProfiles();
			for (TProfile profile : profiles){
				assertNotNull(profile);

				List<TLevel> levels = profile.getLevels();
				for(TLevel level : levels){
					assertNotNull(level);
				}
			}
			config = tConfigDao.findByName("default");
			assertNotNull(config);
		} catch (MCASException e) {
			e.printStackTrace();
			fail();
		}  
	}

}
*/