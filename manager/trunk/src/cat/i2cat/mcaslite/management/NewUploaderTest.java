package cat.i2cat.mcaslite.management;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.NewUploader;

public class NewUploaderTest {
	
	private static Path origin;
	private static String pathName = "/home/marc/Videos/ka.mp4";
	private static String destinationName = "/home/marc/testUploader"; 
	private static URI destination;
	
	public static void main(String[] args) {
		
		try {
			destination = new URI(destinationName);
			origin = new File(pathName).toPath();
			NewUploader uploader = new NewUploader(origin,destination);
			uploader.upload();
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MCASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
