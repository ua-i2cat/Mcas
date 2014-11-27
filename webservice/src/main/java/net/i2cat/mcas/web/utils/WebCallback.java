package net.i2cat.mcas.web.utils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.Callback;

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
			if (request.getTranscoded().size() > 0) {
				JSONArray jsonAr = new JSONArray();
				for (String uri : request.getUris()){
					jsonAr.put(new JSONObject("{uri: '" + uri + "'}"));
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
