package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderConfigTest {
	
	@Test
	public void getConfigTest(){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			TranscoderConfig config = tConfigDao.findById(1);
			assertNotNull(config);
			List<TLevel> levels = config.getLevels();
			for(TLevel level : levels){
				assertNotNull(level);
			}
			List<TProfile> profiles = config.getProfiles();
			for (TProfile profile : profiles){
				assertNotNull(profile);
			}
			config = tConfigDao.findByName("default");
			assertNotNull(config);
		} catch (MCASException e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}

}
