package cat.i2cat.mcaslite.azure.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.cloud.VideoEntity;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.XMLReader;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.queue.client.CloudQueue;
import com.microsoft.windowsazure.services.queue.client.CloudQueueClient;
import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;

public class TranscoServiceAzureClient {
	
	private static final String path = "config/config.xml";
	private static final String storageConnectionString = 
		    "DefaultEndpointsProtocol=" + XMLReader.getStringParameter(path, "cloud.connection.protocol") + ";" + 
	   	    "AccountName=" + XMLReader.getStringParameter(path, "cloud.connection.accountName") + ";" + 
	  	    "AccountKey=" + XMLReader.getStringParameter(path, "cloud.connection.accountKey");
	
	public static String uploadContent() {
		try{
			CloudStorageAccount storageAccount = 
			CloudStorageAccount.parse(storageConnectionString);

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("videoentity");
			container.createIfNotExist();
			
			System.out.println("Write a Source to transcode:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String src = br.readLine();
			CloudBlockBlob blob = container.getBlockBlobReference("mediafile");
			File source = new File(src);
			blob.upload(new FileInputStream(source), source.length());
			return (blob.getUri().toString());
		}
		catch (FileNotFoundException fileNotFoundException){
		    System.out.print("FileNotFoundException encountered: ");
		    System.out.println(fileNotFoundException.getMessage());
		    return("Error");		    
		}
		catch (StorageException storageException){
		    System.out.print("StorageException encountered: ");
		    System.out.println(storageException.getMessage());	
		    return("Error");
		}
		catch (URISyntaxException uriSyntaxException){
		    System.out.print("URISyntaxException encountered: ");
		    System.out.println(uriSyntaxException.getMessage());
		    return("Error");
		}
		catch (Exception e){
		    System.out.print("Exception encountered: ");
		    System.out.println(e.getMessage());
		    return("Error");		    
		}
	}
	
	private static String addVideoEntity(String blobUrl){
		try{
			CloudStorageAccount storageAccount = 
			CloudStorageAccount.parse(storageConnectionString);
					
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
					
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("videoentity");
			container.createIfNotExist();
			
			String partitionKey = (new Date()).toString();
			String rowKey = UUID.randomUUID().toString();
			
			VideoEntity ventity = new VideoEntity(partitionKey, rowKey);
			ventity.setFileName("video2.avi");
			ventity.setDescription("");
			ventity.setCategory("");
			ventity.setTitle("");
			ventity.setVideoUploadedUrl(blobUrl); 
			ventity.setStatus("");
			ventity.setCancelId("");
			ventity.setTenantContainer(container.getName());
			
			TableOperation insertVideoEntity = TableOperation.insert(ventity);
			tableClient.execute(VideoEntity.class.getSimpleName(), insertVideoEntity);
			
			System.out.println("Processing complete.");			
			return(partitionKey + "*" + rowKey);				
		}
		catch (StorageException storageException){
		    System.out.print("StorageException encountered: ");
		    System.out.println(storageException.getMessage());	
		    return("Error");
		}
		catch (URISyntaxException uriSyntaxException){
		    System.out.print("URISyntaxException encountered: ");
		    System.out.println(uriSyntaxException.getMessage());
		    return("Error");
		}
		catch (Exception e){
		    System.out.print("Exception encountered: ");
		    System.out.println(e.getMessage());
		    return("Error");		    
		}		
	}
	
	public static String addMessageQueue(String msg){
		try{
			CloudStorageAccount storageAccount = 
				    CloudStorageAccount.parse(storageConnectionString);
			
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			
			CloudQueue queue = queueClient.getQueueReference("videoqueue");
			
			queue.createIfNotExist();
			
			CloudQueueMessage message = new CloudQueueMessage(msg);

			queue.addMessage(message);
			return("OK");			
		}catch (Exception e){
			e.printStackTrace();
			return("KO");
		}
	}
	
	private static CloudQueueMessage retrieveMessage(int timeOut){
		try{
			CloudStorageAccount storageAccount = 
				    CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference("videoqueue");
			CloudQueueMessage retrievedMessage = queue.retrieveMessage(timeOut, null, null);
			return(retrievedMessage);
			
		}catch(Exception e){
			e.printStackTrace();
			return(null);	
		}			
	}
	
	public static String cancelTask(String cancelId){
		try {
			CloudStorageAccount storageAccount = 
				    CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference("cancelqueue");
			queue.createIfNotExist();
			CloudQueueMessage cancelMessage = new CloudQueueMessage(cancelId);
			queue.addMessage(cancelMessage);			
			return("OK");
		} catch (Exception e) {
			e.printStackTrace();
			return("KO");
		}		
	}

	public static void main (String...args){
		String blobUrl, msg, message;
		int timeOut = 5;
		blobUrl = uploadContent();
		System.out.println(blobUrl);
		msg = addVideoEntity(blobUrl);
		System.out.println(msg);
			
		message = addMessageQueue(msg);
		System.out.println(message);
		
		try{
			String msg2 = retrieveMessage(timeOut).getMessageContentAsString();
			System.out.println(msg2);
			String[] keys = msg.split("\\*");
			VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
			String cancel = video.getCancelId();
			String msg3 = cancelTask(cancel);
			System.out.println(msg3);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}	
}