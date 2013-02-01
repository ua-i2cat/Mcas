package cat.i2cat.mcaslite.management;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.VideoEntity;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.AzureUtils;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;


public class CloudManager implements Runnable, Cancellable {
	
	private static final CloudManager INSTANCE = new CloudManager();
	
	private boolean cancelled = false;
	private ProcessQueue queue;
	private int pollInterval;
	private String storageConnectionString;
	private String cloudQueue;
	private Map<UUID, CloudQueueMessage> messages = new HashMap<UUID, CloudQueueMessage>();

	private CloudManager(){
		queue = ProcessQueue.getInstance();
		//TODO: get connectionString
		//TODO: get pollInterval
		//TODO
	}
	
	public static CloudManager getInstance(){
		return INSTANCE;
	}

	@Override
	public void run() {
		while(! cancelled){
			try {
				if (queue.hasSlot()){
					processMessage(AzureUtils.retrieveMessage(storageConnectionString, cloudQueue, pollInterval));
				}
				updateMessages();
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MCASException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancelled = true;
		return cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return cancelled;
	}
	
	private void processMessage(CloudQueueMessage msg) throws StorageException, InvalidKeyException, URISyntaxException, MCASException{
		String[] keys = msg.getMessageContentAsString().split("*");
		VideoEntity video = (VideoEntity) AzureUtils.getEntity(storageConnectionString, keys[0], keys[1], VideoEntity.class.getName(), VideoEntity.class);
		TRequest request = video.videoEntityToTrequest();
		if (TranscoHandler.getInstance().putRequest(request)){
			messages.put(request.getId(), msg);
		} else {
			//TODO: throw an Exception, update message or nothing?
		}
	}
	
	private void updateMessages() throws MCASException{
		Iterator<UUID> it = messages.keySet().iterator();
		while(it.hasNext()){
			UUID id = it.next();
			if (TranscoHandler.getInstance().getStatus(id).hasNext())
			{
				//TODO: updateMessage
			} else {
				//TODO: update Storage
			}
		}
	}
}
