package cat.i2cat.mcaslite.management;

import java.util.concurrent.Semaphore;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class Transcoder implements Runnable, Cancellable {

	private ProcessQueue queue;
	private TRequest request;
	private MediaHandler mediaH;
	private Transco transco;
	private DefaultExecutor executor;
	private boolean done = false;
	private boolean cancelled = false;
	private final Semaphore semaphore;
	
	
	public Transcoder(TRequest request, Transco transco, Semaphore semaphore) throws MCASException{
		this.request = request;
		this.transco = transco;
		this.executor = new DefaultExecutor(); 
		this.mediaH = new MediaHandler(queue, request);
		this.semaphore = semaphore;
	}

	@Override
	public void run() {
		boolean stopped = false;
		try {
			mediaH.inputHandle(transco.getProfileName());
			execute(transco, request.isLive());
		} catch (MCASException e){
			request.deleteTranscoded(transco);
			queue.update(request);
			e.printStackTrace();
			MediaUtils.deleteFile(transco.getOutputDir());
		} finally {
			semaphore.release();
			setDone(true);
			if (isCancelled()) {
				MediaUtils.clean(request);
				stopped = true;
			}
			try {
				mediaH.outputHandle(stopped, transco);
			} catch (MCASException e) {
				e.printStackTrace();
			}
		}
	}
	
//	private void transcodify() throws MCASException{
//		boolean stopped = false;
//		try {
//			Iterator<Transco> it = transcos.iterator();
//			while(! isCancelled() && it.hasNext()){
//				Transco transco = it.next();
//				try {
//					mediaH.inputHandle(transco.getProfileName());
//					execute(transco, request.isLive());
//				} catch (MCASException e) {
//					request.deleteTranscoded(transco);
//					queue.update(request);
//					e.printStackTrace();
//					MediaUtils.deleteFile(transco.getOutputDir());
//				}
//			}
//			setDone(true);
//			if (isCancelled()) {
//				MediaUtils.clean(request);
//				stopped = true;
//				return;
//			}
//			if (request.isTranscodedEmpty() && ! isCancelled()){
//				manageError();
//				stopped = true;
//			} else {
//				request.increaseStatus();
//				queue.update(request);
//			}
//		} finally {
//			mediaH.outputHandle(stopped);
//		}
//	}
	
	private void execute(Transco transco, boolean live) throws MCASException{
		request.addTrancoded(transco);
		queue.update(request);
		executeCommand(transco.getCommand().trim());
		if (! live){
			processManifest(transco);
		}
	}
	
//	private void manageError(){
//		request.setError();
//		MediaUtils.clean(request);
//		if (queue.remove(request)){
//			requestDao.save(request);
//		}
//	}

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
		CommandLine commandLine = CommandLine.parse(cmd.trim());
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
			setCancelled(mediaH.cancel(mayInterruptIfRunning) | cancelWorker(mayInterruptIfRunning));
			return isCancelled();
		}
	}
	
	private boolean cancelWorker(boolean mayInterruptIfRunning){
		MediaUtils.clean(request);
		if (! isDone()){
			return stop(mayInterruptIfRunning);
		} else {
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
