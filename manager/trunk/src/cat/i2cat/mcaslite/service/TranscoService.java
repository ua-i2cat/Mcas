package cat.i2cat.mcaslite.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import cat.i2cat.mcaslite.cloud.CloudManager;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.RequestUtils;

public class TranscoService {
	
	private static TranscoHandler transcoH;
	private static CloudManager client;
	private static Thread managerTh;
	private static Thread clientTh;
	
	public static void main(String[] args){
	
		transcoH = TranscoHandler.getInstance();
		managerTh = new Thread(transcoH);
		managerTh.setName("MainManager");
		managerTh.setDaemon(true);
		managerTh.start();
		
		client = CloudManager.getInstance();
		clientTh = new Thread(client);
		clientTh.setName("MainManager");
		clientTh.setDaemon(true);
		clientTh.start();
		public class ConfigReader {
			
			private static String inputFile = FilenameUtils.concat(System.getProperty("mcas.home"),"META-INF/config.xml");
			
			public static String configGetter(String configAttribute){
				
			String returnItem = null;
				
			try { 
					SAXBuilder builder = new SAXBuilder();
					File xmlFile = new File(inputFile);
			 
					Document doc = (Document) builder.build(xmlFile);
					
					Element el = doc.getRootElement();
					String[] children = configAttribute.split("\\.");
					
					for (String child : children){
							el = el.getChild(child);
					}
					
					returnItem =  el.getText();
					
				  } catch (IOException io) {
					io.printStackTrace();
				  } catch (JDOMException e) {
					e.printStackTrace();
				  }
			
			return returnItem;
			
			}
		}
		
		
	}
}
