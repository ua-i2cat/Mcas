package cat.i2cat.mcaslite.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;

public class Uploader implements Cancellable {

	private static final int BLOCK_SIZE 	= 1024*100;
	
	private boolean cancelled = false;
	private URI destination;
	private File origin;
	private boolean done = false;
	
	public Uploader(URI destination, File origin){
		this.destination = destination;
		this.origin = origin;
	}
	
	public void toDestinationUri() throws MCASException{
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
		BufferedInputStream inStream = null;
		try {
			writer = new FileOutputStream(new File(destination.getPath()));
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
	
	private void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}
	
	@Override
	public boolean isCancelled(){
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