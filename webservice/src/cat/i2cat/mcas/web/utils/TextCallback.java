package cat.i2cat.mcas.web.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Callback;
import cat.i2cat.mcaslite.utils.Uploader;

public class TextCallback extends Callback {

	@Override
	public void callback(TRequest request) throws MCASException {
		Uploader upload = new Uploader(URI.create(request.getDst()));
		try {
			upload.upload(createTextByteArray(requestToTxt(request)), request.getTitle() + ".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String requestToJson(TRequest request) throws MCASException {
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
	
	public String requestToTxt(TRequest request) throws MCASException {
		String reqStr = "";
		reqStr += "id: " + request.getId() + "\r\n";
		reqStr += "title: " + request.getTitle() + "\r\n";
		reqStr += "status: " + request.getStatus().toString() + "\r\n";
		if (request.getTranscos().size() > 0) {
			reqStr += "uris:\r\n";
			for (SimpleEntry<String, Integer> uri : request.getUris()){
				reqStr += "\t" + uri.getKey() + "\r\n";
			}
		}
		return reqStr;
	}
	
	private byte[] createTextByteArray(String content) throws MCASException, IOException {
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		try {
			data.write(content);
			data.flush();
			return bufferedBytes.toByteArray();
		} catch (IOException e){
			e.printStackTrace();
			throw new MCASException();
		} finally {
			data.close();
		}
	}
}