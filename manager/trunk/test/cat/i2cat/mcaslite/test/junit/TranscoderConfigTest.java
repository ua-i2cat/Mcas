package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertNotNull;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.config.dao.TranscoderConfigDao;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderConfigTest {
	
	@Test
	public void getConfigTest(){
		try {
			TranscoderConfig config = TranscoderConfigDao.findById(1);
			assertNotNull(config);
			Set<TLevel> levels = config.getLevels();
			for(TLevel level : levels){
				assertNotNull(level);
			}
			Set<TProfile> profiles = config.getProfiles();
			for (TProfile profile : profiles){
				assertNotNull(profile);
			}
			config = TranscoderConfigDao.findByName("default");
			assertNotNull(config);
		} catch (MCASException e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}

}
