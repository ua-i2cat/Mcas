package net.i2cat.mcas.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class PostBigTest {
	
	public static final String uri = "http://192.168.1.2:8080/MCASlite";
	
	public static void main(String[] str){
		System.out.println("Which requests file would you like to check:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		URI baseURI = UriBuilder.fromUri(uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		String state;
		try {
			FileInputStream fstream = new FileInputStream(br.readLine());
			DataInputStream in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if (strLine.contains(" ERR")){
					state = service.path("/transco").queryParam("id", strLine.split(" ", 2)[0]).get(String.class);
					if (! state.equals("ERROR")){
						System.out.println(strLine + " KO ERROR");
					}
				} else if (strLine.contains(" PERR")) {
					state = service.path("/transco").queryParam("id", strLine.split(" ", 2)[0]).get(String.class);
					if (! state.equals("PARTIAL_ERROR")){
						System.out.println(strLine + " KO PARTIAL_ERROR");
					}
				} else {
					state = service.path("/transco").queryParam("id", strLine).get(String.class);
					if (! state.equals("DONE")){
						System.out.println(strLine + " KO DONE");
					} 
				}
				Thread.sleep(250);
			}
			fstream.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
