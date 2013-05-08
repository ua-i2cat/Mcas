package cat.i2cat.mcaslite.junit.utils.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Downloader;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Downloader.class)
public class DownloaderTest {

	private static File destination = new File("fakePath");
	private static Downloader downloader;
	
	@BeforeClass
	public static void setup() throws Exception {
		FileOutputStream fileOutMock = createMock(FileOutputStream.class);
		expectNew(FileOutputStream.class, new Class<?>[]{ File.class },(File) anyObject()).andReturn(fileOutMock);
		fileOutMock.write((byte[]) anyObject(), anyInt(), anyInt());
		expectLastCall().anyTimes();
		fileOutMock.close();
		replayAll();
		downloader = new Downloader(URI.create("http://download.thinkbroadband.com/1GB.zip"), destination);
		Runnable downloaderR = new Runnable(){
			@Override public void run(){
				try {
					downloader.toWorkingDir();
				} catch (MCASException e) {
					Assert.fail();
				}
			}
		};
		(new Thread(downloaderR)).start();
		Thread.sleep(250);
	}
	
	@AfterClass
	public static void tearDown() {
		verifyAll();
	}
	
	@Test
	public void testStartDonwload() throws InterruptedException, IOException, MCASException {
		assertTrue(downloader.isRunning());
	}
	
	@Test
	public void testCancelDownload(){
		assertTrue(downloader.cancel(true));
	}
	
	@Test(timeout=2000)
	public void testCancelledDownload(){
		while(downloader.isRunning());
		assertTrue(! destination.exists());
	}
}
