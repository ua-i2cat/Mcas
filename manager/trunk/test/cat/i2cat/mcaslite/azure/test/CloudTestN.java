package cat.i2cat.mcaslite.azure.test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import cat.i2cat.mcaslite.azure.client.TranscoServiceAzureClient;
import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.cloud.VideoEntity;

public class CloudTestN {
	
	public static void main(String[] args) {					
		String fm = "/Users/i2cat/test/video.mp4";
		String fc = "/Users/i2cat/test/corrupte.mp4";
		String fz = "/Users/i2cat/test/video.zip";
				
		String buv = "";
		String buf = "http://storagevideos.blob.core.windows.net/videoentity/mediafals";
		String buc = "http://storagevideos.blob.core.windows.net/videoentity/corrupte";
		
		String idf = "fals*fals";
		String idm = (new Date()).toString() + "**" + UUID.randomUUID().toString();
		
		Testing video = new Testing(15, 1, fm, buf, new Date() + "*" + UUID.randomUUID());
		Testing corrupte = new Testing(15, 2, fc, buc, idm);
		Testing incorrecte = new Testing(15, 3, fz, buv, idf);		
			
		Thread v = new Thread(video);
		Thread c = new Thread(corrupte);
		Thread z = new Thread(incorrecte);		
				
		v.setDaemon(true);
		v.start();
		
		c.setDaemon(true);
		c.start();
		
		z.setDaemon(true);
		z.start();
		
		try {
			v.join();
			c.join();
			z.join();
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
	}		
}

class Testing implements Runnable {
	private int nReq, seed;
	private String file, blobUrl, Id;
	
	public Testing(int nReq, int seed, String file, String blobUrl, String Id){
		this.nReq = nReq;
		this.seed = seed;
		this.file = file;
		this.blobUrl = blobUrl;
		this.Id = Id;		
	}	
	
	@Override
	public void run() {
		Random r = new Random(seed);
		String cc = "output";
		int sleep = 0;
		int up = 0;
		int ve = 0;
		int mq = 0;
		int c = 0;
		for (int j = 0; j < nReq; j++) {
			//Upload content testing
			try {
				sleep = (r.nextInt(50) + 50);
				Thread.sleep(sleep * 15);
				if(sleep < 55){
					up++;
					//Upload content testing						
					String blobUrlf = TranscoServiceAzureClient.uploadContent(file);
					System.out.println(blobUrlf);
					String ventityMessage = TranscoServiceAzureClient.addVideoEntity(blobUrlf, (new Date()).toString(),
					UUID.randomUUID().toString(), cc);
					System.out.println(ventityMessage);
					
					String messageQueue = TranscoServiceAzureClient.addMessageQueue(ventityMessage);
					System.out.println(messageQueue);						
				} else if (sleep >= 55 && sleep < 60){
					ve++;
					//Add videoentity with wrong link testing
					String ventityMessage = TranscoServiceAzureClient.addVideoEntity(blobUrl, (new Date()).toString(),
					UUID.randomUUID().toString(), cc);
					System.out.println(ventityMessage);
					String messageQueue = TranscoServiceAzureClient.addMessageQueue(ventityMessage);
					System.out.println(messageQueue);									
				} else  if (sleep >= 60 && sleep < 65 ){
					c++;
					String[] keys = Id.split("\\*");
					VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
					String cancel = video.getCancelId();
					String out = TranscoServiceAzureClient.cancelTask(cancel);
					System.out.println(out);						
				} else {
					mq++;
					//Add message queue with wrong id
					String messageQueue = TranscoServiceAzureClient.addMessageQueue(Id);
					System.out.println(messageQueue);
				}					
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("File:" + file + " nReq: " + nReq + " Seed: " + seed + " UpContent: " + up + " VEntity: " + ve + " MQueue: " + mq + " Cancels: " + c);
		}
	}
}