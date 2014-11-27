package net.i2cat.mcas.management;

public interface Cancellable {

	public boolean cancel(boolean mayInterruptIfRunning);
	
	public boolean isCancelled();
	
	public boolean isDone();
}
