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
	private boolean cancel = false;
	
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
		while(!cancel && it.hasNext()){
			try {
				Transco transco = it.next();
				executeCommand(transco.getCommand().trim());				
				request.addTrancoded(transco);
			} catch (MCASException e) {
				e.printStackTrace();
			}
		}
		try {
			if (request.isTranscodedEmpty()){
				MediaUtils.deleteInputFile(request.getIdStr(), config.getId());
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
			done = true;
		} catch (MCASException e){
			e.printStackTrace();
			done = true;
		}	
	}

	private boolean stop(boolean mayInterruptIfRunning) {
		cancel = true;
		if (this.executor != null) {
			while (this.executor.getWatchdog().isWatching()) {
				if (mayInterruptIfRunning) {
					this.executor.getWatchdog().destroyProcess();
				}
			}
			MediaUtils.clean(request);
			request.setCancelled();
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
		return stop(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public boolean isDone() {
		return done;
	}	
}
