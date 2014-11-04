package net.i2cat.mcas.junit.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.config.model.Transco;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.utils.DefaultsUtils;
import net.i2cat.mcas.utils.MediaUtils;

import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;


@RunWith(PowerMockRunner.class)
@PrepareForTest(MediaUtils.class)
public class MediaUtilsTest {

	private String relativeWorkingDir = "this/is/relative/path";
	private String absoluteWorkingDir = "/this/is/absolute/path";
	private static String fakeHome = "/fake/home/path";
	private static TranscoderConfig tConfig;
	private String fakeId = "fakeId";
	
	@BeforeClass
	public static void setup(){
		System.setProperty("mcas.home", fakeHome);
		tConfig = DefaultsUtils.tConfigGetDefaults();
	}
	
	@After
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void testGetWorkDirRelative(){
		assertEquals(fakeHome + "/" + relativeWorkingDir,MediaUtils.getWorkDir(relativeWorkingDir));
	}
	
	@Test
	public void testGetWorkDirAbsolute(){
		assertEquals(absoluteWorkingDir,MediaUtils.getWorkDir(absoluteWorkingDir));
	}
	
	@Test
	public void testSetInWorkDirExists() throws Exception{
		File inDirMock = createMock(File.class);
		expectNew(File.class,tConfig.getInputWorkingDir()).andReturn(inDirMock).times(2);
		expect(inDirMock.exists()).andReturn(true).once();
		expect(inDirMock.isDirectory()).andReturn(true).once();
		expect(inDirMock.isAbsolute()).andReturn(true).once();
		expect(inDirMock.getPath()).andReturn(tConfig.getInputWorkingDir()).times(2);
		
		File outDirMock = createMock(File.class);
		expectNew(File.class,tConfig.getOutputWorkingDir()).andReturn(outDirMock).times(2);
		expect(outDirMock.exists()).andReturn(false).once();
		expect(outDirMock.isAbsolute()).andReturn(true).once();
		expect(outDirMock.mkdirs()).andReturn(true).once();
		expect(outDirMock.getPath()).andReturn(tConfig.getOutputWorkingDir()).times(2);
		
		File inFileMock = createMock(File.class);
		expectNew(File.class,tConfig.getInputWorkingDir() + "/" + fakeId).andReturn(inFileMock);
		expect(inFileMock.getPath()).andReturn(tConfig.getInputWorkingDir() + "/" + fakeId);
		
		replayAll();
		
		File file = MediaUtils.setInFile(fakeId, tConfig);
		assertEquals(file.getPath(), tConfig.getInputWorkingDir() + "/" + fakeId);
	}
	
	@Test(expected = MCASException.class)
	public void testSetInWorkDirExistsFail() throws Exception{
		File inDirMock = createMock(File.class);
		expectNew(File.class,tConfig.getInputWorkingDir()).andReturn(inDirMock).times(2);
		expect(inDirMock.exists()).andReturn(true).once();
		expect(inDirMock.isDirectory()).andReturn(false).once();
		expect(inDirMock.isAbsolute()).andReturn(true).once();
		expect(inDirMock.getPath()).andReturn(tConfig.getInputWorkingDir()).once();
		
		replayAll();
		
		File file = MediaUtils.setInFile(fakeId, tConfig);
		assertEquals(file.getPath(), tConfig.getInputWorkingDir() + "/" + fakeId);
	}
	
	@Test
	public void testDeleteFileExist() throws Exception{
		File inFileMock = createMock(File.class);
		expectNew(File.class, fakeId).andReturn(inFileMock).once();
		expect(inFileMock.exists()).andReturn(true).once();
		expect(inFileMock.delete()).andReturn(true).once();
		
		replayAll();
		
		assertTrue(MediaUtils.deleteFile(fakeId));
	}
	
	@Test
	public void testDeleteFileNoExist() throws Exception{
		File inFileMock = createMock(File.class);
		expectNew(File.class, fakeId).andReturn(inFileMock).once();
		expect(inFileMock.exists()).andReturn(false).once();
		
		replayAll();
		
		assertTrue(! MediaUtils.deleteFile(fakeId));
	}
	
	@Test
	public void testCleanWithOut(){
		suppress(method(MediaUtils.class, "cleanTranscos"));
		TRequest request = TRequest.getEqualRequest(UUID.randomUUID());
		Transco transco = new Transco();
		request.addTrancoded(transco);
		MediaUtils.clean(request);
	}
	
	@Test
	public void testCleanWithoutOut(){
		suppress(method(MediaUtils.class, "deleteInputFile"));
		TRequest request = TRequest.getEqualRequest(UUID.randomUUID());
		request.setTConfig(tConfig);
		MediaUtils.clean(request);
	}

}
