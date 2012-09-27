package cat.i2cat.mcaslite.management;

import java.net.URI;
import java.net.URISyntaxException;

import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class MediaHandler implements Runnable {

	private TranscoQueue queue;
	private TranscoRequest request;
	
	public MediaHandler(TranscoQueue queue, TranscoRequest request){
		this.queue = queue;
		this.request = request;
	}
	
	@Override
	public void run() {
		try {
			MediaUtils.toWorkingDir(new URI(request.getSrc()), request.getIdStr());
			request.increaseState();
			synchronized(queue){
				queue.update(request);
				queue.notifyAll();
			}
		} catch (MCASException e) {
			request.setError();
			queue.update(request);
			MediaUtils.deleteInput(request.getIdStr());
			//TODO Error callback hook, should be synchronized?
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
