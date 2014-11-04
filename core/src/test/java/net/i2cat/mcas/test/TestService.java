package net.i2cat.mcas.test;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TestService {

public static final String uri = "http://localhost:8080/MCASlite";
	
	public static void main(String[] args){
		String src = "file:///home/david/Videos/galaxy.mov";
		String dst;
		String input;
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		try {
			for(int i = 0; i < 20; i++){
				dst = "file:///home/david/galaxy" + i + ".mov";
				input = "{\"usr\":\"joe\",\"dst\":\"" + dst + "\","
						+ "\"src\":\"" + src + "\"}";
				Thread.sleep(500);
				service.path("/transco").type("application/json").post(ClientResponse.class, input);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
