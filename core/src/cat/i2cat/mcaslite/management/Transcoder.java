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

	private final TRequest request;
	private MediaHandler mediaH;
	private Transco transco;
	private DefaultExecutor executor;
	private boolean done = false;
	private boolean cancelled = false;
	private final Semaphore semaphore;
	private final Semaphore mutex;
	
	
	public Transcoder(TRequest request, int transco, Semaphore semaphore, Semaphore mutex) throws MCASException{
		this.request = request;
		this.transco = request.getSingleTransco(transco);
		this.executor = new DefaultExecutor(); 
		this.mediaH = new MediaHandler(request);
		this.semaphore = semaphore;
		this.mutex = mutex;
	}

	@Override
	public void run() {
		boolean stopped = false;
		try {
			mutex.acquire();
			mediaH.inputHandle(transco);
			mutex.release();
			execute(transco, request.isLive());
		} catch (MCASException e){
			request.setTranscoStatus(transco, Status.ERROR);
			e.printStackTrace();
			MediaUtils.deleteFile(transco.getOutputDir());
		} catch (InterruptedException e) {
			mutex.release();
			e.printStackTrace();
		} finally {
			semaphore.release();
			setDone(true);
			if (isCancelled()) {
				MediaUtils.cleanTransco(transco);
				stopped = true;
			}
			try {
				mediaH.outputHandle(stopped, transco);
				request.setTranscoStatus(transco, Status.DONE);
			} catch (MCASException e) {
				request.setTranscoStatus(transco, Status.ERROR);
				e.printStackTrace();
			}
			request.updateStatus();
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
		request.setTranscoStatus(transco, Status.PROCESS_T);
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
		setCancelled(mediaH.cancel(mayInterruptIfRunning) | cancelWorker(mayInterruptIfRunning));
		if (isCancelled()){
			request.setTranscoStatus(transco, Status.CANCELLED);
		}
		return isCancelled();
	}
	
	private boolean cancelWorker(boolean mayInterruptIfRunning){
		MediaUtils.cleanTransco(transco);
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
