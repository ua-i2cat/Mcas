package cat.i2cat.mcaslite.test.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TranscoderUtils.class)
@SuppressStaticInitializationFor("cat.i2cat.mcaslite.config.dao.DAO")
public class TranscoderUtilsTest {
	
	
	@BeforeClass
	public static void setup() throws Exception{
		@SuppressWarnings("unchecked")
		DAO<TranscoderConfig> tConfigDaoMock = (DAO<TranscoderConfig>) createMock(DAO.class);
		expectNew(DAO.class, TranscoderConfig.class).andReturn(tConfigDaoMock).times(2);
		expect(tConfigDaoMock.findByName("default")).andReturn(DefaultsUtils.tConfigGetDefaults()).times(2);
		expect(tConfigDaoMock.findByName("fakeConfig")).andThrow(new MCASException()).times(2);
		replayAll();
	}
	
	@AfterClass
	public static void tearDown() {
		verify();
	}

	@Test
	public void testLoadConf() throws MCASException{
		TranscoderConfig tConfig = TranscoderUtils.loadConfig("fakeConfig");
		assertTrue(tConfig.getName().equals("default"));
	}

}
