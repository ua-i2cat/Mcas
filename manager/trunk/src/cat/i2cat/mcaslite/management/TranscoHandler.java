package cat.i2cat.mcaslite.management;

import java.util.UUID;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoHandler implements Runnable {

	private static final int DEFAULT_CONFIG_ID = 1;
	
	private TranscoQueue queue;
	private int maxInMedia;
	private int maxOutMedia;
	private int maxTransco;
	private DAO<ApplicationConfig> applicationDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
	
	public TranscoHandler() throws MCASException{
		queue = TranscoQueue.getInstance();
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
				request = queue.get(State.M_QUEUED);
				if (request != null && queue.count(State.M_PROCESS) < maxInMedia){
					mediaHandle(request);
				}
				request = queue.get(State.T_QUEUED);
				if (request != null && queue.count(State.T_PROCESS) < maxTransco){
					transcode(request);
				}
				request = queue.get(State.T_TRANSCODED);
				if (request != null && queue.count(State.T_TRANSCODED) < maxOutMedia){
					mediaHandle(request);
				}
			} catch (MCASException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void loadDefaults() throws MCASException{
		ApplicationConfig config = applicationDao.findById(DEFAULT_CONFIG_ID);
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

	public void putRequest(TranscoRequest request) throws MCASException {
		request.increaseState();
		synchronized(queue){
			queue.put(request);
			queue.notifyAll();
		}
	}
	
	public void increaseRequestState(TranscoRequest request) throws MCASException {
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
		MediaHandler mediaH = new MediaHandler(queue, request);
		Thread mediaTh = new Thread(mediaH);
		mediaTh.setDaemon(true);
		mediaTh.start();
	}
	
	private void transcode(TranscoRequest request) throws MCASException{
		increaseRequestState(request);
		Transcoder transcoder = new Transcoder(queue, request);
		Thread transcoTh = new Thread(transcoder);
		transcoTh.setDaemon(true);
		transcoTh.start();
	}
	
	private void waitCondition() throws MCASException, InterruptedException{
		if ((queue.isEmpty(State.T_TRANSCODED) && queue.isEmpty(State.T_QUEUED) && queue.isEmpty(State.M_QUEUED)) ||
				(queue.isEmpty(State.T_TRANSCODED) && queue.isEmpty(State.T_QUEUED) && queue.count(State.M_PROCESS) >= maxInMedia) ||
				(queue.isEmpty(State.T_TRANSCODED) && queue.count(State.T_PROCESS) >= maxTransco && queue.isEmpty(State.M_QUEUED)) ||
				(queue.isEmpty(State.T_TRANSCODED) && queue.count(State.T_PROCESS) >= maxTransco && queue.count(State.M_PROCESS) >= maxInMedia) ||
				(queue.count(State.T_TRANSCODED) >= maxOutMedia && queue.isEmpty(State.T_QUEUED) && queue.isEmpty(State.M_QUEUED)) ||
				(queue.count(State.T_TRANSCODED) >= maxOutMedia && queue.isEmpty(State.T_QUEUED) && queue.count(State.M_PROCESS) >= maxInMedia) ||
				(queue.count(State.T_TRANSCODED) >= maxOutMedia && queue.count(State.T_PROCESS) >= maxTransco && queue.isEmpty(State.M_QUEUED)) ||
				(queue.count(State.T_TRANSCODED) >= maxOutMedia && queue.count(State.T_PROCESS) >= maxTransco && queue.count(State.M_PROCESS) >= maxInMedia)){
			queue.wait();
		}
	}
}
