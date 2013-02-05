package cat.i2cat.mcaslite.cloud;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;
import cat.i2cat.mcaslite.management.ProcessQueue;
import cat.i2cat.mcaslite.management.TranscoHandler;

public class CloudManager implements Runnable, Cancellable {
	
	private static final CloudManager INSTANCE = new CloudManager();
	
	private boolean cancelled = false;
	private ProcessQueue queue;
	//TODO: configuration
	private int pollInterval = 30;
	private int pollFactor = 2;
	// End of configuration
	private Map<String, CloudQueueMessage> messages = new ConcurrentHashMap<String, CloudQueueMessage>();

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
					processMessage(AzureUtils.retrieveMessage(pollFactor*pollInterval));
				}
				updateStatus();
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
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
	
	private void processMessage(CloudQueueMessage msg) throws MCASException{
		try {
			String[] keys = msg.getMessageContentAsString().split("\\*");
			VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
			if (TranscoHandler.getInstance().putRequest(video.videoEntityToTrequest())){
				messages.put(video.videoEntityToTrequest().getId(), msg);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void updateStatus() throws MCASException {
		Iterator<String> it = messages.keySet().iterator();
		while(it.hasNext()){
			String id = it.next();
			if (TranscoHandler.getInstance().getStatus(id).hasNext()){
				AzureUtils.updateMessage(messages.get(id), false, pollFactor*pollInterval) ;
			}
		}
	}
	
	public CloudQueueMessage getCloudMessage(String requestId) throws MCASException{
		if (messages.containsKey(requestId)) {
			return messages.get(requestId);
		}
		throw new MCASException();
	}
}
