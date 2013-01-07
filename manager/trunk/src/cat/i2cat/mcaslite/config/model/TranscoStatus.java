package cat.i2cat.mcaslite.config.model;

import cat.i2cat.mcaslite.exceptions.MCASException;

public interface TranscoStatus {

	public static final int CREATED 		= 1;
	public static final int QUEUED 			= 2;
	public static final int M_PROCESS 		= 3;
	public static final int T_QUEUED 		= 4;
	public static final int PROCESS 		= 5;
	public static final int TRANSCODED 		= 6;
	public static final int MOVING 			= 7;
	public static final int DONE 			= 8;
	public static final int CANCELLED 		= 50;
	public static final int PARTIAL_ERROR 	= 101;
	public static final int ERROR 			= 100;

	public abstract void increaseStatus() throws MCASException;
	
	public abstract void setCancelled();
	
	public abstract void setError();
	
	public abstract void setPartialError();
	
	public abstract String toString();
	
	public abstract int getId();
}
