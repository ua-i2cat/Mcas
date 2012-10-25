package cat.i2cat.mcaslite.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class Downloader implements Runnable {

	private static final int BLOCK_SIZE 	= 1024*100;
	private static final int HTTP_TIMEOUT 	= 30*1000;
	
	private boolean cancelled = false;
	private URI input;
	private File destination;
	private Thread th;
	
	public Downloader(URI input, File destination){
		this.input = input;
		this.destination = destination;
	}
	
	public void toWorkingDir() throws MCASException{
		th = new Thread(this);
		th.start();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public boolean cancel(boolean mayInterruptIfRunning){
		if (mayInterruptIfRunning){
			setCancelled(true);
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			if (input.getScheme().equals("file")) {
				fileToFile();
			} else if (input.getScheme().equals("http")) {
				httpToFile();
			} else if (input.getScheme().equals("https")) {
				//TODO
				throw new MCASException();
			} else if (input.getScheme().equals("ftp")) {
				//TODO
				throw new MCASException();
			} else if (input.getScheme().equals("scp")) {
				//TODO
				throw new MCASException();
			}
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	private void fileToFile() throws MCASException{
		try {
			inputStreamToFile(new FileInputStream(new File(input.getPath())));
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void httpToFile() throws MCASException {
		try {
			URL url = input.toURL();
			URLConnection conn = url.openConnection();
			conn.setReadTimeout(HTTP_TIMEOUT);
			inputStreamToFile(conn.getInputStream());
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
			writer = new FileOutputStream(destination);
			inStream = new BufferedInputStream(in);
			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead = 0;
			while ((bytesRead = inStream.read(buffer)) != -1) {
		        if (isCancelled()){
		        	return;
		        }
		        writer.write(buffer, 0, bytesRead);
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
				MediaUtils.deleteFile(destination.getPath());
			}
		}
	}
	
	private boolean isCancelled(){
		return cancelled;
	}
	
	private void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}
	
	public boolean isRunning(){
		if (th != null){
			return th.isAlive();
		}
		return false;
	}
}
