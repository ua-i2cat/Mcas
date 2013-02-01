package cat.i2cat.mcaslite.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.queue.client.CloudQueue;
import com.microsoft.windowsazure.services.queue.client.CloudQueueClient;
import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableEntity;
import com.microsoft.windowsazure.services.table.client.TableOperation;

public class AzureUtils {

	public static CloudQueueMessage retrieveMessage(String storageConnectionString, String cloudQueue, int timeout) throws InvalidKeyException, URISyntaxException, StorageException {
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
		CloudQueue queue = queueClient.getQueueReference(cloudQueue);
		return queue.retrieveMessage(timeout, null, null);
	}
	
	public static void blobToInputStream(String storageConnectionString, String containerName) throws InvalidKeyException, URISyntaxException, StorageException, IOException{
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
		CloudBlobContainer container = blobClient.getContainerReference(containerName);
		for (ListBlobItem blobItem : container.listBlobs()) {
		    if (blobItem instanceof CloudBlob) {
		        CloudBlob blob = (CloudBlob) blobItem;
		        blob.download(null);
		    }
		}
	}
	
	public static Object getEntity(String storageConnectionString, String partitonKey, String rowKey, String tableName,Class<? extends TableEntity> type) throws InvalidKeyException, URISyntaxException, StorageException{
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		CloudTableClient tableClient = storageAccount.createCloudTableClient();
		TableOperation retrieveEntity =  TableOperation.retrieve(partitonKey, rowKey, type);
		return  tableClient.execute(tableName, retrieveEntity).getResultAsType();
	}

}
