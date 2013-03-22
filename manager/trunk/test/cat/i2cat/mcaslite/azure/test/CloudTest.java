package cat.i2cat.mcaslite.azure.test;

import java.util.Date;
import java.util.UUID;

import cat.i2cat.mcaslite.azure.client.TranscoServiceAzureClient;
import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.cloud.VideoEntity;

public class CloudTest {
		
	public static String cancel(String ventityMsg)
	{
		try {
			String[] keys = ventityMsg.split("\\*");
			VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
			String cancel = video.getCancelId();
			String out = TranscoServiceAzureClient.cancelTask(cancel);
			return(out);
		} catch (Exception e) {
			e.printStackTrace();
			return("cancel KO");
		}
	}
	
	public static void main(String[] args) {
		String cc = "output";
		String pk = (new Date()).toString();
		String rk = UUID.randomUUID().toString();				
		
		String fa = "/Users/i2cat/test/video.avi";
		String na = "videoavi";
		String fm = "/Users/i2cat/test/video.mp4";
		String nm = "videomp4";
		String fc = "/Users/i2cat/test/corrupte.mp4";
		String nc = "corrupte";
		String fs = "/Users/i2cat/test/video.sh";
		String ns = "sh";
		String fz = "/Users/i2cat/test/video.zip";
		String nz = "zip";
		String listf[] = {fm, fm , fc, fs, fz};
		String listf2[] = {fa ,fa, fa, fa, fa};
		String listn[] = {nm, nm , nc, ns, nz};
		String listn2[] = {na ,na, na, na, na};
		
		String buv = "";
		String buf = "http://storagevideos.blob.core.windows.net/videoentity/mediafals";
		String listb[] = {buf, buv};
		
		String idf = "fals*fals";
		String idm = pk + "**" + rk;
		String listId[] = {idf, idm}; 
		for (int j = 0; j < 100; j++) {
			//Upload content testing
			for (int i = 0; i < listf.length; i++) {
				String blobUrlf = TranscoServiceAzureClient.uploadContent(listf[i], listn[i]);
				System.out.println(blobUrlf);
				String ventityMessage = TranscoServiceAzureClient.addVideoEntity(blobUrlf, (new Date()).toString(),
				UUID.randomUUID().toString(), cc);
				System.out.println(ventityMessage);
				
				String messageQueue = TranscoServiceAzureClient.addMessageQueue(ventityMessage);
				System.out.println(messageQueue);			
			}
			
			//Add videoentity with wrong link testing
			for (int i = 0; i < listb.length; i++) {
				String ventityMessage1 = TranscoServiceAzureClient.addVideoEntity(listb[i], (new Date()).toString(),
						UUID.randomUUID().toString(), cc);
				System.out.println(ventityMessage1);
				String messageQueue = TranscoServiceAzureClient.addMessageQueue(ventityMessage1);
				System.out.println(messageQueue);
			}		
			
			//Add message queue with wrong id
			for (int i = 0; i < listId.length; i++) {
				String messageQueue = TranscoServiceAzureClient.addMessageQueue(listId[i]);
				System.out.println(messageQueue);
			}
		}		
				
	}		
}
