package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

import cat.i2cat.mcaslite.utils.RequestUtils;

public class RequestUtilsTest {
	
	@Test
	public void requestValidSrcUri(){
		URI uri = URI.create("http://www.google.cat");
		assertTrue(RequestUtils.isValidSrcUri(uri));
		uri = URI.create("http://www.google.pop");
		assertTrue(! RequestUtils.isValidSrcUri(uri));
		uri = URI.create("file://etc/fstab");
		assertTrue(! RequestUtils.isValidSrcUri(uri));
		uri = URI.create("file:///etc/fstab");
		assertTrue(RequestUtils.isValidSrcUri(uri));
	}

}
