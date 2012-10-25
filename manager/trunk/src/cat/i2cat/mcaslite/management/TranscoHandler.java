package cat.i2cat.mcaslite.management;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class TranscoHandler implements Runnable {

	private static final int MAX_REQUESTS = 1000;
	
	private TranscoQueue queue;
	private int maxInMedia;
	private int maxOutMedia;
	private int maxTransco;
	private DAO<ApplicationConfig> applicationDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
	private Stack<SimpleEntry<String, Cancellable>> workers = new Stack<SimpleEntry<String, Cancellable>>();
	
	private boolean MQBlock = false;
	private boolean TQBlock = false;
	private boolean TTBlock = false;
	
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
		while(true){
			try {
				synchronized(queue){
					waitCondition();
				}
				if (! MQBlock){
					request = queue.get(State.M_QUEUED);
					mediaHandle(request);
				} 
				if (! TQBlock) {
					request = queue.get(State.T_QUEUED);
					transcode(request);
				} 
				if (! TTBlock) {
					request = queue.get(State.T_TRANSCODED);
					mediaHandle(request);
				} 
			} catch (MCASException e) {
				if (request != null && request.getState().equals(State.CANCELLED)){
					synchronized(queue){
						if (queue.removeRequest(request)){
							requestDao.save(request);
							queue.notifyAll();
						}
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
			maxInMedia = config.getMaxInMediaH();
			maxOutMedia = config.getMaxOutMediaH();
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
				if (request.getState().equals(State.M_PROCESS) || 
					request.getState().equals(State.T_PROCESS) ||
					request.getState().equals(State.MOVING)) {
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
	
	public TranscoRequest getRequest(UUID id) throws MCASException {
		return getRequest(TranscoRequest.getEqualRequest(id));
	}
	
	public String getState(TranscoRequest r) throws MCASException {
		State state = queue.getState(r);
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
		if(conditionMQ() & conditionTQ() & conditionTT()){
			queue.wait();
			waitCondition();
		}
	}
	
	private boolean conditionMQ() throws MCASException {
		MQBlock = queue.isEmpty(State.M_QUEUED) || queue.count(State.M_PROCESS) >= maxInMedia || queue.count(State.T_QUEUED) >= maxInMedia;
		return MQBlock;
	}
	
	private boolean conditionTQ() throws MCASException {
		TQBlock = queue.isEmpty(State.T_QUEUED) || queue.count(State.T_PROCESS) >= maxTransco || queue.count(State.T_TRANSCODED) >= maxOutMedia;
		return TQBlock;
	}
	
	private boolean conditionTT() throws MCASException {
		TTBlock = queue.isEmpty(State.T_TRANSCODED) || queue.count(State.MOVING) >= maxOutMedia;
		return TTBlock;
	}
}
