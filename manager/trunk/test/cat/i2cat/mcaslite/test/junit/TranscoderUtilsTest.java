package cat.i2cat.mcaslite.test.junit;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.reset;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TranscoderUtils.class)
@SuppressStaticInitializationFor("cat.i2cat.mcaslite.config.dao.DAO")
public class TranscoderUtilsTest {
	
	private static String DEFAULT = "default";
	private static String FAKECONFIG = "fakeConfig";
	private static String FAKEDST = "fakeDst";
	private static String FAKEREQ = "fakeRequestId";
	private static String FAKEHOME =  "/This/Is/Fake/Home";
	
	@BeforeClass
	public static void setup() throws Exception{
		System.setProperty("mcas.home", FAKEHOME);
	}
	
	@AfterClass
	public static void tearDown() {
		verify();
	}
	
	@Before
	public void setUp() throws Exception{
		@SuppressWarnings("unchecked")
		DAO<TranscoderConfig> tConfigDaoMock = (DAO<TranscoderConfig>) createMock(DAO.class);
		expectNew(DAO.class, TranscoderConfig.class).andReturn(tConfigDaoMock);
		expect(tConfigDaoMock.findByName(DEFAULT)).andReturn(DefaultsUtils.tConfigGetDefaults());
		expect(tConfigDaoMock.findByName(FAKECONFIG)).andThrow(new MCASException());
		replayAll();
	}
	
	@After
	public void teardown(){
		reset(DAO.class);
	}

	@Test
	public void testLoadConf() throws MCASException {
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		assertTrue(tConfig.getName().equals(DEFAULT));
	}
	
	@Test
	public void testTranscoBuilder_1st() throws MCASException{
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		List<Transco> transcos = TranscoderUtils.transcoBuilder(tConfig, FAKEREQ, FAKEDST);
		assertTrue(tConfig.getNumOutputs() == transcos.size());
	}
	
	@Test
	public void testTranscoBuilder_2nd() throws MCASException{
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		List<Transco> transcos = TranscoderUtils.transcoBuilder(tConfig, FAKEREQ, FAKEDST);
		assertEquals("ffmpeg -i /This/Is/Fake/Home/input/fakeRequestId -s 1280x720 -b:v 1024k  -ac 2 -b:a 128k  -f mp4 -codec:v libx264 -codec:a libfaac -y /This/Is/Fake/Home/output/fakeRequestIddefault.mp4",transcos.get(0).getCommand().trim());
	}

}
