package cat.i2cat.mcaslite.test;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class BenchmarkTest {

	public static final String uri = "http://192.168.1.2:8080/MCASlite";

	public static final String input = "{\"usr\":\"joe\",\"dst\":\"file:///home/i2cat/prova\","
			+ "\"src\":\"file:///home/i2cat/maninman.avi\"}";
	
	public static void main(String[] args) throws InterruptedException{
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		ClientResponse response = new ClientResponse(0, null, null, null);
		for(int i = 1; i <= 4; i++){
			for(int j = 1; j <= i; j++){
				response = service.path("/transco").type("application/json").post(ClientResponse.class, input);
			}
			if (! response.hasEntity()) return;
			String id = response.getEntity(String.class);
			String state = "CREATED";
			while(! state.equals("DONE") && ! state.equals("ERROR") && ! state.equals("PARTIAL_ERROR")){
				Thread.sleep(3000);
				state = service.path("/transco").queryParam("id", id).get(String.class);
			}
			System.out.println(state);
			Thread.sleep(5000);
			System.out.println("Iteration " + i + " done.");
		}
	}
}
