package cat.i2cat.mcaslite.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


import cat.i2cat.mcaslite.exceptions.MCASException;

public class Uploader implements Runnable {

	private static final int BLOCK_SIZE 	= 1024*100;
	
	private boolean cancel = false;
	private URI destination;
	private File origin;
	private boolean failed = false;
	
	public Uploader(URI destination, File origin){
		this.destination = destination;
		this.origin = origin;
	}
	
	public void toDestinationUri() throws MCASException{
		Thread th = new Thread(this);
		try {
			th.start();
			th.join();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
		if (isFailed()){
			throw new MCASException();
		}
	}

	public boolean cancel(boolean mayInterruptIfRunning){
		if (mayInterruptIfRunning){
			cancel = true;
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			if (destination.getScheme().equals("file")) {
				fileToFile();
			} else if (destination.getScheme().equals("http")) {
				//TODO
				throw new MCASException();
			} else if (destination.getScheme().equals("https")) {
				//TODO
				throw new MCASException();
			} else if (destination.getScheme().equals("ftp")) {
				//TODO
				throw new MCASException();
			} else if (destination.getScheme().equals("scp")) {
				//TODO
				throw new MCASException();
			}
		} catch (Exception e){
			e.printStackTrace();
			setFailed(true);
		}
	}
	
	private void fileToFile() throws MCASException{
		try {
			inputStreamToFile(new FileInputStream(origin));
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void inputStreamToFile(InputStream in) throws IOException {
		FileOutputStream writer = null;
		InputStream inStream = null;
		boolean done = false;
		try {
			writer = new FileOutputStream(new File(destination.getPath()));
			inStream = new BufferedInputStream(in);
			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead = 0;
			while ((bytesRead = in.read(buffer)) != -1) {
				writer.write(buffer, 0, bytesRead);
		        if (cancel){
		        	return;
		        }
		    }
			done = true;
		} finally {
			if (in != null) {
				inStream.close();
			}
			if (writer != null){
				writer.close();
			}
			if (! done){
				MediaUtils.deleteFile(origin.getPath());
				MediaUtils.deleteFile(destination.getPath());
			}
		}
	}
	
	private boolean isFailed(){
		return failed;
	}
	
	private void setFailed(boolean failed){
		this.failed = failed;
	}
}