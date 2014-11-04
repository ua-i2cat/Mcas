package net.i2cat.mcas.junit.utils.test;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.Transco;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.utils.DefaultsUtils;
import net.i2cat.mcas.utils.TranscoderUtils;

import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TranscoderUtils.class)
@SuppressStaticInitializationFor("net.i2cat.mcas.config.dao.DAO")
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
		verifyAll();
		resetAll();
	}

	@Test
	public void testLoadConf() throws MCASException {
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		assertTrue(tConfig.getName().equals(DEFAULT));
	}
	
	@Test
	public void testTranscoBuilder_1st() throws Exception{
		File goodFileMock = createMock(File.class);
		expectNew(File.class, FAKEDST).andReturn(goodFileMock).times(3);
		expect(goodFileMock.exists()).andReturn(true).times(3);
		expect(goodFileMock.isDirectory()).andReturn(true).times(3);
		expect(goodFileMock.canWrite()).andReturn(true).times(3);
		
		replay(goodFileMock, File.class);
		
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		List<Transco> transcos = TranscoderUtils.transcoBuilder(tConfig, FAKEREQ, FAKEDST);
		assertTrue(tConfig.getNumOutputs() == transcos.size());
	}
	
	@Test
	public void testTranscoBuilder_2nd() throws Exception{
		File goodFileMock = createMock(File.class);
		expectNew(File.class, FAKEDST).andReturn(goodFileMock).times(3);
		expect(goodFileMock.exists()).andReturn(true).times(3);
		expect(goodFileMock.isDirectory()).andReturn(true).times(3);
		expect(goodFileMock.canWrite()).andReturn(true).times(3);
		
		replay(goodFileMock, File.class);
		
		TranscoderConfig tConfig = TranscoderUtils.loadConfig(FAKECONFIG);
		List<Transco> transcos = TranscoderUtils.transcoBuilder(tConfig, FAKEREQ, FAKEDST);
		assertEquals("ffmpeg -i /This/Is/Fake/Home/input/fakeRequestId -s 1280x720 -b:v 1024k  -ac 2 -b:a 128k  -f mp4 -codec:v libx264 -codec:a libfaac -y /This/Is/Fake/Home/output/fakeRequestIddefault.mp4",transcos.get(0).getCommand().trim());
	}

}
