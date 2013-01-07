package cat.i2cat.mcaslite.management;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoStatus;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class TranscoHandler implements Runnable {

	private static final int MAX_REQUESTS = 1000;
	
	private TranscoQueue queue;
	private int maxTransco;
	private DAO<ApplicationConfig> applicationDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
	private Stack<SimpleEntry<String, Cancellable>> workers = new Stack<SimpleEntry<String, Cancellable>>();
	
	private boolean MQBlock = false;
	private boolean TQBlock = false;
	private boolean TTBlock = false;
	
	private boolean run = true;
	
	public TranscoHandler() throws MCASException{
		queue = TranscoQueue.getInstance();
		if (DefaultsUtils.feedDefaultsNeeded()){
			DefaultsUtils.applicationFeedDefaults();
			DefaultsUtils.tConfigFeedDefaults();
		}
		loadDefaults();
	}
	
	@Override
	public void run() {
		TranscoRequest request = null;
		while(run){
			try {
				synchronized(queue){
					waitCondition();
				}
//				if (! MQBlock){
//					request = queue.get(State.M_QUEUED);
//					mediaHandle(request);
//				} 
				if (! TQBlock) {
					request = queue.get(Status.T_QUEUED);
					transcode(request);
				} 
//				if (! TTBlock) {
//					request = queue.get(State.T_TRANSCODED);
//					mediaHandle(request);
//				} 
			} catch (MCASException e) {
				if (request != null && ! request.getState().equals(Status.CANCELLED)){
					request.setError();
				}
				synchronized(queue){
					if (queue.removeRequest(request)){
						requestDao.save(request);
						queue.notifyAll();
					}
				}
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadDefaults() throws MCASException {
		ApplicationConfig config = applicationDao.findByName(DefaultsUtils.DEFAULT);
		maxInMedia = config.getMaxInMediaH();
		maxOutMedia = config.getMaxOutMediaH();
		maxTransco = config.getMaxTransco();
	}
	
	public void loadConfig(Integer id) throws MCASException {
		ApplicationConfig config;
		try {
			config = applicationDao.findById(id);
			maxTransco = config.getMaxTransco();
		} catch (MCASException e) {
			loadDefaults();
			e.printStackTrace();
		}
	}
	
	public boolean cancelRequest(TranscoRequest request, boolean mayInterruptIfRunning) {
		synchronized(queue){
			request = queue.getRequest(request);
			if (request != null){
				if (request.getState().equals(Status.M_PROCESS) || 
					request.getState().equals(Status.T_PROCESS) ||
					request.getState().equals(Status.MOVING)) {
					try {
						if (! cancelWorker(request.getIdStr(), mayInterruptIfRunning)){
							MediaUtils.clean(request);
						}
					} catch (Exception e) {
						return false;
					}
				} else {
					MediaUtils.clean(request);
				}
				request.setCancelled();
				if (queue.removeRequest(request)) {
					requestDao.save(request);
					queue.notifyAll();
				}
				return true;
			}
		}
		return false;
	}

	public boolean putRequest(TranscoRequest request) throws MCASException {
		if (queue.size() < MAX_REQUESTS) {
			request.initTConf();
			request.increaseState();
			synchronized(queue){
				queue.put(request);
				queue.notifyAll();
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void increaseRequestState(TranscoRequest request) throws MCASException {
		request.increaseState();
		synchronized(queue){
			queue.update(request);
			queue.notifyAll();
		}
	}
	
	public TranscoRequest getRequest(TranscoRequest r) throws MCASException {
		TranscoRequest request = queue.getRequest(r);
		if (request == null)
		{
			request = requestDao.findById(r.getId());
			if (request == null){
				return null;
			} else {
				return request;
			}
		} else {
			return request;
		}
		
	}
	
	public void stop(){
		run = false;
	}
	
	public TranscoRequest getRequest(UUID id) throws MCASException {
		return getRequest(TranscoRequest.getEqualRequest(id));
	}
	
	public String getState(TranscoRequest r) throws MCASException {
		Status state = queue.getState(r);
		if (state != null){
			return state.getName();
		} else {
			TranscoRequest req = requestDao.findById(r.getId());
			if (req == null){
				return null;
			} else {
				return req.getState().getName();
			}
		}
	}
	
	public String getState(UUID id) throws MCASException{
		return getState(TranscoRequest.getEqualRequest(id));
	}
	
	private void mediaHandle(TranscoRequest request) throws MCASException{
		increaseRequestState(request);
		MediaHandler mediaTh = new MediaHandler(queue, request);
		(new Thread(mediaTh)).start();
		addWorkerInStack(mediaTh, request.getIdStr());
	}
	
	private void transcode(TranscoRequest request) throws MCASException{
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
				return worker.getValue().cancel(mayInterruptIfRunning);
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
		workers.push(new SimpleEntry<String, Cancellable>(id, thread));
		cleanWorkers();
	}
	
	private void cleanWorkers(){
		if (workers.size() >= maxInMedia + maxOutMedia + maxTransco){
			workers.pop();
		}
	}
	
	private void waitCondition() throws MCASException, InterruptedException{
		if(queue.isEmpty() || queue.count(Status.T_PROCESS) >= maxTransco){
			queue.wait();
			waitCondition();
		}
	}
	
	private boolean conditionMQ() throws MCASException {
		MQBlock = queue.isEmpty(Status.M_QUEUED) || queue.count(Status.M_PROCESS) >= maxInMedia || queue.count(Status.T_QUEUED) >= maxInMedia;
		return MQBlock;
	}
	
	private boolean conditionTQ() throws MCASException {
		TQBlock = queue.isEmpty(Status.T_QUEUED) || queue.count(Status.T_PROCESS) >= maxTransco || queue.count(Status.T_TRANSCODED) >= maxOutMedia;
		return TQBlock;
	}
	
	private boolean conditionTT() throws MCASException {
		TTBlock = queue.isEmpty(Status.T_TRANSCODED) || queue.count(Status.MOVING) >= maxOutMedia;
		return TTBlock;
	}
}
