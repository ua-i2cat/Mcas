package cat.i2cat.mcaslite.management;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

public class MediaHandler implements Runnable {

	private TranscoQueue queue;
	private TranscoRequest request;
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class); 
	
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
			synchronized(queue){
				queue.removeRequest(request);
				queue.notifyAll();
				requestDao.save(request);
			}
			e.printStackTrace();
		} 
	}
	
	private void inputHandle() throws MCASException {
		try {
			MediaUtils.toWorkingDir(new URI(request.getSrc()), request.getIdStr(), TranscoderUtils.getConfigId(request.getConfig()));
		} catch (Exception e) {
			e.printStackTrace();
			MediaUtils.deleteInputFile(request.getIdStr(), TranscoderUtils.getConfigId(request.getConfig()));
			throw new MCASException();
		}
		request.increaseState();
		synchronized(queue){
			queue.update(request);
			queue.notifyAll();
		}
	}
	
	private void outputHandle() throws MCASException {
		Iterator<Transco> i = request.getTranscoded().iterator();
		while(i.hasNext()){
			Transco transco = i.next();
			try {
				MediaUtils.toDestinationUri(transco.getOutputFile(), transco.getDestinationUriUri());
			} catch (Exception e) {
				e.printStackTrace();
				i.remove();
				MediaUtils.deleteFile(transco.getOutputFile());
			}
		}
		if (request.getNumOutputs() > request.getTranscoded().size()){
			if (request.isTranscodedEmpty()){
				MediaUtils.deleteInputFile(request.getIdStr(), TranscoderUtils.getConfigId(request.getConfig()));
				request.setError();
			} else {
				request.setPartialError();
			}
		} else {
			request.increaseState();
		}
		MediaUtils.clean(request.getTranscoded());
		synchronized(queue){
			queue.removeRequest(request);
			queue.notifyAll();
			requestDao.save(request);
		}
	}
	
	public void inputHandleTest() throws MCASException, URISyntaxException {
		inputHandle();
	}
	
	public void outputHandleTest() throws MCASException, URISyntaxException {
		outputHandle();
	}
}
