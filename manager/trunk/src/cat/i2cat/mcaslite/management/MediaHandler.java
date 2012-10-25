package cat.i2cat.mcaslite.management;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Downloader;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.Uploader;

public class MediaHandler implements Runnable, Cancellable {

	private TranscoQueue queue;
	private TranscoRequest request;
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class); 
	private Downloader downloader;
	private Uploader uploader;
	private boolean cancelled = false;
	private boolean done = false;
	
	public MediaHandler(TranscoQueue queue, TranscoRequest request) throws MCASException {
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
			setDone(true);
		} catch (MCASException e) {
			request.setError();
			MediaUtils.clean(request);
			synchronized(queue){
				if (queue.removeRequest(request)){
					requestDao.save(request);
					queue.notifyAll();
				}
			}
			e.printStackTrace();
			setDone(true);
		} 
	}
	
	private void inputHandle() throws MCASException {
		try {
			downloader = new Downloader(new URI(request.getSrc()), MediaUtils.setInFile(request.getIdStr(), request.getTConfig()));
			downloader.toWorkingDir();
		} catch (Exception e) {
			e.printStackTrace();
			MediaUtils.deleteInputFile(request.getIdStr(), request.getTConfig().getInputWorkingDir());
			throw new MCASException();
		}
		setDone(true);
		if (! isCancelled()) {
			request.increaseState();
			synchronized(queue){
				queue.update(request);
				queue.notifyAll();
			}
		}
	}
	
	private void outputHandle() throws MCASException {
		Iterator<Transco> i = request.getTranscoded().iterator();
		while(i.hasNext()){
			Transco transco = i.next();
			try {
				uploader = new Uploader(new URI(transco.getDestinationUri()), new File(transco.getOutputFile()));
				uploader.toDestinationUri();
			} catch (Exception e) {
				e.printStackTrace();
				i.remove();
				MediaUtils.deleteFile(transco.getOutputFile());
			}
		}
		try {
			setDone(true);
			if (! isCancelled()) {
				if (request.getNumOutputs() > request.getTranscoded().size()){
					if (request.isTranscodedEmpty()){
						MediaUtils.deleteInputFile(request.getIdStr(), request.getTConfig().getInputWorkingDir());
						request.setError();
					} else {
						request.setPartialError();
					}
				} else {
					request.increaseState();
				}
				synchronized(queue){
					if (queue.removeRequest(request)) {
						requestDao.save(request);
						queue.notifyAll();
					}
				}
			}
		} finally {
			MediaUtils.clean(request);
		}
	}
	
	private boolean cancelDownload(boolean mayInterruptIfRunning){
		if (downloader != null) {
			return downloader.cancel(mayInterruptIfRunning);
		}
		return true;
	}
	
	private boolean cancelUpload(boolean mayInterruptIfRunning){
		if (uploader != null) {
			return uploader.cancel(mayInterruptIfRunning);
		}
		return true;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (! isDone()){
			switch (request.getState()) {
				case M_PROCESS:
					setCancelled(cancelDownload(mayInterruptIfRunning));
					break;
				case MOVING:
					setCancelled(cancelUpload(mayInterruptIfRunning));
					break;
				default:
					setCancelled(false);
			}
			return isCancelled();
		} else {
			MediaUtils.clean(request);
			return true;
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	private void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	private void setDone(boolean done){
		this.done = done; 
	}
}
