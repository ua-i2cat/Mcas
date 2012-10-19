package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class RequestUtils {

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
		} else if (uri.getScheme().equals("scp")) {
			return false;
		}
		
		return false;
	}
	
	public static boolean isValidDestination(URI uri){
		if (! isValidUri(uri) || FilenameUtils.getBaseName(uri.getPath()).equals("")){
			return false;
		}
		return true;
	}
	
	public static String destinationJSONbuilder(TranscoRequest request) throws MCASException{
		if (!request.getState().equals(State.DONE) && !request.getState().equals(State.PARTIAL_ERROR)){
			throw new MCASException();
		}
		String json = "{\n \"destinationUris\": [\n";
		for(Transco transco : request.getTranscoded()){
			json += "{\"uri\":\"" + transco.getDestinationUri() + "\"}\n";
		}
		json += "]\n}";
		return json;
	}
}
