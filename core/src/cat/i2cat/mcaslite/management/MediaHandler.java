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

	public void inputHandle(String profile) throws MCASException {
		String path = MediaUtils.createOutputWorkingDir(request.getId(), request.getTConfig().getOutputWorkingDir());
		if (request.isLive()){
			initWatcher(profile, path);
		} else {
			copyToWorkingDir();
		}
	}
	
	private void initWatcher(String profile, String path) throws MCASException {
		try {
			URI dst = new URI(request.getDst());
			watcher = new Watcher(path, request.getTConfig(), dst, profile, request.getTitle());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
		(new Thread(watcher)).start();
	}
	
	public void cancelWatcher(){
		if (watcher != null){
			watcher.cancel(true);
		}
	}
	
	private void fileToWorkingDir(File file) throws MCASException {
		try {
			downloader = new Downloader(new URI(request.getSrc()), file);
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
	
	private void copyToWorkingDir() throws MCASException{
		File file = MediaUtils.setInFile(request.getId(), request.getTConfig());
		if (! file.exists()){
			fileToWorkingDir(file);
		}
	}
	
	public void filesUpload(Transco transco) throws MCASException {
		setDone(false);
		try {
			uploader = new Uploader(new URI(request.getDst()));
			uploader.upload(Paths.get(transco.getOutputDir()));		
		} catch (URISyntaxException e) {
			throw new MCASException();
		} finally {
			setDone(true);
			MediaUtils.cleanTransco(transco);
		}
	}
	
	public void outputHandle(boolean stopped, Transco transco) throws MCASException {
		if (request.isLive()){
			cancelWatcher();
		} else if (! stopped) {
			filesUpload(transco);
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
			setCancelled(cancelDownload(mayInterruptIfRunning) | cancelUpload(mayInterruptIfRunning));
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
