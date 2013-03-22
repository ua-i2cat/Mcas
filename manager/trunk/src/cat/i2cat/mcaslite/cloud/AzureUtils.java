package cat.i2cat.mcaslite.cloud;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.utils.XMLReader;

import com.microsoft.windowsazure.services.blob.client.BlobContainerPermissions;
import com.microsoft.windowsazure.services.blob.client.BlobContainerPublicAccessType;
import com.microsoft.windowsazure.services.blob.client.BlobOutputStream;
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
import com.microsoft.windowsazure.services.table.client.TableBatchOperation;
import com.microsoft.windowsazure.services.table.client.TableEntity;
import com.microsoft.windowsazure.services.table.client.TableOperation;

public class AzureUtils {
	
	private static final String path = "config/config.xml";
	private static final String storageConnectionString = 
		    "DefaultEndpointsProtocol=" + XMLReader.getStringParameter(path, "cloud.connection.protocol") + ";" + 
	   	    "AccountName=" + XMLReader.getStringParameter(path, "cloud.connection.accountName") + ";" + 
	  	    "AccountKey=" + XMLReader.getStringParameter(path, "cloud.connection.accountKey");

	public static CloudQueueMessage retrieveMessage(int timeout, String cloudQueue) throws MCASException {
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
	
	public static CloudQueueMessage peekMessage(String cloudQueue) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference(cloudQueue);
			return queue.peekMessage();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		} 
	}
	
	public static void deleteQueueMessage(CloudQueueMessage msg, String cloudQueue) throws MCASException{
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
			CloudQueue queue = queueClient.getQueueReference(cloudQueue);
			queue.deleteMessage(msg);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		} 
	}
	
	public static void updateMessage(CloudQueueMessage message, boolean newContent, int timeout, String cloudQueue) throws MCASException  {
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
	
	public static <T extends TableEntity> void insertEntities(String tableName, List<T> entities) throws MCASException {
		try {
			if (entities==null){
				return;
			} else {
				CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
				CloudTableClient tableClient = storageAccount.createCloudTableClient();
				TableBatchOperation batchOperation = new TableBatchOperation();
				for (TableEntity entity : entities){
					batchOperation.insert(entity);
				}
				tableClient.execute(tableName,batchOperation);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static boolean updateEntity(String partitonKey, String rowKey, String tableName, TableEntity entity) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			TableOperation replaceEntity = TableOperation.replace(entity);
			tableClient.execute(tableName, replaceEntity);
			return true;
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

		
	public static CloudBlob getFirstBlob(String cloudCont, String fileName) throws MCASException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(cloudCont);
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
	
	public static BlobOutputStream fileToOutputStream(String cloudCont, String fileName) throws MCASException{
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(cloudCont);
			container.createIfNotExist();
			//TODO: I should'nt create that container
			CloudBlockBlob blob = container.getBlockBlobReference(fileName);
			return blob.openOutputStream();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void byteArrayToBlob(byte[] byteArray, String cloudCont, String fileName) throws MCASException{
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(cloudCont);
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
	
	public static boolean updateVideoEntity(TRequest request) throws MCASException {
		try{ 
			String[] keys = CloudManager.getInstance().getCloudMessage(request.getId()).getMessageContentAsString().split("\\*");
			VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
			updateDateTime(request, video);
			video.updateFromRequest(request);
			return updateEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), video);
		} catch (Exception e){
			return false;
		}
	}
	
	private static void updateDateTime(TRequest request, VideoEntity video){
		if (request.getStatus().getInt() == Status.PROCESS_T || request.getStatus().getInt() == Status.PROCESS_L){
			video.setStartConvertion(new Date());
		} else if (request.getStatus().getInt() == Status.DONE){
			video.setEndConvertion(new Date());
			video.setEndJob(new Date());
		}
	}
	
	public static boolean deleteBlob(String blobName, String cloudCont) {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(cloudCont);
			for (ListBlobItem blobItem : container.listBlobs(blobName)) {
				if (blobItem instanceof CloudBlob) {
					((CloudBlob) blobItem).delete();
				} 
			}
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
}
