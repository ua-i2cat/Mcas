package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class Connection {
	
	public static OutputStream getOutputStream (URI destination, String fileName) throws MCASException {
		try {
			if(destination.getScheme().equals("file")){
				File file = new File(new URI(destination.getScheme(), destination.getHost(), destination.getPath() + RequestUtils.URIseparator + fileName, null));
				return new FileOutputStream(file);
			} else if(destination.getScheme().equals("blob")) {
				return AzureUtils.fileToOutputStream(Paths.get(destination.getPath()).getName(0).toString(), fileName);
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

}
