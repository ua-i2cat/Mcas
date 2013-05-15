//package cat.i2cat.mcaslite.management;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Semaphore;
//
//import cat.i2cat.mcaslite.config.dao.DAO;
//import cat.i2cat.mcaslite.config.model.TRequest;
//import cat.i2cat.mcaslite.config.model.Transco;
//import cat.i2cat.mcaslite.exceptions.MCASException;
//import cat.i2cat.mcaslite.utils.MediaUtils;
//import cat.i2cat.mcaslite.utils.TranscoderUtils;
//
//public class TranscoParallelisation implements Runnable, Cancellable {
//
//	private final Semaphore semaphore;
//	private List<Transco> transcos;
//	private TRequest request;
//	private ProcessQueue queue;
//	private List<Cancellable> workers = new ArrayList<Cancellable>();
//	private boolean done = false;
//	private boolean cancelled = false;
//	private final ExecutorService executor;
//	private final DAO<TRequest> requestDao;
//	
//	public TranscoParallelisation(TRequest request, ProcessQueue queue, ExecutorService executor) throws MCASException{
//		this.request = request;
//		this.queue = queue;
//		this.executor = executor;
//		this.requestDao = new DAO<TRequest>(TRequest.class);
//		try {
//			this.transcos = TranscoderUtils.transcoBuilder(request.getTConfig(), request.getId(), 
//					new URI(request.getSrc()), request.getTitle());
//			this.semaphore = new Semaphore(transcos.size());
//		} catch (URISyntaxException e) {
//			throw new MCASException();
//		}
//	}
//	
//	@Override
//	public boolean cancel(boolean mayInterruptIfRunning) {
//		if (! isDone()){
//			boolean and = true;
//			for (Cancellable cancellableWorker : workers){
//				and = and && cancellableWorker.cancel(mayInterruptIfRunning);
//			}
//			return true;
//		}
//		return true;
//	}
//
//	@Override
//	public boolean isCancelled() {
//		return cancelled;
//	}
//
//	@Override
//	public boolean isDone() {
//		return done;
//	}
//
//	@Override
//	public void run() {
//		synchronized(this){
//			for (Transco transco : transcos){
//				try {
//					semaphore.acquire();
//					Transcoder transcoder = new Transcoder(request, transco, semaphore);
//					//workers.add(transcoder);
//					//executor.execute(transcoder);
//				} catch (MCASException e) {
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					return;
//				}
//			}
//		}
//		try {
//			semaphore.acquire(transcos.size());
//			setDone(true);
//			request.increaseStatus();
//			if (! isCancelled()) {
//				if (request.getNumOutputs() > request.getTranscoded().size()){
//					if (request.isTranscodedEmpty()){
//						MediaUtils.clean(request);
//						request.setError();
//					} else {
//						request.setPartialError();
//					}
//				}
//			}
//		} catch (Exception e){
//			e.printStackTrace();
//			MediaUtils.clean(request);
//			request.setError();
//		} finally {
//			if (queue.remove(request)){
//				requestDao.save(request);
//			}
//		}
//	}
//
//	
//	
//	public void setDone(boolean done) {
//		this.done = done;
//	}
//
//	public void setCancelled(boolean cancelled) {
//		this.cancelled = cancelled;
//	}
//}
