package cat.i2cat.mcaslite.management;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;

public class TranscoHandler implements Runnable {

	private static final int MAX_REQUESTS = 1000;
	
	private ProcessQueue queue;
	private DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
	private List<SimpleEntry<String, Cancellable>> workers = new ArrayList<SimpleEntry<String, Cancellable>>();
	
	private boolean run = true;
	
	public TranscoHandler() throws MCASException{
		queue = ProcessQueue.getInstance();
		queue.setMaxProcess(DefaultsUtils.MAX_PROCESS);
		if (DefaultsUtils.feedDefaultsNeeded()){
			DefaultsUtils.tConfigFeedDefaults();
		}
	}
	
	@Override
	public void run() {
		TRequest request = null;
		while(run){
			try {
				request = queue.getAndProcess();
				transcode(request);
			} catch (MCASException e) {
				if (request != null && ! request.getStatus().equals(Status.CANCELLED)){
					request.setError();
				}
				if (queue.remove(request)){
						requestDao.save(request);
				}
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean cancelRequest(TRequest request, boolean mayInterruptIfRunning) {
		synchronized(queue){
			request = queue.getProcessObject(request);
			if (request != null && (request.isProcessing() || request.isWaiting())){
				try {
					if (request.isProcessing() && ! cancelWorker(request.getIdStr(), mayInterruptIfRunning)){
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				request.setCancelled();
				if (queue.remove(request)) {
					requestDao.save(request);
				}
				return true;
			}
			return false;
		}
	}

	public boolean putRequest(TRequest request) throws MCASException {
		if (queue.size() < MAX_REQUESTS) {
			request.increaseStatus();
			queue.put(request);
			return true;
		} else {
			return false;
		}
	}
	
	private void increaseRequestState(TRequest request) throws MCASException {
		request.increaseStatus();
		queue.update(request);
	}
	
	public void stop(){
		run = false;
	}
	
	public TRequest getRequest(UUID id) throws MCASException {
		TRequest request = queue.getProcessObject(TRequest.getEqualRequest(id));
		if (request != null){
			return request;
		} else {
			request = requestDao.findById(id);
			if (request != null){
				return request;
			} else {
				throw new MCASException();
			}
		}
	}
	
	public Status getStatus(UUID id) throws MCASException {
		return getRequest(id).getStatus();
	}
	
//	private void mediaHandle(ProcessObject request) throws MCASException{
//		increaseRequestState(request);
//		MediaHandler mediaTh = new MediaHandler(queue, request);
//		(new Thread(mediaTh)).start();
//		addWorkerInStack(mediaTh, request.getIdStr());
//	}

	private void transcode(TRequest request) throws MCASException{
		increaseRequestState(request);
		Transcoder transTh = new Transcoder(queue, request);
		(new Thread(transTh)).start();
		addWorkerInStack(transTh, request.getIdStr());
	}
	
	private boolean cancelWorker(String id, boolean mayInterruptIfRunning) throws InterruptedException, ExecutionException{
		Iterator<SimpleEntry<String, Cancellable>> it = workers.iterator();
		while(it.hasNext()){
			SimpleEntry<String, Cancellable> worker = it.next();
			if (worker.getKey().equals(id)) {
				if (worker.getValue().cancel(mayInterruptIfRunning)) {
					it.remove();
					return true;
				} else {
					return false;
				}
			} 
		}
		return false;
	}
	
	private void addWorkerInStack(Cancellable thread, String id){
		Iterator<SimpleEntry<String, Cancellable>> it = workers.iterator();
		while(it.hasNext()){
			SimpleEntry<String, Cancellable> worker = it.next();
			if (worker.getKey().equals(id)) {
				worker.setValue(thread);
				cleanWorkers();
				return;
			} 
		}
		workers.add(new SimpleEntry<String, Cancellable>(id, thread));
		cleanWorkers();
	}
	
	private void cleanWorkers(){
		if (workers.size() > queue.getMaxProcess()){
			workers.remove(0);
		}
	}
}
