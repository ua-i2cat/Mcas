package cat.i2cat.mcaslite.management;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

public class Transcoder implements Runnable, Cancellable {

	private TranscoderConfig config;
	private TranscoQueue queue;
	private TranscoRequest request;
	private List<Transco> transcos;
	private DefaultExecutor executor;
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class);
	private boolean done = false;
	private boolean cancelled = false;
	
	public Transcoder(TranscoQueue queue, TranscoRequest request) throws MCASException{
		this.queue = queue;
		this.request = request;
		this.config = TranscoderUtils.loadConfig(request.getConfig());
		this.transcos = TranscoderUtils.transcoBuilder(this.config, request.getIdStr(), request.getDst());
		this.executor = new DefaultExecutor(); 
	}

	@Override
	public void run() {
		request.setNumOutputs(transcos.size());
		Iterator<Transco> it = transcos.iterator();
		while(! isCancelled() && it.hasNext()){
			Transco transco = it.next();
			try {
				executeCommand(transco.getCommand().trim());				
				request.addTrancoded(transco);
			} catch (MCASException e) {
				e.printStackTrace();
				MediaUtils.deleteFile(transco.getOutputFile());
			}
		}
		try {
			setDone(true);
			if (! isCancelled()) {
				if (request.isTranscodedEmpty()){
					MediaUtils.clean(request);
					request.setError();
					synchronized(queue){
						if (queue.removeRequest(request)) {
							requestDao.save(request);
							queue.notifyAll();
						}
					}
					return;
				}
				request.increaseState();
				synchronized(queue){
					queue.update(request);
					queue.notifyAll();
				}
			} else {
				MediaUtils.clean(request);
			}
		} catch (MCASException e){
			e.printStackTrace();
			setDone(true);
		}	
	}

	private boolean stop(boolean mayInterruptIfRunning) {
		setCancelled(true);
		if (this.executor != null) {
			while (this.executor.getWatchdog().isWatching()) {
				if (mayInterruptIfRunning) {
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
		executor.setWatchdog(new ExecuteWatchdog(config.getTimeout() * 1000));
		//TODO: manage in a different manner timeout when live processing
		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		try {
			executor.execute(commandLine);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (! isDone()){
			return stop(mayInterruptIfRunning);
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
