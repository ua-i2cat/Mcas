package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

public class TranscoderUtilsTest {

	@Test
	public void transcoBuilderTest(){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			TranscoderConfig config = tConfigDao.findByName("default");
			List<Transco> transcos = TranscoderUtils.transcoBuilder(config, UUID.fromString("04e119ed-8862-42ba-b8ee-22e3d97df550").toString(), "file:///home/david/prova.unusedextension");
			for(Transco transco : transcos){
				assertNotNull(transco);
				assertEquals("/home/david/work/input/04e119ed-8862-42ba-b8ee-22e3d97df550", transco.getInputFile());
				assertTrue(transco.getDestinationUri().equals("file:///home/david/prova1.ogg") ||
						transco.getDestinationUri().equals("file:///home/david/prova1.webm") ||
						transco.getDestinationUri().equals("file:///home/david/prova1.mp4"));
				System.out.println(transco.getCommand());
				System.out.println(transco.getDestinationUri());
				System.out.println("---");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
