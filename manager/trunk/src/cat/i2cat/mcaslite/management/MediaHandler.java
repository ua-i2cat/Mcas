package cat.i2cat.mcaslite.management;

import java.net.URI;
import java.net.URISyntaxException;

import cat.i2cat.mcaslite.entities.ApplicationConfig;
import cat.i2cat.mcaslite.entities.Transco;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class MediaHandler implements Runnable {

	private TranscoQueue queue;
	private TranscoRequest request;
	
	public MediaHandler(TranscoQueue queue, TranscoRequest request){
		this.queue = queue;
		this.request = request;
	}
	
	@Override
	public void run() {
		try {
			switch (request.getState()) {
				case M_PROCESS:
					inputHandle();
					break;
				case MOVING:
					outputHandle();
					break;
				default: 
					throw new MCASException();
			}
		} catch (MCASException e) {
			request.setError();
			queue.update(request);
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void inputHandle() throws MCASException, URISyntaxException{
		MediaUtils.toWorkingDir(new URI(request.getSrc()), request.getIdStr());
		request.increaseState();
		synchronized(queue){
			queue.update(request);
			queue.notifyAll();
		}
	}
	
	private void outputHandle() throws MCASException {
		for(Transco transco : request.getTranscoded()){
			try {
				MediaUtils.toDestinationUri(transco.getOutputFile(), transco.getDestinationUri());
			} catch (MCASException e) {
				e.printStackTrace();
				request.deleteTranscoded(transco);
				MediaUtils.deleteFile(transco.getOutputFile());
			}
		}
		if (request.getNumOutputs() > request.getTranscoded().size()){
			if (request.isTranscodedEmpty()){
				MediaUtils.deleteFile(ApplicationConfig.getInputWorkingDir() + request.getIdString());
				request.setError();
			} else {
				request.setPartialError();
			}
		} else {
			request.increaseState();
		}
		MediaUtils.clean(request.getTranscoded());
		synchronized(queue){
			queue.update(request);
			queue.notifyAll();
		}
	}
	
	public void inputHandleTest() throws MCASException, URISyntaxException {
		inputHandle();
	}
	
	public void outputHandleTest() throws MCASException, URISyntaxException {
		outputHandle();
	}
}
