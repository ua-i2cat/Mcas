package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class Connection  {

private static String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
protected static int httpTimeout = XMLReader.getIntParameter(path, "downloader.httptimeout");	
	
	
	public InputStream getInputStream (URI input) throws MCASException{
		try{
			if (input.getScheme().equals("file")) {
				return new FileInputStream(new File(input));
			} else if (input.getScheme().equals("http")) {
				URL url = input.toURL();
				URLConnection conn = url.openConnection();
				conn.setReadTimeout(httpTimeout);
				return conn.getInputStream();
			} else if (input.getScheme().equals("https")) {
				//TODO
				throw new MCASException();
			} else if (input.getScheme().equals("ftp")) {
				//TODO
				throw new MCASException();
			} else if (input.getScheme().equals("scp")) {
				//TODO
				throw new MCASException();
			} else if (input.getScheme().equals("blob")) {
				return blobToFile(input);
			} else {
				throw new MCASException();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	
	public OutputStream getOutputStream (URI destination, String fileName) throws MCASException {
		try {
			if(destination.getScheme().equals("file")){
				File file = new File(new URI(destination.getScheme(), destination.getHost(), destination.getPath() + RequestUtils.URIseparator + fileName, null));
				return new FileOutputStream(file);
			} else if(destination.getScheme().equals("blob")) {
				return fileToBlob(destination,fileName);
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
	
	protected OutputStream fileToBlob(URI destination, String fileName) throws MCASException {
		throw new MCASException();
	}

	protected InputStream blobToFile(URI input) throws MCASException {
		throw new MCASException();
	}
	
}
