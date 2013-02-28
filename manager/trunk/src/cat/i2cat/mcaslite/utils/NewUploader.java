package cat.i2cat.mcaslite.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;

public class NewUploader implements Cancellable {
	
	private int blockSize;
	private boolean cancelled = false;
	private URI destination;
	private boolean done = false;
	
	public NewUploader(URI destination){
		String path = Paths.get(System.getProperty("mcas.home"), "WEB-INF/config.xml").toString();
		this.blockSize = Integer.parseInt(XMLReader.getXMLParameter(path, "uploader.ublocksize"));
		this.destination = destination;
	}
	
	public void upload(Path origin) throws MCASException {
		try {
			File file = origin.toFile();
			if (file.isDirectory()){
				for (String fileName : file.list()){
					fileToOutputStream(Connection.getOutputStream(destination, fileName), new File(file, fileName));
				}
			} else if (file.exists() && !file.isDirectory()){
				fileToOutputStream(Connection.getOutputStream(destination, file.getName()), file);
			}
		} catch (Exception e) {
			throw new MCASException();
		}
	}
	
	public void uploadLive(byte[] byteArray, String fileName) throws MCASException{
		try {
			byteArrayToOutputStream(Connection.getOutputStream(destination, fileName), byteArray);
		} catch (Exception e) {
			throw new MCASException();
		}
	}
	
	
	private void fileToOutputStream(OutputStream out, File origin) throws MCASException, IOException {
		FileInputStream reader = null;
		BufferedOutputStream outStream = null;
		try {
			reader = new FileInputStream(origin);
			outStream = new BufferedOutputStream(out);
			byte[] buffer = new byte[blockSize];
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
	
	private void byteArrayToOutputStream(OutputStream out , byte[] byteArray) throws IOException, MCASException {
		ByteArrayInputStream reader = null;
		BufferedOutputStream outStream = null;
		try {
			reader = new ByteArrayInputStream(byteArray);
			outStream = new BufferedOutputStream(out);
			byte[] buffer = new byte[blockSize];
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
