package cat.i2cat.mcaslite.junit.utils.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Uploader;

import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Uploader.class)
public class UploaderTest {

	private static File source = new File("file:///fakePath/fakeSrc");
	private static Uploader uploader;
	private static URI fakeDestination = URI.create("file:///home/fakeUsr/fakeFile");
	
	@BeforeClass
	public static void setup() throws Exception {
		File fileMock = createMock(File.class);
		expectNew(File.class, new Class<?>[]{ String.class },(String) anyObject()).andReturn(fileMock);
		
		FileOutputStream fileOutMock = createMock(FileOutputStream.class);
		expectNew(FileOutputStream.class, new Class<?>[]{ File.class },(File) anyObject()).andReturn(fileOutMock);
		fileOutMock.write((byte[]) anyObject(), anyInt(), anyInt());
		expectLastCall().anyTimes();
		fileOutMock.close();
		
		FileInputStream fileInMock = createMock(FileInputStream.class);
		expectNew(FileInputStream.class, new Class<?>[]{ File.class },(File) anyObject()).andReturn(fileInMock);
		
		BufferedInputStream buffInMock = createMock(BufferedInputStream.class);
		expectNew(BufferedInputStream.class, (InputStream) anyObject()).andReturn(buffInMock);
		expect(buffInMock.read((byte[]) anyObject())).andReturn(1024*100).anyTimes();
		buffInMock.close();
		
		replayAll();
		
		uploader = new Uploader(fakeDestination, source);
		Runnable downloaderR = new Runnable(){
			@Override public void run(){
				try {
					uploader.toDestinationUri();
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
	public void testStartDonwload() {
		assertTrue(uploader.isRunning());
	}
	
	@Test
	public void testCancelDownload(){
		assertTrue(uploader.isRunning() && uploader.cancel(true));
	}
	
	@Test(timeout=2000)
	public void testCancelledDownload(){
		while(uploader.isRunning());
		assertTrue(! (new File(fakeDestination.getPath())).exists());
	}
	
}
