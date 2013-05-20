package cat.i2cat.mcaslite.management;

import cat.i2cat.mcaslite.exceptions.MCASException;

public abstract class Status {
	
	public static final int CREATED 	= 1;
	public static final int QUEUED 		= 2;
	public static final int PROCESS_C 	= 3;
	public static final int PROCESS_T 	= 4;
	public static final int PROCESS_L	= 5;
	public static final int PROCESS_M 	= 6;
	public static final int DONE		= 7;
	public static final int ERROR		= 100;
	public static final int CANCELLED	= 101;
	public static final int P_ERROR 	= 102;

	protected int status = CREATED;
	
	public abstract void increaseStatus() throws MCASException;
	
	public abstract void setError();
	
	public abstract void setPartialError();
	
	public abstract void setCancelled();
	
	public abstract Status getStatus();
	
	public abstract int getInt();
	
	public abstract boolean hasNext();
	
	public abstract void setCopying() throws MCASException;
	
	public abstract void setTranscoding() throws MCASException;
	
	public abstract void setMoving() throws MCASException;
	
	public abstract void setDone() throws MCASException;
	
	public boolean isDone() {
		if (status == DONE){
			return true;
		}
		return false;
	}
	
	public boolean isCancelled() {
		if (status == CANCELLED){
			return true;
		}
		return false;
	}
	
	public boolean isError() {
		if (status == ERROR){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		switch(getInt()){
			case CREATED:
				return "CREATED";
			case QUEUED:
				return "QUEUED";
			case PROCESS_C:
				return "PROCESS_C";
			case PROCESS_T:
				return "PROCESS_T";
			case DONE:
				return "DONE";
			case PROCESS_L:
				return "PROCESS_L";
			case PROCESS_M:
				return "PROCESS_M";
			case ERROR:
				return "ERROR";
			case CANCELLED:
				return "CANCELLED";
			case P_ERROR:
				return "P_ERROR";
			default:
				return "INVALID_STATUS";
		}
	}
}
