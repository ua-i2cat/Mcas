package cat.i2cat.mcas.cloud;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Cancellable;
import cat.i2cat.mcaslite.management.ProcessQueue;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.XMLReader;

import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;

public class CloudManager implements Runnable, Cancellable {
	
	private static final CloudManager INSTANCE = new CloudManager();
	
	private boolean cancelled = false;
	private ProcessQueue queue;
	
	private String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
	
	private int pollInterval = XMLReader.getIntParameter(path, "cloud.pollInterval");
	private int pollFactor = XMLReader.getIntParameter(path, "cloud.pollFactor");

	private String videoQueue = XMLReader.getStringParameter(path, "cloud.processqueue");
	private String cancelQueue = XMLReader.getStringParameter(path, "cloud.cancelqueue");
	
	private int cancelTryout = XMLReader.getIntParameter(path, "cloud.cancelTryout");
	
	private Map<String, CloudQueueMessage> messages; 
	private String cancelId = "";
	private int cancelRetry;
	

	private CloudManager(){
		queue = ProcessQueue.getInstance();
		messages = new ConcurrentHashMap<String, CloudQueueMessage>();
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
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	private void processCancelMessage(CloudQueueMessage msg) throws MCASException {
		if (msg != null){
			try {
				if (msg.getMessageContentAsString() != null && ! msg.getMessageContentAsString().equals(cancelId)) {
					cancelId =  msg.getMessageContentAsString();
					cancelRetry = 0;
				}
				if (messages.containsKey(cancelId)){
					TranscoHandler.getInstance().cancelRequest(TRequest.getEqualRequest(cancelId), true);
					msg = AzureUtils.retrieveMessage(pollInterval, cancelQueue);
					AzureUtils.deleteQueueMessage(msg, cancelQueue);
				} else {
					if (cancelRetry++ >= cancelTryout){
						cancelRetry = 0;
						cancelId = "";
						msg = AzureUtils.retrieveMessage(pollInterval, cancelQueue);
						AzureUtils.deleteQueueMessage(msg, cancelQueue);
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
				TRequest request = video.toRequest();
				if (TranscoHandler.getInstance().putRequest(request)){
					messages.put(request.getId(), msg);
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
			Status status = TranscoHandler.getInstance().getStatus(id);
			if (status != null && status.hasNext()){
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
	
	public Map<String, CloudQueueMessage> getMessages(){
		return messages;
	}
}
