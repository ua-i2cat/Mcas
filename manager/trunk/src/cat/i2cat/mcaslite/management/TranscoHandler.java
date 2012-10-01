package cat.i2cat.mcaslite.management;

import java.util.UUID;

import cat.i2cat.mcaslite.entities.ApplicationConfig;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.entities.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoHandler implements Runnable {

	private TranscoQueue queue;
	private int MAX_IN_MEDIAH = ApplicationConfig.getMaxInMediaH();
	private int MAX_OUT_MEDIAH = ApplicationConfig.getMaxOutMediaH();
	private int MAX_TRANSCO = ApplicationConfig.getMaxTransco();
	
	public TranscoHandler(){
		queue = TranscoQueue.getInstance();
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
				if (request != null && queue.count(State.M_PROCESS) < MAX_IN_MEDIAH){
					mediaHandle(request);
				}
				request = queue.get(State.T_QUEUED);
				if (request != null && queue.count(State.T_PROCESS) < MAX_TRANSCO){
					transcode(request);
				}
				request = queue.get(State.T_TRANSCODED);
				if (request != null && queue.count(State.T_TRANSCODED) < MAX_OUT_MEDIAH){
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
	
	public String getState(TranscoRequest r) {
		State state = queue.getState(r);
		if (state != null){
			return state.getName();
		} else {
			return null;
		}
	}
	
	public String getState(UUID id){
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
				(queue.isEmpty(State.T_TRANSCODED) && queue.isEmpty(State.T_QUEUED) && queue.count(State.M_PROCESS) >= MAX_IN_MEDIAH) ||
				(queue.isEmpty(State.T_TRANSCODED) && queue.count(State.T_PROCESS) >= MAX_TRANSCO && queue.isEmpty(State.M_QUEUED)) ||
				(queue.isEmpty(State.T_TRANSCODED) && queue.count(State.T_PROCESS) >= MAX_TRANSCO && queue.count(State.M_PROCESS) >= MAX_IN_MEDIAH) ||
				(queue.count(State.T_TRANSCODED) >= MAX_OUT_MEDIAH && queue.isEmpty(State.T_QUEUED) && queue.isEmpty(State.M_QUEUED)) ||
				(queue.count(State.T_TRANSCODED) >= MAX_OUT_MEDIAH && queue.isEmpty(State.T_QUEUED) && queue.count(State.M_PROCESS) >= MAX_IN_MEDIAH) ||
				(queue.count(State.T_TRANSCODED) >= MAX_OUT_MEDIAH && queue.count(State.T_PROCESS) >= MAX_TRANSCO && queue.isEmpty(State.M_QUEUED)) ||
				(queue.count(State.T_TRANSCODED) >= MAX_OUT_MEDIAH && queue.count(State.T_PROCESS) >= MAX_TRANSCO && queue.count(State.M_PROCESS) >= MAX_IN_MEDIAH)){
			queue.wait();
		}
	}
}
