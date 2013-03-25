package cat.i2cat.mcaslite.management;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

public class Transcoder implements Runnable, Cancellable {

	private ProcessQueue queue;
	private TRequest request;
	private MediaHandler mediaH;
	private List<Transco> transcos;
	private DefaultExecutor executor;
	private DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
	private boolean done = false;
	private boolean cancelled = false;
	
	public Transcoder(ProcessQueue queue, TRequest request) throws MCASException{
		this.queue = queue;
		this.request = request;
		try {
			this.transcos = TranscoderUtils.transcoBuilder(request.getTConfig(), request.getId(), 
					new URI(request.getSrc()), request.getTitle());
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
		this.executor = new DefaultExecutor(); 
		this.mediaH = new MediaHandler(queue, request);
	}

	@Override
	public void run() {
		try {
			transcodify();
		} catch (MCASException e){
			manageError();
			setDone(true);
			e.printStackTrace();
		}
	}
	
	private void transcodify() throws MCASException{
		try {
			Iterator<Transco> it = transcos.iterator();
			while(! isCancelled() && it.hasNext()){
				Transco transco = it.next();
				try {
					mediaH.inputHandle(request.isLive(), transco.getProfileName());
					execute(transco, request.isLive());
				} catch (MCASException e) {
					request.deleteTranscoded(transco);
					queue.update(request);
					e.printStackTrace();
					MediaUtils.deleteFile(transco.getOutputDir());
				}
			}
			setDone(true);
			if (isCancelled()) {
				MediaUtils.clean(request);
				return;
			}
			if (request.isTranscodedEmpty() && ! isCancelled()){
				manageError();
			} else {
				request.increaseStatus();
				queue.update(request);
			}
		} finally {
			mediaH.outputHandle(request.isLive());
		}
	}
	
	private void execute(Transco transco, boolean live) throws MCASException{
		request.addTrancoded(transco);
		queue.update(request);
		executeCommand(transco.getCommand().trim());
		if (! live){
			processManifest(transco);
		}
	}
	
	private void manageError(){
		request.setError();
		MediaUtils.clean(request);
		if (queue.remove(request)){
			requestDao.save(request);
		}
	}

	private boolean stop(boolean mayInterruptIfRunning) {
		setCancelled(true);
		if (this.executor != null) {
			if (mayInterruptIfRunning) {
				while (this.executor.getWatchdog().isWatching()) {
					this.executor.getWatchdog().destroyProcess();
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void processManifest(Transco transco) throws MCASException{
		for(TProfile profile : request.getTConfig().getProfiles()){
			if (profile.getName().equals(transco.getProfileName())){
				profile.processManifest(transco, request.getTitle());
				return;
			}
		}
	}
	
	private void executeCommand(String cmd) throws MCASException{
		CommandLine commandLine = CommandLine.parse("/Users/i2cat/ffmpeg/" + cmd.trim());
		System.out.println(commandLine.toString());
		executor.setWatchdog(new ExecuteWatchdog(request.getTConfig().getTimeout() * 1000));
		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		try {
			executor.execute(commandLine);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning){
		synchronized(queue){
			switch(request.getStatus().getInt()){
				case Status.PROCESS_M:
					return mediaH.cancel(mayInterruptIfRunning);
				case Status.PROCESS_MO:
					return mediaH.cancel(mayInterruptIfRunning);
				case Status.PROCESS_T:
					MediaUtils.clean(request);
					if (! isDone()){
						return stop(mayInterruptIfRunning);
					} else {
						return true;
					}
				case Status.PROCESS_L:
					MediaUtils.clean(request);
					if (! isDone()){
						return stop(mayInterruptIfRunning);
					} else {
						return true;
					}
				default:
					return false;
			}
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
