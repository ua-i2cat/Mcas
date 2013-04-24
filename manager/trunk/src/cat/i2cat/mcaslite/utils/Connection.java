package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class Connection {
	
	public static OutputStream getOutputStream (URI destination, String fileName) throws MCASException {
		try {
			if(destination.getScheme().equals("file")){
				File file = new File(new URI(destination.getScheme(), destination.getHost(), destination.getPath() + "/" + fileName, null));
				return new FileOutputStream(file);
			}
			throw new MCASException();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MCASException();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void deleteFromServer (URI destination, String fileName) throws MCASException {
		try {
			if(destination.getScheme().equals("file")){
				Files.delete(Paths.get(new URI(destination.getScheme(), destination.getHost(), destination.getPath() + "/" + fileName, null)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MCASException();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

}
