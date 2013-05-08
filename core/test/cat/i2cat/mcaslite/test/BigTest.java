package cat.i2cat.mcaslite.test;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Random;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class BigTest {
	
	private static final String src = "file:///home/i2cat/testVid/";
	private static final String dst =  "file:///home/i2cat/testVid/output/";
	
	public static final String uri = "http://192.168.1.2:8080/MCASlite";

	public static void main(String[] args){
		Producer trailer = new Producer(50, src + "trailer.mov", dst + "trailer", 1, "/home/david/testVid/Trecord");
		Producer galaxy = new Producer(200, src + "galaxy.mov", dst + "galaxy", 2, "/home/david/testVid/Grecord");
		Producer fake = new Producer(200, src + "fakeVid.mp4", dst + "fakeVid", 3, "/home/david/testVid/Frecord");
		Producer notFound = new Producer(200, src + "notFound.mp4", dst + "notFound", 4, "/home/david/testVid/Nrecord");
		Producer maninman = new Producer(100, src + "maninman.avi", dst + "maninman", 5, "/home/david/testVid/Mrecord");
		
		Thread t = new Thread(trailer);
		Thread g = new Thread(galaxy);
		Thread f = new Thread(fake);
		Thread n = new Thread(notFound);
		Thread m = new Thread(maninman);
		
		t.setDaemon(true);
		t.start();
		
		g.setDaemon(true);
		g.start();
		
		f.setDaemon(true);
		f.start();
		
		m.setDaemon(true);
		m.start();
		
		n.setDaemon(true);
		n.start();
		
		try {
			t.join();
			m.join();
			g.join();
			f.join();
			n.join();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}

class Producer implements Runnable {

	private int nReq;
	private String src;
	private String dst;
	private int seed;
	private String file;
	
	public Producer(int nReq, String src, String dst, int seed, String file){
		this.nReq = nReq;
		this.src = src;
		this.dst = dst;
		this.seed = seed;
		this.file = file;
	}
	

	@Override
	public void run() {
		Random r = new Random(seed);
		URI baseURI = UriBuilder.fromUri(BigTest.uri).build();
		Client client = Client.create();
		WebResource service = client.resource(baseURI);
		String input = "";
		int pe = 0;
		int e = 0;
		int sleep = 0;
		String id = "";
		try {
			FileWriter outFile = new FileWriter(file);
			PrintWriter out = new PrintWriter(outFile);
			for(int i = 0; i < nReq; i ++){
				try {
					sleep = (r.nextInt(50) + 50);
					Thread.sleep(sleep * 15);
					if (sleep < 55){
						pe++;
						input = "{\"usr\":\"joe\",\"dst\":\"" + dst + "_" + i + "\","
								+ "\"src\":\"" + src + "\",\"config\":\"error\"}";
						ClientResponse response = service.path("/transco").type("application/json").post(ClientResponse.class, input);
						id = response.getEntity(String.class);
						out.println(id + " PERR");
					} else if (sleep >= 55 && sleep < 60){
						e++;
						input = "{\"usr\":\"joe\",\"dst\":\"" + dst.replace("output", "outpiff") + "_" + i + "\","
								+ "\"src\":\"" + src + "\",\"config\":\"error\"}";
						ClientResponse response = service.path("/transco").type("application/json").post(ClientResponse.class, input);
						id = response.getEntity(String.class);
						out.println(id + " ERR");
					} else {
						input = "{\"usr\":\"joe\",\"dst\":\"" + dst + "_" + i + "\","
								+ "\"src\":\"" + src + "\",\"config\":\"default\"}";
						ClientResponse response = service.path("/transco").type("application/json").post(ClientResponse.class, input);
						id = response.getEntity(String.class);
						out.println(id);
					}
				} catch (Exception e1){
					e1.printStackTrace();
				}
			}
			out.close();
		} catch (Exception e1){
			e1.printStackTrace();
		}
		System.out.println("For Src: " + src + " nReq: " + nReq + " Err: " + e + " PErr: " + pe);
	}
	
	
	
}
