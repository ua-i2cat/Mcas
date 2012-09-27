package cat.i2cat.mcaslite.management;

import java.util.UUID;

import cat.i2cat.mcaslite.entities.ApplicationConfig;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.entities.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoHandler implements Runnable {

	private TranscoQueue queue;
	private int MAX_MEDIAH = ApplicationConfig.getMaxMediaH();
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
					if ((queue.isEmpty(State.M_QUEUED) && queue.isEmpty(State.T_QUEUED)) ||
							(queue.isEmpty(State.M_QUEUED) && queue.count(State.T_PROCESS) >= MAX_TRANSCO) ||
							(queue.isEmpty(State.T_QUEUED) && queue.count(State.M_PROCESS) >= MAX_MEDIAH)){
						queue.wait();
					}
				}
				request = queue.get(State.M_QUEUED);
				if (request != null && queue.count(State.M_PROCESS) < MAX_MEDIAH){
					increaseRequestState(request);
					MediaHandler mediaH = new MediaHandler(queue, request);
					Thread mediaTh = new Thread(mediaH);
					mediaTh.setDaemon(true);
					mediaTh.start();
				}
				request = queue.get(State.T_QUEUED);
				if (request != null && queue.count(State.T_PROCESS) < MAX_TRANSCO){
					increaseRequestState(request);
//					Transcoder transcoder = new Transcoder(queue, request);
//					Thread transcoTh = new Thread(transcoder);
//					transcoTh.setDaemon(true);
//					transcoTh.start();
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
		queue.update(request);
		queue.notifyAll();
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
	
}
