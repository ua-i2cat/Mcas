package net.i2cat.mcas.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;

import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.Cancellable;
import net.i2cat.mcas.management.ClassFactory;

public class Downloader implements Cancellable {

	private int blockSize; 
	private Connection conn;
	private boolean cancelled = false;
	private URI input;
	private File destination;
	private boolean done = false;
	
	public Downloader(URI input, File destination){
		String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
		this.blockSize = XMLReader.getIntParameter(path, "downloader.dblocksize");
		this.input = input;
		this.destination = destination;
		this.conn = ClassFactory.getConnection(XMLReader.getStringParameter(path, "connection"));
	}
	
	public void toWorkingDir() throws MCASException{
		try{
			inputStreamToFile(conn.getInputStream(input));
		}catch(IOException e){ 
			
			 
			
			 
			
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
