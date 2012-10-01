package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import cat.i2cat.mcaslite.entities.ApplicationConfig;

public class ApplicationConfigTest {
	
	@Test
	public void getInputWorkingDirTest(){
		String input = ApplicationConfig.getInputWorkingDir();
		assertNotNull(input);
		File file = new File(input);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}
	
	@Test
	public void getMaxTest(){
		int i = ApplicationConfig.getMaxTransco();
		assertTrue(i > 0);
		i = ApplicationConfig.getMaxInMediaH();
		assertTrue(i > 0);
	}

}
