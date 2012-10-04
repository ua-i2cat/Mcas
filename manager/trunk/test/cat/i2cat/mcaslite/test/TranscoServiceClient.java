package cat.i2cat.mcaslite.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TranscoServiceClient {
	
	public static final String uri = "http://localhost:8080/MCASlite";
	
	public static void main(String[] args){
		BufferedReader br;
		String src;
		String dst;
		String input;
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		try {
			while (true){
				System.out.println("Write a Source to transcode:");
				br = new BufferedReader(new InputStreamReader(System.in));
				src = br.readLine();
				System.out.println("Write a Destination place resulting contentse:");
				br = new BufferedReader(new InputStreamReader(System.in));
				dst = br.readLine();
				input = "{\"usr\":\"joe\",\"dst\":\"" + dst + "\","
						+ "\"src\":\"" + src + "\"}";
				ClientResponse response= service.path("/transco").type("application/json").post(ClientResponse.class, input);
				if (response.hasEntity()){
					String id = response.getEntity(String.class);
					String state = "CREATED";
					while(! state.equals("DONE") && ! state.equals("ERROR") && ! state.equals("PARTIAL_ERROR")){
						Thread.sleep(1000*5);
						state = service.path("/transco").queryParam("id", id).get(String.class);
						System.out.println(state);
					}
					state = service.path("/transco/uris").queryParam("id", id).get(String.class);
					System.out.println(state);
				} else {
					Assert.fail();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
