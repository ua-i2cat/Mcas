package net.i2cat.mcas.junit;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TranscoRestServiceTest {

	public static final String uri = "http://localhost:8081/MCASlite";
	
	@Test
	public void getStatusTest(){
		WebResource service = createServiceConnection();
		UUID validId = UUID.randomUUID();
		getGood(validId.toString(), service, validId);
		getBad(validId.toString() + " ", service);
	}
	
	@Test
	public void addTranscoTest() {
		WebResource service = createServiceConnection();
		String badAPath = "{\"usr\":\"joe\",\"dst\":\"file://this/is/relative/path/destination\","
				+ "\"src\":\"file://this/is/relative/path/source/video.mp4\",\"config\":\"default\"}";
		String goodAPath = "{\"usr\":\"joe\",\"dst\":\"file:///etc/fstab\","
				+ "\"src\":\"file:///usr\",\"config\":\"default\"}";
		String goodMissUsr = "{\"src\":\"file:///home\","
				+ "\"dst\":\"file:///bin\",\"config\":\"default\"}";
		String badRPath = "{\"usr\":\"joe\",\"dst\":\"this/is/relative/path/destination\","
				+ "\"src\":\"this/is/relative/path/source/video.mp4\",\"config\":\"default\"}";
		String badURI = "{\"usr\":\"joe\",\"dst\":\"file:///this/is/absolut/path/destination\","
				+ "\"src\":\"@this/is/baduri/path/source/video.mp4\",\"config\":\"default\"}";
		String badScheme = "{\"usr\":\"joe\",\"dst\":\"http://www.google.com\","
				+ "\"src\":\"http://www.google.com\",\"config\":\"default\"}";
		String badDPath = "{\"usr\":\"joe\",\"dst\":\"file:///my/bad/destination/folder/\","
				+ "\"src\":\"file:///usr\",\"config\":\"default\"}";
		addBad(badAPath, service);
		addGood(goodAPath, service);
		addGood(goodMissUsr, service);
		addBad(badRPath, service);
		addBad(badURI, service);
		addBad(badScheme, service);
		addBad(badDPath, service);
	}
	
	private WebResource createServiceConnection(){
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		return service;
	}
	
	private void addGood(String input, WebResource service){
		ClientResponse response = service.path("/test/rest").type("application/json").post(ClientResponse.class, input);
		assertEquals(201,response.getStatus());
		assertTrue(response.hasEntity());
		try {
			UUID.fromString(response.getEntity(String.class));
		} catch(IllegalArgumentException e) {
			Assert.fail();
		}
	}
	
	private void addBad(String input, WebResource service){
		ClientResponse response = service.path("/test/rest").type("application/json").post(ClientResponse.class, input);
		assertEquals(400,response.getStatus());
	}
	
	private void getGood(String input, WebResource service, UUID id){
		String idStr = service.path("/test/rest").queryParam("id", input).get(String.class);
		try {
			UUID id2 = UUID.fromString(idStr);
			assertEquals(id,id2);
		} catch(IllegalArgumentException e) {
			Assert.fail();
		}
	}
	
	private void getBad(String input, WebResource service){
		String id = service.path("/test/rest").queryParam("id", input).get(String.class);
		boolean fail = false;
		try {
			UUID.fromString(id);
			Assert.fail();
		} catch(IllegalArgumentException e) {
			fail = true;
		}
		if (! fail){
			Assert.fail();
		}
	}
}
