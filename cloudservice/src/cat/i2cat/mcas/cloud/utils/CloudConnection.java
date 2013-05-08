package cat.i2cat.mcas.cloud.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import cat.i2cat.mcas.cloud.AzureUtils;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Connection;
import cat.i2cat.mcaslite.utils.XMLReader;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;

public class CloudConnection extends Connection {
	
	private static String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
	private static String cloudContainer = XMLReader.getStringParameter(path , "cloud.inContainer");

	@Override
	protected InputStream blobToFile(URI input) throws MCASException {
		try {
			CloudBlob blob = AzureUtils.getFirstBlob(cloudContainer, Paths.get(input.getPath()).getFileName().toString());
//			blob.download(new FileOutputStream(destination));
			return blob.openInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	@Override
	protected OutputStream fileToBlob(URI destination, String fileName) throws MCASException {
		return AzureUtils.fileToOutputStream(Paths.get(destination.getPath()).getName(0).toString(), fileName);
	}
	
	
}
