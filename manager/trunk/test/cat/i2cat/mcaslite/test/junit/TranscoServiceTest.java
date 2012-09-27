package cat.i2cat.mcaslite.test.junit;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TranscoServiceTest {

	public static final String uri = "http://localhost:8081/MCASlite";
	
	@Test
	public void addTranscoTest() throws InterruptedException {
		WebResource service = createServiceConnection();
		String test = "{\"usr\":\"joe\",\"dst\":\"file:///home/david/prova.mp3\","
				+ "\"src\":\"file:///home/david/Videos/big_buck_bunny.avi\",\"config\":\"default\"}";
		String[] id = new String[5]; 
		for(int i = 0; i < 5; i++){
			id[i] = addTransco(test, service);
		}
		long endTimeMillis = System.currentTimeMillis() + 1000*60*10;
		while(true){
			for(int i = 0; i < 5; i++){
				System.out.println(((Integer) i).toString() + " " + getState(id[i], service));
			}
			if (System.currentTimeMillis() > endTimeMillis) return;
			if (getState(id[4], service).equals("T_PROCESS")) return;
			Thread.sleep(1000);
		}
	}
	
	private WebResource createServiceConnection(){
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		return service;
	}
	
	private String addTransco(String input, WebResource service){
		ClientResponse response = service.path("/transco").type("application/json").post(ClientResponse.class, input);
		assertEquals(201,response.getStatus());
		assertTrue(response.hasEntity());
		try {
			return response.getEntity(String.class);
		} catch(IllegalArgumentException e) {
			Assert.fail();
		}
		return null;
	}
	
	private String getState(String input, WebResource service){
		return service.path("/transco").queryParam("id", input).get(String.class);
	}
	
}
