package cat.i2cat.mcaslite.service;

import java.net.URI;
import java.net.URISyntaxException;

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
	
		
	}
}
