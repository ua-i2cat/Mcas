package cat.i2cat.mcaslite.test.junit;

import java.io.File;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Downloader;

public class DownloaderTest implements Runnable{

	private Downloader down;
	private String dst = "testDown";
	
	@Test
	public void donwloaderTest() throws InterruptedException{
		Thread th = new Thread(this);
		th.start();
		File destination = new File(dst);
		Thread.sleep(1000);
		if (destination.exists()){
			down.cancel(true);
		} else {
			Thread.sleep(30000);
			if (destination.exists()){
				down.cancel(true);
			} else {
				Assert.fail();
			}
		}
		if (destination.exists()){
			Thread.sleep(1000);
			if (destination.exists()){
				Assert.fail();
			}
		} 
	}

	@Override
	public void run() {
		File destination = new File(dst);
		URI input = URI.create("http://ftp.akl.lt/Video/Big_Buck_Bunny/big_buck_bunny_480p_stereo.ogg");
		try {
			down = new Downloader(input, destination);
			down.toWorkingDir();
		} catch (MCASException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
