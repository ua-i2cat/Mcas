package cat.i2cat.mcaslite.management;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class FileStatus extends Status{

	private int nextStatus(int status) throws MCASException{
		switch(status){
			case CREATED:
				return QUEUED;
			case QUEUED:
				return PROCESS_T;
			case PROCESS_T:
				return DONE;
			default:
				throw new MCASException();
		}
	}
	
	public void increaseStatus() throws MCASException{
		status = nextStatus(status);
	}
	
	public void setError(){
		status = ERROR;
	}
	
	public void setPartialError(){
		status = P_ERROR;
	}
	
	public void setCancelled(){
		status = CANCELLED;
	}
	
	public Status getStatus(){
		return this;
	}
	
	public int getInt(){
		return status;
	}

	public boolean hasNext() {
		try {
			nextStatus(status);
		} catch (Exception e){
			return false;
		}
		return true;
	}
	
	public void setCopying() throws MCASException {
		throw new MCASException();
		
	}

	public void setTranscoding() throws MCASException {
		throw new MCASException();
		
	}

	public void setMoving() throws MCASException {
		throw new MCASException();
		
	}

	public void setDone() throws MCASException {
		throw new MCASException();
	}
}
