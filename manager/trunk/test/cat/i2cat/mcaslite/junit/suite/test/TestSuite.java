package cat.i2cat.mcaslite.junit.suite.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cat.i2cat.mcaslite.junit.management.test.MediaHandlerTest;
import cat.i2cat.mcaslite.junit.management.test.TranscoQueueTest;
import cat.i2cat.mcaslite.junit.management.test.TranscoderTest;
import cat.i2cat.mcaslite.junit.service.test.TranscoServiceTest;
import cat.i2cat.mcaslite.junit.utils.test.DownloaderTest;
import cat.i2cat.mcaslite.junit.utils.test.MediaUtilsTest;
import cat.i2cat.mcaslite.junit.utils.test.RequestUtilsTest;
import cat.i2cat.mcaslite.junit.utils.test.TranscoderUtilsTest;
import cat.i2cat.mcaslite.junit.utils.test.UploaderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	DownloaderTest.class,
	MediaUtilsTest.class,
	RequestUtilsTest.class,
	TranscoderUtilsTest.class,
	UploaderTest.class,
	TranscoServiceTest.class,
	MediaHandlerTest.class,
	TranscoderTest.class,
	TranscoQueueTest.class
})
public class TestSuite {}
