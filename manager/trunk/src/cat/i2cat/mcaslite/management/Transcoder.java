package cat.i2cat.mcaslite.management;

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

public class Transcoder implements Runnable {

	private TranscoderConfig config;
	private TranscoQueue queue;
	private TranscoRequest request;
	private List<Transco> transcos;
	private DefaultExecutor executor;
	private DAO<TranscoRequest> requestDao = new DAO<TranscoRequest>(TranscoRequest.class); 
	
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
		for(Transco transco : transcos){
			try {
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
					queue.removeRequest(request);
					requestDao.save(request);
				}
				return;
			}
			request.increaseState();
			synchronized(queue){
				queue.update(request);
				queue.notifyAll();
			}
		} catch (MCASException e){
			e.printStackTrace();
		}	
	}

	public void stop() {
		if (this.executor != null)
			while (this.executor.getWatchdog().isWatching()) {
				this.executor.getWatchdog().destroyProcess();
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
	
}
