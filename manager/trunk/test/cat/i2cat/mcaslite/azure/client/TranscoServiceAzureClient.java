package cat.i2cat.mcaslite.azure.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	public static String uploadContent(String src) {
		try{
			CloudStorageAccount storageAccount = 
			CloudStorageAccount.parse(storageConnectionString);

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("videoentity");
			//container.createIfNotExist();

			CloudBlockBlob blob = container.getBlockBlobReference("file");
			File source = new File(src);
			blob.upload(new FileInputStream(source), source.length());
			return (blob.getUri().toString());
		}
		catch (FileNotFoundException fileNotFoundException){
			fileNotFoundException.printStackTrace();
		    System.out.print("FileNotFoundException encountered: ");
		    System.out.println(fileNotFoundException.getMessage());
		    return("Error - fileNotFound");		    
		}
		catch (StorageException storageException){
			storageException.printStackTrace();
		    System.out.print("StorageException encountered: ");
		    System.out.println(storageException.getMessage());	
		    return("Error - Storage");
		}
		catch (URISyntaxException uriSyntaxException){
			uriSyntaxException.printStackTrace();
		    System.out.print("URISyntaxException encountered: ");
		    System.out.println(uriSyntaxException.getMessage());
		    return("Error - Uri");
		}
		catch (Exception e){
			e.printStackTrace();
		    System.out.print("Exception encountered: ");
		    System.out.println(e.getMessage());
		    return("Error");		    
		}
	}
	
	public static String addVideoEntity(String blobUrl, String partitionKey, String rowKey, String container){
		try{
			CloudStorageAccount storageAccount = 
			CloudStorageAccount.parse(storageConnectionString);					
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
	
			VideoEntity ventity = new VideoEntity(partitionKey, rowKey);
			ventity.setFileName("video2.avi");
			ventity.setDescription("");
			ventity.setCategory("");
			ventity.setTitle("");
			ventity.setUniqueFileName("uniqueFileName");
			ventity.setVideoUploadedUrl(blobUrl); 
			ventity.setStatus("");
			ventity.setCancelId("");
			ventity.setTenantContainer(container);
			
			TableOperation insertVideoEntity = TableOperation.insert(ventity);
			tableClient.execute(VideoEntity.class.getSimpleName(), insertVideoEntity);
			
			System.out.println("Processing complete.");			
			return(partitionKey + "*" + rowKey);				
		}
		catch (StorageException storageException){
			storageException.printStackTrace();
		    System.out.print("StorageException encountered: ");
		    System.out.println(storageException.getMessage());	
		    return("Error - Storage");
		}
		catch (URISyntaxException uriSyntaxException){
			uriSyntaxException.printStackTrace();
		    System.out.print("URISyntaxException encountered: ");
		    System.out.println(uriSyntaxException.getMessage());
		    return("Error - Uri");
		}
		catch (Exception e){
			e.printStackTrace();
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
			
			CloudQueue queue = queueClient.getQueueReference("video2queue");
			
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
		String blobUrl;
		String container = "output2";
		int timeOut = 5;
		try {
			while(true){
				System.out.println("Choose an option:");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				int option = Integer.parseInt(br.readLine());
				switch (option){
				case 1:
					System.out.println("Insert a source to upload:");
					String src = br.readLine();
					blobUrl = uploadContent(src);
					System.out.println(blobUrl);
					String partitionKey = (new Date()).toString();
					String rowKey = UUID.randomUUID().toString();
					String msg = addVideoEntity(blobUrl, partitionKey, rowKey, container);
					System.out.println(msg);
					break;
				
				case 2:
					System.out.println("Insert message in the queue:");
					String message = br.readLine();
					System.out.println(addMessageQueue(message));
					break;
					
				case 3:
					System.out.println("Message to cancel:");
					String cancelMessage = br.readLine();
					String[] keys = cancelMessage.split("\\*");
					VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
					System.out.println(cancelTask(video.getCancelId()));
					
				
				default:
					System.out.println("Code not valid. 1-Upload video   2-Insert message in queue");
					break;
				}
			}
		
		} catch (Exception e){
			e.printStackTrace();
		}		
				
//			message = addMessageQueue(msg);
//			System.out.println(message);
//			String msg2 = retrieveMessage(timeOut).getMessageContentAsString();
//			System.out.println(msg2);
//			String[] keys = msg.split("\\*");
//			VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
//			String cancel = video.getCancelId();
//			String msg3 = cancelTask(cancel);
//			System.out.println(msg3);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (StorageException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MCASException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}				
	}	
}