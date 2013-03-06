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
import java.nio.file.Paths;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;

public class Downloader implements Cancellable {

	private int blockSize; 
	private int httpTimeout;
	private boolean cancelled = false;
	private URI input;
	private File destination;
	private boolean done = false;
	
	public Downloader(URI input, File destination){
		String path = Paths.get(System.getProperty("mcas.home"), "WEB-INF/config.xml").toString();
		this.blockSize = XMLReader.getIntParameter(path, "downloader.dblocksize");
		this.httpTimeout = XMLReader.getIntParameter(path, "downloader.httptimeout");
		this.input = input;
		this.destination = destination;
	}
	
	public void toWorkingDir() throws MCASException{
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
			conn.setReadTimeout(httpTimeout);
			inputStreamToFile(conn.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void inputStreamToFile(InputStream in) throws IOException {
		FileOutputStream writer = null;
		InputStream inStream = null;
		try {
			writer = new FileOutputStream(destination);
			inStream = new BufferedInputStream(in);
			byte[] buffer = new byte[blockSize];
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
	
	private void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning){
		if (mayInterruptIfRunning){
			setCancelled(true);
		}
		return true;
	}
}
