package cat.i2cat.mcaslite.cloud;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;
import cat.i2cat.mcaslite.management.ProcessQueue;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.XMLReader;

public class CloudManager implements Runnable, Cancellable {
	
	private static final CloudManager INSTANCE = new CloudManager();
	
	private boolean cancelled = false;
	private ProcessQueue queue;

	private int pollInterval = 10;
	private int pollFactor = 2;

	private String videoQueue = XMLReader.getXMLParameter("config/config.xml", "cloud.processqueue");
	private String cancelQueue = XMLReader.getXMLParameter("config/config.xml", "cloud.cancelqueue");
	
	private Map<String, CloudQueueMessage> messages = new ConcurrentHashMap<String, CloudQueueMessage>();

	private CloudManager(){
		queue = ProcessQueue.getInstance();
	}
	
	public static CloudManager getInstance(){
		return INSTANCE;
	}

	@Override
	public void run() {
		while(! cancelled){
			try {
				if (queue.hasSlot()){
					processMessage(AzureUtils.retrieveMessage(pollFactor*pollInterval, videoQueue));
				}
				processCancelMessage(AzureUtils.peekMessage(cancelQueue));
				Thread.sleep(pollInterval*1000);
				updateStatus();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MCASException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processCancelMessage(CloudQueueMessage msg) throws MCASException {
		if (msg != null){
			try {
				String id = msg.getMessageContentAsString();
				if (messages.containsKey(id)){
					if (TranscoHandler.getInstance().cancelRequest(
						TRequest.getEqualRequest(id), true)){
						//AzureUtils.
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				throw new MCASException();
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
		if (msg != null){
			try {
				String[] keys = msg.getMessageContentAsString().split("\\*");
				VideoEntity video = AzureUtils.getEntity(keys[0], keys[1], VideoEntity.class.getSimpleName(), VideoEntity.class);
				if (TranscoHandler.getInstance().putRequest(video.toRequest())){
					messages.put(video.toRequest().getId(), msg);
				}
			} catch (Exception e){
				e.printStackTrace();
				throw new MCASException();
			}
		}
	}
	
	private void updateStatus() throws MCASException {
		Iterator<String> it = messages.keySet().iterator();
		while(it.hasNext()){
			String id = it.next();
			if (TranscoHandler.getInstance().getStatus(id).hasNext()){
				AzureUtils.updateMessage(messages.get(id), false, pollFactor*pollInterval, videoQueue) ;
			}
		}
	}
	
	public CloudQueueMessage getCloudMessage(String requestId) throws MCASException{
		if (messages.containsKey(requestId)) {
			return messages.get(requestId);
		}
		throw new MCASException();
	}
	
	public CloudQueueMessage popCloudMessage(String requestId) throws MCASException{
		if (messages.containsKey(requestId)) {
			return messages.remove(requestId);
		}
		throw new MCASException();
	}
}