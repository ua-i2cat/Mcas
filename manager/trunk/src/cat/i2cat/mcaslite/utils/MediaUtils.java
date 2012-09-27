package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import cat.i2cat.mcaslite.entities.ApplicationConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class MediaUtils {

	public static void toWorkingDir(URI uri, String id) throws MCASException {
		try {
			if (uri.getScheme().equals("file")) {
				FileUtils.copyFile(new File(uri.getPath()), new File(ApplicationConfig.getInputWorkingDir() + id));
			} else if (uri.getScheme().equals("http")) {
				//TODO
			} else if (uri.getScheme().equals("https")) {
				//TODO
			} else if (uri.getScheme().equals("ftp")) {
				//TODO
			} else if (uri.getScheme().equals("scp")) {
				//TODO
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void toDestinationUri(String file, URI uri) throws MCASException {
		try {
			if (uri.getScheme().equals("file")) {
				FileUtils.copyFile(new File(file), new File(uri.getPath()));
			} else if (uri.getScheme().equals("http")) {
				//TODO
			} else if (uri.getScheme().equals("https")) {
				//TODO
			} else if (uri.getScheme().equals("ftp")) {
				//TODO
			} else if (uri.getScheme().equals("scp")) {
				//TODO
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static void deleteInput(String id) {
		File file = new File(ApplicationConfig.getInputWorkingDir() + id);
		if (file.exists()){
			file.delete();
		}
	}

}
