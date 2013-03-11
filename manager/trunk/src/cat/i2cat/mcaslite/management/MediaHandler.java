package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Downloader;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.Uploader;

public class MediaHandler implements Cancellable {

	private ProcessQueue queue;
	private TRequest request;
	private DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class); 
	private Downloader downloader;
	private Uploader uploader;
	private boolean cancelled = false;
	private boolean done = false;
	private Watcher watcher;
	
	public MediaHandler(ProcessQueue queue, TRequest request) throws MCASException {
		this.queue = queue;
		this.request = request;
	}

	public void inputHandle() throws MCASException {
		copyToWorkingDir();
	}
	
	public void initWatcher() throws IOException, MCASException, URISyntaxException{
		String path = MediaUtils.createOutputWorkingDir(request.getId(), request.getTConfig().getOutputWorkingDir());
		URI dst = new URI(request.getDst());
		watcher = new Watcher(path, request.getTConfig(), dst);
		(new Thread(watcher)).start();
	}
	
	public void cancelWatcher(){
		if (watcher != null){
			watcher.cancel(true);
		}
	}
	
	private void copyToWorkingDir() throws MCASException {
		try {
			downloader = new Downloader(new URI(request.getSrc()), MediaUtils.setInFile(request.getId(), request.getTConfig()));
			downloader.toWorkingDir();
		} catch (Exception e) {
			e.printStackTrace();
			request.setError();
			MediaUtils.clean(request);
			if (queue.remove(request)){
				requestDao.save(request);
			}
			setDone(true);
			throw new MCASException();
		}
		setDone(true);
		if (! isCancelled()) {
			request.increaseStatus();
			queue.update(request);
		}
	}
	
	public void outputHandle() throws MCASException {
		try {
			for (Transco transco : request.getTranscoded()){
				uploader = new Uploader(new URI(request.getDst()));
				uploader.toDestinationUri(new File(transco.getOutputFile()));	
			}	
			
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
		
		try {
			setDone(true);
			if (! isCancelled()) {
				if (request.getNumOutputs() > request.getTranscoded().size()){
					if (request.isTranscodedEmpty()){
						MediaUtils.deleteInputFile(request.getId(), request.getTConfig().getInputWorkingDir());
			//TODO			Uploader.deleteDestination(request.getDst());
						request.setError();
					} else {
						request.setPartialError();
					}
				} else {
					request.increaseStatus();
				}
				if (queue.remove(request)) {
					requestDao.save(request);
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
			switch (request.getStatus().getInt()) {
				case Status.PROCESS_M:
					setCancelled(cancelDownload(mayInterruptIfRunning));
					break;
				case Status.PROCESS_MO:
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
