package cat.i2cat.mcaslite.junit.utils.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;

import cat.i2cat.mcaslite.utils.RequestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RequestUtils.class)
public class RequestUtilsTest {
	
	private static String fakeFile = "file:///FakeFile";
	private static String malformedUri_1st = "file://thisIsNotAbsolutePath";
	private static String malformedUri_2nd = "ftp://fakeUsr@badScheme.com";
	private static String malformedUri_3rd = "http:/badUri.org";
	
	@After
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void testValidFileSrc() throws Exception{
		File fileMock = createMock(File.class);
		expectNew(File.class, URI.create(fakeFile).getPath()).andReturn(fileMock);
		expect(fileMock.exists()).andReturn(true).once();
		
		replayAll();
		
		URI uri = URI.create(fakeFile);
		assertTrue(RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testInValidFileSrc() throws Exception{	
		URI uri = URI.create(fakeFile);
		assertTrue(! RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testValidHttpSrc(){
		URI uri = URI.create("http://www.google.cat");
		assertTrue(RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testInvalidHttpSrc(){
		URI uri = URI.create("http://www.google.pop");
		assertTrue(! RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testMalformedUri_1st(){
		URI uri = URI.create(malformedUri_1st);
		assertTrue(! RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testMalformedUri_2nd(){
		URI uri = URI.create(malformedUri_2nd);
		assertTrue(! RequestUtils.isValidSrcUri(uri));
	}
	
	@Test
	public void testMalformedUri_3rd(){
		URI uri = URI.create(malformedUri_3rd);
		assertTrue(! RequestUtils.isValidSrcUri(uri));
	}
	
	//TODO: test jsonBuilder

}
