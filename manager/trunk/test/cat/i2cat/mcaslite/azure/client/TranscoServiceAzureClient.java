package cat.i2cat.mcaslite.azure.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import cat.i2cat.mcaslite.cloud.VideoEntity;
import cat.i2cat.mcaslite.utils.XMLReader;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;

public class TranscoServiceAzureClient {
	
	private static final String path = "config/config.xml";
	private static final String storageConnectionString = 
		    "DefaultEndpointsProtocol=" + XMLReader.getStringParameter(path, "cloud.connection.protocol") + ";" + 
	   	    "AccountName=" + XMLReader.getStringParameter(path, "cloud.connection.accountName") + ";" + 
	  	    "AccountKey=" + XMLReader.getStringParameter(path, "cloud.connection.accountKey");
	
	public static void UploadContent() {
		try{
			CloudStorageAccount storageAccount = 
			CloudStorageAccount.parse(storageConnectionString);
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("videoentity"); //review container name
			container.createIfNotExist();
			
			System.out.println("Write a Source to transcode:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String src = br.readLine();
			CloudBlockBlob blob = container.getBlockBlobReference("mediafile");
			File source = new File(src);
			blob.upload(new FileInputStream(source), source.length());
			String blobUrl = blob.getUri().toString();
			
			
			
			VideoEntity ventity = new VideoEntity((new Date()).toString(), UUID.randomUUID().toString());
			ventity.setFileName("video.avi");
			ventity.setDescription("");
			ventity.setCategory("");
			ventity.setTitle("");
			ventity.setVideoUploadedUrl(blobUrl); 
			ventity.setStatus("");
			ventity.setCancelId("");
			ventity.setTenantContainer(container.getName());
			
			TableOperation insertVideoEntity = TableOperation.insert(ventity);
			tableClient.execute(VideoEntity.class.getSimpleName(), insertVideoEntity);
			//tableClient.execute(tableName, insertVideoEntity);
			
			System.out.println("Processing complete.");
			System.exit(0);
			
		} catch (FileNotFoundException fileNotFoundException)
		{
		    System.out.print("FileNotFoundException encountered: ");
		    System.out.println(fileNotFoundException.getMessage());
		    System.exit(-1);
		}
		catch (StorageException storageException)
		{
		    System.out.print("StorageException encountered: ");
		    System.out.println(storageException.getMessage());
		    System.exit(-1);
		}
		catch (URISyntaxException uriSyntaxException)
		{
		    System.out.print("URISyntaxException encountered: ");
		    System.out.println(uriSyntaxException.getMessage());
		    System.exit(-1);
		}
		catch (Exception e)
		{
		    System.out.print("Exception encountered: ");
		    System.out.println(e.getMessage());
		    System.exit(-1);
		}
	}
	
	public static String ListTheBlobs()
	{
		try	{
			CloudStorageAccount storageAccount =
				    CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("videoentity");
			for (ListBlobItem blobItem : container.listBlobs()){
				System.out.println(blobItem.getUri());
			} System.exit(0);
		} catch (Exception e){
			System.exit(-1);
			}
		return null;
	}
	
	public static void main (String...args){
		UploadContent();
		//ListTheBlobs();
	}	
}