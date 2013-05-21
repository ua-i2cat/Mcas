package cat.i2cat.mcaslite.management;

import java.util.ArrayList;
import java.util.List;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class ProcessQueue {

	private List<TRequest> queue;
	private int maxProcess = 0;
	private int nProcess = 0;
	private int nWait = 0;
	private static final ProcessQueue INSTANCE = new ProcessQueue();

	public static ProcessQueue getInstance(){
		return INSTANCE;
	}

	private ProcessQueue() {
		this.queue = new ArrayList<TRequest>();
	}
	
	synchronized private boolean hasParallelLimit(){
		if (maxProcess <= 0){
			return false;
		} else {
			return true;
		}
	}
	
	synchronized public int getMaxProcess(){
		return maxProcess;
	}
	
	synchronized public void setMaxProcess(int maxProcess){
		this.maxProcess = maxProcess;
	}
	
	synchronized public int getNWait(){
		return nWait;
	}
	
	synchronized public int getNProcess(){
		return nProcess;
	}
	
	synchronized public int indexOf(TRequest r){
		return queue.indexOf(r);
	}
	
	synchronized public void update(TRequest r) {
		int index = queue.indexOf(r);
		if (index >= 0){
			queue.remove(index);
			queue.add(index, r);
		}
	}
	
	synchronized public boolean remove(TRequest r) {
		if (queue.remove(r)){
			if (r.isWaiting()){
				nWait--;
			} else {
				nProcess--;
			}
			notifyAll();
			return true;
		} else {
			return false;
		} 
	}

	synchronized public TRequest getAndProcess() throws InterruptedException {
		while((hasParallelLimit() && maxProcess <= nProcess)|| nWait <= 0){
			wait();
		}
		nWait--;
		return queue.get(nProcess++);
	}
	
	synchronized public TRequest getProcessObject(TRequest r) {
		if (queue.contains(r)){
			return queue.get(queue.indexOf(r));
		}
		return null;
	}

	synchronized public void put(TRequest r) throws MCASException {
		if (queue.contains(r)) {
			throw new MCASException(); 
		} else {
			this.queue.add(r);
			nWait++;
			notifyAll();
		}
	}
	
	synchronized public void clearQueue(){
		this.queue.clear();
		nProcess = 0;
		nWait = 0;
		notifyAll();
	}

	synchronized public boolean contains(TRequest r) {
		return this.queue.contains(r);
	}

	synchronized public boolean isEmpty() {
		return this.queue.isEmpty();
	}
	
	synchronized public int size() {
		return this.queue.size();
	}
	
	synchronized public boolean hasSlot() {
		boolean hasSlot = (nProcess < maxProcess) && nWait < (maxProcess - nProcess);
		return hasSlot;
	}
	
	synchronized List<TRequest> getAll(){
		return new ArrayList<TRequest>(queue);
	}
	
}

