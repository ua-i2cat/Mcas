package cat.i2cat.mcas.web.utils;

import java.util.AbstractMap.SimpleEntry;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Callback;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class WebCallback extends Callback {

	@Override
	public void callback(TRequest request) throws MCASException {
		Client client = Client.create();
		WebResource service = client.resource(request.getOrigin());
		service.path("/transco/update").type("application/json").post(ClientResponse.class, RequestToJson(request));
	}

	@Override
	public String RequestToJson(TRequest request) throws MCASException {
		try {
			JSONObject json = new JSONObject();
			json.put("id", request.getId());
			json.put("status", request.getStatus().toString());
			if (request.getTranscos().size() > 0) {
				JSONArray jsonAr = new JSONArray();
				for (SimpleEntry<String, Integer> uri : request.getUris()){
					if (uri.getValue() != null){
						jsonAr.put(new JSONObject("{url: '" + uri.getKey() + "', bitrate: '" + uri.getValue() + "'}"));
					} else {
						jsonAr.put(new JSONObject("{url: '" + uri.getKey() + "'}"));
					}
				}
				json.put("uris", jsonAr);
			}
			return json.toString();
		} catch (JSONException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

}
