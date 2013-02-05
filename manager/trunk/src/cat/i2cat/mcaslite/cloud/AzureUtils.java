package cat.i2cat.mcaslite.cloud;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.EnumSet;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

import com.microsoft.windowsazure.services.blob.client.BlobContainerPermissions;
import com.microsoft.windowsazure.services.blob.client.BlobContainerPublicAccessType;
import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.queue.client.CloudQueue;
import com.microsoft.windowsazure.services.queue.client.CloudQueueClient;
import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;
import com.microsoft.windowsazure.services.queue.client.MessageUpdateFields;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableEntity;
import com.microsoft.windowsazure.services.table.client.TableOperation;

public class AzureUtils {
	
	private static final String storageConnectionString = 
		    "DefaultEndpointsProtocol=http;" + 
	   	    "AccountName=storagevideos;" + 
	  	    "AccountKey=qesnMc8PWB9tvMi2IaH3E4OuEVTmyX893T8f6OqwaatGeb23F/vZR8+pq6d5paQWYcZSUArJVGhqvaFESYUW0A==";
	private static final String cloudQueue = "videoqueue";

	public static CloudQueueMessage retrieveMessage(int timeout) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference(cloudQueue);
			return queue.retrieveMessage(timeout, null, null);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		} 
	}
	
	public static void updateMessage(CloudQueueMessage message, boolean newContent, int timeout) throws MCASException  {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference(cloudQueue);
			EnumSet<MessageUpdateFields> updateFields;
			if (newContent){
				updateFields = EnumSet.of(MessageUpdateFields.CONTENT, MessageUpdateFields.VISIBILITY);
			} else {
				updateFields = EnumSet.of(MessageUpdateFields.VISIBILITY);
			}
			queue.updateMessage(message, timeout, updateFields, null, null);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static <T extends TableEntity> T getEntity(String partitonKey, String rowKey, String tableName, Class<T> type) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			TableOperation retrieveEntity =  TableOperation.retrieve(partitonKey, rowKey, type);
			return  tableClient.execute(tableName, retrieveEntity).getResultAsType();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private static void updateEntity(String partitonKey, String rowKey, String tableName, TableEntity entity) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			TableOperation replaceEntity = TableOperation.replace(entity);
			tableClient.execute(tableName, replaceEntity);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

		
	public static CloudBlob getFirstBlob(String containerName, String fileName) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			for (ListBlobItem blobItem : container.listBlobs(fileName)) {
				if (blobItem instanceof CloudBlob) {
					return (CloudBlob) blobItem;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
		throw new MCASException();
	}
	
	public static void fileToBlob(File file, String containerName, String fileName) throws MCASException{
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			if (container.exists()){
				CloudBlockBlob blob = container.getBlockBlobReference(fileName);
				blob.upload(new FileInputStream(file), file.length());
			} else {
				throw new MCASException();
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void byteArrayToBlob(byte[] byteArray, String containerName, String fileName) throws MCASException{
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			if (container.exists()){
				CloudBlockBlob blob = container.getBlockBlobReference(fileName);
				blob.upload(new ByteArrayInputStream(byteArray), byteArray.length);
			} else {
				throw new MCASException();
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static String createContainer(String containerName, boolean publish) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			container.createIfNotExist();
			if (publish){
				BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
				containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
				container.uploadPermissions(containerPermissions);
			}
			return container.getName();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void updateRequest(TRequest request) throws MCASException {
		try{ 
			String[] keys = CloudManager.getInstance().getCloudMessage(request.getId()).getMessageContentAsString().split("\\*");
			VideoEntity video = new VideoEntity(request, keys[0], keys[1]);
			updateEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), video);
		} catch (Exception e){
			
		}
	}
}
