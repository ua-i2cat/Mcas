package cat.i2cat.mcaslite.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;

public class Uploader implements Cancellable {

	private static final int BLOCK_SIZE = Integer.parseInt(XMLReader.getXMLParameter("config/config.xml", "uploader.ublocksize"));;
	
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
		} else if (destination.getScheme().equals("blob")) {
			fileToBlob(origin);
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
		} else if (destination.getScheme().equals("blob")) {
			byteArrayToBlob(byteArray, fileName);
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
	
	private void fileToBlob(File origin) throws MCASException {
		try {
			if (origin.isDirectory()){
				for (String file : origin.list()){
					fileToOutputStream(AzureUtils.fileToOutputStream(new File(origin, file), 
						Paths.get(destination.getPath()).getName(0).toString(), file), new File(file));
				}
			} else if (origin.exists()){
				fileToOutputStream(AzureUtils.fileToOutputStream(origin, 
						Paths.get(destination.getPath()).getName(0).toString(), 
						Paths.get(destination.getPath()).getFileName().toString()), origin);
			} else {
				throw new MCASException();
			}
		} catch (IOException e){
			throw new MCASException();
		}
	}
	
	private void byteArrayToBlob(byte[] byteArray, String fileName) throws MCASException {
		AzureUtils.byteArrayToBlob(byteArray, 
				Paths.get(destination.getPath()).getName(0).toString(), 
				fileName);
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
	
	private void fileToOutputStream(OutputStream out, File origin) throws IOException {
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
				deleteDestination(destination);
			}
		}
	}
	
	//TODO: delete it
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
		} else if (destination.getScheme().equals("blob")) {
			//deleteBlob(content, destination.getPath());
		}
	}
	
	//TODO: delete it
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

	public static URI createDestinationDir(String id, URI destination) throws MCASException {
		if (destination.getScheme().equals("file")) {
			return makerDestinationDir(id, destination);
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
		} else if (destination.getScheme().equals("blob")) {
			return destination;
		} else {
			throw new MCASException();
		}
	}
	
	private static URI makerDestinationDir(String id, URI dst) throws MCASException{
		URI path = TranscoderUtils.getDestinationDir(dst, id);
		if (! (new File(path)).mkdirs()){
			throw new MCASException();
		} else {
			return path;
		}
	}

	public static boolean deleteDestination(String dst) {
		try {
			URI destination = new URI(dst);
			return deleteDestination(destination);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean deleteDestination(URI destination){
		if (destination.getScheme().equals("file")) {
			try {
				return Files.deleteIfExists(Paths.get(destination));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else if (destination.getScheme().equals("http")) {
			//TODO
			return false;
		} else if (destination.getScheme().equals("https")) {
			//TODO
			return false;
		} else if (destination.getScheme().equals("ftp")) {
		//TODO
			return false;
		} else if (destination.getScheme().equals("scp")) {
			//TODO
			return false;
		} else if (destination.getScheme().equals("blob")) {
			return AzureUtils.deleteBlob(destination.getPath(), 
				Paths.get(destination.getPath()).getFileName().toString());
		} else {
			return false;
		}
	}
}