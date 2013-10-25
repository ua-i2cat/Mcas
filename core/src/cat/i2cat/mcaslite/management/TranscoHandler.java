package cat.i2cat.mcaslite.management;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.RequestUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;
import cat.i2cat.mcaslite.utils.XMLReader;


public class TranscoHandler implements Runnable {

	private static final TranscoHandler INSTANCE = new TranscoHandler();
	
	private int maxRequests;
	
	private ProcessQueue queue;
	private DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
	private List<SimpleEntry<String, Cancellable>> workers = new ArrayList<SimpleEntry<String, Cancellable>>();
	private final Semaphore semaphore;
	
	private boolean run = true;
	
	private TranscoHandler() {
		String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
		maxRequests = XMLReader.getIntParameter(path, "maxreq");
		queue = ProcessQueue.getInstance();
		queue.setMaxProcess(XMLReader.getIntParameter(path, "maxproc"));
		this.semaphore = new Semaphore(XMLReader.getIntParameter(path, "maxproc"), true);
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
	
	public boolean cancelRequest(TRequest request, boolean mayInterruptIfRunning) {
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

	public boolean putRequest(TRequest request) throws MCASException {
		if (queue.size() < maxRequests) {
			request.initRequest();
			request.increaseStatus(true);
			queue.put(request);
			return true;
		} else {
			return false;
		}
	}
	
	public void stop(){
		run = false;
	}
	
	public TRequest getRequest(String id) throws MCASException {
		TRequest request = queue.getProcessObject(TRequest.getEqualRequest(id));
		if (request != null){
			return request;
		} else {
			return requestDao.findById(id);
		}
	}
	
	public Status getStatus(String id) throws MCASException {
		return getRequest(id).getStatus();
	}
	
	private void transcode(TRequest request) throws MCASException{
		try {
			request.setTranscos(TranscoderUtils.transcoBuilder(request.getTConfig(), request.getId(), 
					new URI(request.getSrc()), request.getTitle()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
		request.increaseStatus(true);
		for (int i = 0; i < request.getTranscos().size() ; i++){
			try {
				semaphore.acquire();
				Transcoder transcoder = new Transcoder(request, i, semaphore, new Semaphore(1,true));
				(new Thread(transcoder)).start();
				addWorkerInStack(transcoder, request.getId());
			} catch (MCASException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	private boolean cancelWorker(String id, boolean mayInterruptIfRunning) throws InterruptedException, ExecutionException{
		Iterator<SimpleEntry<String, Cancellable>> it = workers.iterator();
		boolean cancel = true;
		boolean found = false;
		while(it.hasNext()){
			SimpleEntry<String, Cancellable> worker = it.next();
			if (worker.getKey().equals(id)) {
				found = true;
				if (worker.getValue().cancel(mayInterruptIfRunning)) {
					it.remove();
					cancel = cancel & true;
				} else {
					cancel = cancel & false;
				}
			} 
		}
		return cancel & found;
	}
	
	private void addWorkerInStack(Cancellable thread, String id){
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
	
	public String requestStatus(TRequest request) throws MCASException{
		return RequestUtils.requestToJSON(request);
	}
}
