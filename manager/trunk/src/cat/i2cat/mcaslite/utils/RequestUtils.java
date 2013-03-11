package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class RequestUtils {	
	public static final String PATH = Paths.get(System.getProperty("mcas.home"), "WEB-INF/config.xml").toString();
	public static final String CALLBACK = XMLReader.getStringParameter(PATH, "callback");

	public static boolean isValidSrcUri(URI uri) {
		try {
			if (isValidUri(uri)){
				if (uri.getScheme().equals("file")) {
					File file = new File(uri.getPath());
					return file.exists();
				} else if (uri.getScheme().equals("http")) {
					HttpURLConnection httpCon = (HttpURLConnection) uri.toURL().openConnection();
					httpCon.setRequestMethod("HEAD");
					return (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK);
				} else if (uri.getScheme().equals("rtp") || uri.getScheme().equals("rtsp")) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static boolean isValidUri(URI uri){
		if (! uri.isAbsolute()) {
			return false;
		} else if (uri.getScheme().equals("file")) {
			if (uri.getAuthority() != null) {
				return false;
			} else {
				return true;
			}
		} else if (uri.getScheme().equals("http")) {
			UrlValidator urlValidator = new UrlValidator();
			return urlValidator.isValid(uri.toString());
		} else if (uri.getScheme().equals("https")) {
			return false;
		} else if (uri.getScheme().equals("ftp")) {
			return false;
		} else if (uri.getScheme().equals("rtp") || uri.getScheme().equals("rtsp")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isValidDestination(URI uri){
		if (! isValidUri(uri) || FilenameUtils.getBaseName(uri.getPath()).equals("")){
			return false;
		}
		return true;
	}
	
	

	public static void callback(TRequest request) throws MCASException{
		Client client = Client.create();
		WebResource service = client.resource(CALLBACK);
		service.path("/transco/update").type("application/json").post(ClientResponse.class, request.toJSON());
	}
}
