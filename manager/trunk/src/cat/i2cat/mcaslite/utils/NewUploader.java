package cat.i2cat.mcaslite.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;

public class NewUploader implements Cancellable {
	
private static final int BLOCK_SIZE = 102400;
	
	private boolean cancelled = false;
	private URI destination;
	private Path origin;
	private boolean done = false;
	
	public NewUploader(Path origin, URI destination){
		this.origin = origin;
		this.destination = destination;
	}
	
	public void upload() throws MCASException {
		try {
			File file = origin.toFile();
			if (file.isDirectory()){
				for (String fileName : file.list()){
					fileToOutputStream(Connection.getOutputStream(destination, fileName), new File(file, fileName));
				}
			} else if (file.exists() && !file.isDirectory()){
				fileToOutputStream(Connection.getOutputStream(destination, file.getName()), file);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void fileToOutputStream(OutputStream out, File origin) throws IOException, MCASException {
		FileInputStream reader = null;
		BufferedOutputStream outStream = null;
		try {
			reader = new FileInputStream(origin);
			outStream = new BufferedOutputStream(out);
			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead = 0;
			while ((bytesRead = reader.read(buffer)) != -1) {
		        if (isCancelled()){
		        	return;
		        }
		        outStream.write(buffer, 0, bytesRead);
		    }
			done = true;
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (outStream != null){
				outStream.close();
			}
			if (! done){
				throw new MCASException();
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
