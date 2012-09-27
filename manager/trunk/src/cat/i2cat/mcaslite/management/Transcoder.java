package cat.i2cat.mcaslite.management;

import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import cat.i2cat.mcaslite.entities.Transco;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.entities.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

public class Transcoder implements Runnable {

	private TranscoderConfig config;
	private TranscoQueue queue;
	private TranscoRequest request;
	private List<Transco> transcos;
	private DefaultExecutor executor;
	
	public Transcoder(TranscoQueue queue, TranscoRequest request, String config) throws MCASException{
		this.queue = queue;
		this.request = request;
		this.config = TranscoderUtils.loadConfig(config);
		this.transcos = TranscoderUtils.transcoBuilder(this.config, request.getIdStr(), request.getDst());
	}

	@Override
	public void run() {
		request.setNumOutputs(transcos.size());
		for(Transco transco : transcos){
			try {
				executeCommand(transco.getCommand().trim(), executor);
				request.addTrancodedUri(transco.getDestinationUriStr());
			} catch (MCASException e) {
				e.printStackTrace();
			}
		}
		if (request.isTranscodedUriEmpty()){
			request.setError();
			//TODO: manage callback
			return;
		}
		queue.update(request);
		try {
			synchronized(queue){
				request.increaseState();
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
	
	private void executeCommand(String cmd, DefaultExecutor executor) throws MCASException{
		CommandLine commandLine = CommandLine.parse(cmd.trim());
		executor = new DefaultExecutor();
		executor.setWatchdog(new ExecuteWatchdog(24 * 3600 * 1000));
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
