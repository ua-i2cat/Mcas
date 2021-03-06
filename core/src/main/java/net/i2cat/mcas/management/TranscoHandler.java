package net.i2cat.mcas.management;

import java.io.File;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TLevel;
import net.i2cat.mcas.config.model.TProfile;
import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.utils.XMLReader;


public class TranscoHandler implements Runnable {

	private static final TranscoHandler INSTANCE = new TranscoHandler();
	
	private int maxRequests;
	
	private ProcessQueue queue;
	private DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
	private List<SimpleEntry<String, Cancellable>> workers = new ArrayList<SimpleEntry<String, Cancellable>>();
	
	private boolean run = true;
	
	private TranscoHandler() {
		String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
		maxRequests = XMLReader.getIntParameter(path, "maxreq");
		queue = ProcessQueue.getInstance();
		queue.setMaxProcess(XMLReader.getIntParameter(path, "maxproc"));
	}
	
	public static TranscoHandler getInstance(){
		return INSTANCE;
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
	
	public synchronized boolean cancelRequest(TRequest request, boolean mayInterruptIfRunning) {
		synchronized(queue){//TODO: is this needed?
			request = queue.getProcessObject(request);
			if (request != null && (request.isProcessing() || request.isWaiting())){
				try {
					if (request.isProcessing() && ! cancelWorker(request.getId(), mayInterruptIfRunning)){
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				request.setCancelled();
				if (queue.remove(request)) {
					requestDao.save(request);
					return true;
				}
			}
			return false;
		}
	}

	public synchronized boolean putRequest(TRequest request) throws MCASException {
		if (queue.size() < maxRequests) {
			request.initRequest();
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
	
	public synchronized TRequest getRequest(String id) throws MCASException {
		TRequest request = queue.getProcessObject(TRequest.getEqualRequest(id));
		if (request != null){
			return request;
		} else {
			return requestDao.findById(id);
		}
	}
	
	public synchronized Status getStatus(String id) throws MCASException {
		return getRequest(id).getStatus();
	}
	
	private void transcode(TRequest request) throws MCASException{
		increaseRequestState(request);
		Transcoder transTh = new Transcoder(queue, request);
		(new Thread(transTh)).start();
		addWorkerInStack(transTh, request.getId());
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

	public List<String> getProfiles() throws MCASException {
		DAO<TProfile> profileDao = new DAO<TProfile>(TProfile.class);
		List<String> profiles = new ArrayList<String>();
		for (TProfile profile : profileDao.listAll()){
			profiles.add(profile.getName());
		}
		return profiles;
	}

	public List<String> getLevels() throws MCASException {
		DAO<TLevel> levelDao = new DAO<TLevel>(TLevel.class);
		List<String> levels = new ArrayList<String>();
		for (TLevel level : levelDao.listAll()){
			levels.add(level.getName());
		}
		return levels;
	}
}
