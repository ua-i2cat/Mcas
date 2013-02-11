package cat.i2cat.mcaslite.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;
import cat.i2cat.mcaslite.management.ConfigReader;

public class Uploader implements Cancellable {

	private static final int BLOCK_SIZE = Integer.parseInt(ConfigReader.configGetter("uploader.ublocksize"));
	
	private boolean cancelled = false;
	private URI destination;
	private boolean done = false;
	
	public Uploader(URI destination){
		this.destination = destination;
	}
	
	public void toDestinationUri(File origin) throws MCASException{
		if (destination.getScheme().equals("file")) {
			fileToFile(origin, new File(destination.getPath()));
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
	
	public void toDestinationUri(byte[] byteArray, String fileName) throws MCASException{
		if (destination.getScheme().equals("file")) {
			byteArrayToFile(byteArray, new File(destination.getPath()), fileName);
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
	
	private void byteArrayToFile(byte[] byteArray, File dst, String fileName) throws MCASException {
		try {
			if (dst.exists() && dst.isDirectory() && dst.canWrite()) {
				inputStreamToFile(new ByteArrayInputStream(byteArray), new File(destination.getPath(), fileName));
			} else if(! dst.exists() && dst.getParentFile().canWrite()) {
				inputStreamToFile(new ByteArrayInputStream(byteArray), new File(destination.getPath()));
			} else {
				throw new MCASException();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void fileToFile(File origin, File destination) throws MCASException{
		try {
			if (origin.isDirectory() && (new File(destination.getPath())).isDirectory()){
				for (String file : origin.list()){
					inputStreamToFile(new FileInputStream(new File(origin, file)), new File(destination, file));
				}
			} else if (! destination.exists() && destination.getParentFile().canWrite()) {
				inputStreamToFile(new FileInputStream(origin), destination);
			} else if (destination.exists() && destination.isDirectory() && destination.canWrite()){
				inputStreamToFile(new FileInputStream(origin), new File(destination.toString(), origin.getName()));
			} else {
				throw new MCASException();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void inputStreamToFile(InputStream in, File destination) throws IOException {
		FileOutputStream writer = null;
		BufferedInputStream inStream = null;
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
	
	public void deleteContent(String content) throws MCASException{
		if (destination.getScheme().equals("file")) {
			deleteFile(content, new File(destination.getPath()));
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
	
	private void deleteFile(String fileName, File dst) throws MCASException{
		try {
			if (dst.isDirectory() && dst.canWrite()){
				Files.deleteIfExists(Paths.get(dst.getPath(), fileName));
			} else {
				Files.deleteIfExists(Paths.get(dst.getPath()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
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