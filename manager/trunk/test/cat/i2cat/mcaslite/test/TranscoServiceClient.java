package cat.i2cat.mcaslite.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TranscoServiceClient {
	
	public static final String uri = "http://localhost:8080/mcaslite";
	
	public static void main(String[] args){
		BufferedReader br;
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		int request;
		try {
			while (true){
				System.out.println("Choose request [1-5]: ");
				br = new BufferedReader(new InputStreamReader(System.in));
				request = Integer.decode(br.readLine());
				switch(request){
					case 1:
						System.out.println(addTransco(service, br));
						break;
					case 2:
						System.out.println(getState(service, br));
						break;
					case 3: 
						System.out.println(getUris(service, br));
						break;
					case 4: 
						cancel(service, br);
						break;
					case 5: 
						System.out.println(addLiveTransco(service, br));
						break;
					default:
						break;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private static String addTransco(WebResource service, BufferedReader br) {
		try {
			System.out.println("Write a Source to transcode:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String src = br.readLine();
			System.out.println("Write a Destination place resulting contentse:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String dst = br.readLine();
			String input = "{\"config\":\"default\",\"title\":\"joe\",\"dst\":\"" + dst + "\","
				+ "\"src\":\"" + src + "\"}";
			ClientResponse response= service.path("/transco").type("application/json").post(ClientResponse.class, input);
			if (response.hasEntity()){
				return response.getEntity(String.class);
			} else {
				return "";
			}
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	private static String addLiveTransco(WebResource service, BufferedReader br) {
		try {
			System.out.println("Write a Source to transcode:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String src = br.readLine();
			System.out.println("Write a Destination place resulting contents:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String dst = br.readLine();
			String input = "{\"config\":\"live\",\"usr\":\"joe\",\"dst\":\"" + dst + "\","
				+ "\"src\":\"" + src + "\"}";
			ClientResponse response= service.path("/transco").type("application/json").post(ClientResponse.class, input);
			if (response.hasEntity()){
				return response.getEntity(String.class);
			} else {
				return "";
			}
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	private static String getState(WebResource service, BufferedReader br){
		try {
			System.out.println("Write id request to ask for:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String id = br.readLine();
			return service.path("/transco").queryParam("id", id).get(String.class);
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	private static String getUris(WebResource service, BufferedReader br) throws IOException{
		try {
			System.out.println("Write id request to ask for:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String id = br.readLine();
			return service.path("/transco/uris").queryParam("id", id).get(String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static void cancel(WebResource service, BufferedReader br){
		try {
			System.out.println("Write id request to cancel:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String id = br.readLine();
			System.out.println("Force interruption (true by default) [true/false]:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String interrupt = br.readLine();
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("id", id);
			queryParams.add("interrupt", interrupt);
			service.path("/transco/cancel").queryParams(queryParams).post(ClientResponse.class, "");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
