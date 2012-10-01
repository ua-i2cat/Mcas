package cat.i2cat.mcaslite.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TranscoServiceClient {
	
	public static final String uri = "http://localhost:8081/MCASlite";
	
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
						+ "\"src\":\"" + src + "\",\"config\":\"default\"}";
				service.path("/transco").type("application/json").post(ClientResponse.class, input);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
