package net.i2cat.mcas.management;

import net.i2cat.mcas.exceptions.MCASException;

public abstract class Status {
	
	public static final int CREATED 	= 1;
	public static final int QUEUED 		= 2;
	public static final int PROCESS_M 	= 3;
	public static final int PROCESS_T 	= 4;
	public static final int PROCESS_L	= 5;
	public static final int PROCESS_MO	= 6;
	public static final int DONE		= 7;
	public static final int ERROR		= 100;
	public static final int CANCELLED	= 101;
	public static final int P_ERROR 	= 102;

	public abstract void increaseStatus() throws MCASException;
	
	public abstract void setError();
	
	public abstract void setPartialError();
	
	public abstract void setCancelled();
	
	public abstract Status getStatus();
	
	public abstract int getInt();
	
	public abstract boolean isDone();
	
	public abstract boolean hasNext();;
	
	@Override
	public String toString() {
		switch(getInt()){
			case CREATED:
				return "CREATED";
			case QUEUED:
				return "QUEUED";
			case PROCESS_M:
				return "PROCESS_M";
			case PROCESS_T:
				return "PROCESS_T";
			case PROCESS_MO:
				return "PROCESS_MO";
			case DONE:
				return "DONE";
			case PROCESS_L:
				return "PROCESS_L";
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
