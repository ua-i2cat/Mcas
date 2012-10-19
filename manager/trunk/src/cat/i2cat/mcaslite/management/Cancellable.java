package cat.i2cat.mcaslite.management;

public interface Cancellable {

	public boolean cancel(boolean mayInterruptIfRunning);
	
	public boolean isCancelled();
	
	public boolean isDone();
}
