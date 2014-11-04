package net.i2cat.mcas.junit.suite.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import net.i2cat.mcas.junit.management.test.MediaHandlerTest;
import net.i2cat.mcas.junit.management.test.TranscoHandlerTest;
import net.i2cat.mcas.junit.management.test.TranscoQueueTest;
import net.i2cat.mcas.junit.management.test.TranscoderTest;
import net.i2cat.mcas.junit.service.test.TranscoServiceTest;
import net.i2cat.mcas.junit.utils.test.DownloaderTest;
import net.i2cat.mcas.junit.utils.test.MediaUtilsTest;
import net.i2cat.mcas.junit.utils.test.RequestUtilsTest;
import net.i2cat.mcas.junit.utils.test.TranscoderUtilsTest;
import net.i2cat.mcas.junit.utils.test.UploaderTest;

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
	TranscoQueueTest.class,
	TranscoHandlerTest.class
})
public class TestSuite {}
