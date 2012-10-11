package cat.i2cat.mcaslite.management;

import java.util.UUID;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.DefaultsUtils;

public class TranscoHandler implements Runnable {

	private static final int MAX_REQUESTS = 1000;
	
	private TranscoQueue queue;
	private int maxInMedia;
	private int maxOutMedia;
	private int maxTransco;
	private DAO<ApplicationConfig> applicationDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
	
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
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadDefaults() throws MCASException{
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
