package cat.i2cat.mcaslite.brush;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class URIParser {

	
	public static void main(String[] args){
		while (true){
			System.out.println("Write an URI to parse:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				URI uri = new URI(br.readLine());
				System.out.println("schemeSpecific: " + (( uri.getSchemeSpecificPart() == null ) ? "NULL" : uri.getSchemeSpecificPart()));
				System.out.println("scheme: " + (( uri.getScheme() == null ) ? "NULL" : uri.getScheme()));
				System.out.println("authority: " + (( uri.getAuthority() == null ) ? "NULL" : uri.getAuthority()));
				System.out.println("host: " + (( uri.getHost() == null ) ? "NULL" : uri.getHost()));
				System.out.println("user: " + (( uri.getUserInfo() == null ) ? "NULL" : uri.getUserInfo()));
				System.out.println("port: " + (( uri.getPort() < 0 ) ? "NULL" : ((Integer) uri.getPort()).toString()));
				System.out.println("path: " + (( uri.getPath() == null ) ? "NULL" : uri.getPath()));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
}
