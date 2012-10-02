package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class RequestUtils {

	public static boolean isValidSrcUri(URI uri){
		if (isValidUri(uri)){
			File file = new File(uri.getPath());
			return file.exists();
		}
		return false;
	}
	
	public static boolean isValidUri(URI uri){
		if (! uri.isAbsolute()) {
			return false;
		} else if (uri.getScheme().equals("file")) {
			if (uri.getAuthority() != null) {
				return false;
			} else {
				return true;
			}
		} else if (uri.getScheme().equals("http")) {
			return false;
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
	
	public static boolean obsoleteRequest(TranscoRequest request){
		
		return false;
	}
}
