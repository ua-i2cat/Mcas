package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;


public class RequestUtils {
	
	public static final String CALLBACK = "http://localhost:8080/mcasWeb";

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
				} else if (uri.getScheme().equals("rtp")) {
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
		} else if (uri.getScheme().equals("rtp")) {
			if (uri.getPort() > 1024) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	public static boolean isValidDestination(URI uri){
		if (! isValidUri(uri) || FilenameUtils.getBaseName(uri.getPath()).equals("")){
			return false;
		}
		return true;
	}
	
	public static String destinationJSONbuilder(TRequest request) throws MCASException {
		if (!(request.getStatus().getInt() == Status.DONE)  && !(request.getStatus().getInt() == Status.P_ERROR)
				&& !(request.getStatus().getInt() == Status.PROCESS_L)){
			throw new MCASException();
		}
		JSONArray jsonAr = new JSONArray();
		try {
			for(Transco transco : request.getTranscoded()){
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("uri", transco.getDestinationUri());
				jsonAr.put(jsonObj);
			}
			return (new JSONObject()).put("uris", jsonAr).toString();
		} catch (JSONException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static void callback(TRequest request) throws MCASException{
		Client client = Client.create();
		WebResource service = client.resource(CALLBACK);
		service.path("/transco/update").type("application/json").post(ClientResponse.class, request.toJSON());
	}
}
