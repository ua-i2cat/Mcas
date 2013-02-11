package cat.i2cat.mcaslite.management;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import cat.i2cat.mcaslite.config.dao.DAO;
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
					new URI(request.getDst()), new URI(request.getSrc()));
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
		this.executor = new DefaultExecutor(); 
		this.mediaH = new MediaHandler(queue, request);
	}

	@Override
	public void run() {
		try {
			if (! request.isLive()){
				videoOnDemand();
			} else {
				HTTPLive();
			}
		} catch (MCASException e){
			manageError();
			setDone(true);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void HTTPLive() throws MCASException, IOException, URISyntaxException {
		//TODO: test input output?
		try {
			mediaH.initWatcher();
			transcodify();
			setDone(true);
			if (! isCancelled()) {
				stop(true);
				//throw new MCASException();
			}
			request.increaseStatus();
			queue.update(request);
		} finally {
			mediaH.cancelWatcher();
		}
	}
	
	private void videoOnDemand() throws MCASException {
		mediaH.inputHandle();
		transcodify();
		setDone(true);
		if (isCancelled()) {
			MediaUtils.clean(request);
			return;
		}
		if (request.isTranscodedEmpty()){
			manageError();
		}
		request.increaseStatus();
		queue.update(request);
		mediaH.outputHandle();
	}
	
	private void transcodify(){
		Iterator<Transco> it = transcos.iterator();
		while(! isCancelled() && it.hasNext()){
			Transco transco = it.next();
			try {
				request.addTrancoded(transco);
				executeCommand(transco.getCommand().trim());				
			} catch (MCASException e) {
				request.deleteTranscoded(transco);
				e.printStackTrace();
				MediaUtils.deleteFile(transco.getOutputFile());
			}
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
	
	private void executeCommand(String cmd) throws MCASException{
		CommandLine commandLine = CommandLine.parse(cmd.trim());
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
					if (! isDone()){
						return stop(mayInterruptIfRunning);
					} else {
						MediaUtils.clean(request);
						return true;
					}
				case Status.PROCESS_L:
					if (! isDone()){
						return stop(mayInterruptIfRunning);
					} else {
						MediaUtils.clean(request);
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
