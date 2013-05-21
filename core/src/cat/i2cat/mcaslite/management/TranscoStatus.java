package cat.i2cat.mcaslite.management;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoStatus extends Status{
	
	public void increaseStatus() throws MCASException{
		throw new MCASException();
	}
	
	public void setError(){
		status = ERROR;
	}
	
	public void setPartialError(){
		status = ERROR;
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
		if (status < DONE){
			return true;
		} else {
			return false;
		}
	}
	
	public void setCopying() throws MCASException {
		if (status < PROCESS_C){
			status = PROCESS_C;
		} else {
			throw new MCASException();
		}
	}

	public void setTranscoding() throws MCASException {
		if (status < PROCESS_T){
			status = PROCESS_T;
		} else {
			throw new MCASException();
		}
	}

	public void setMoving() throws MCASException {
		if (status < PROCESS_M){
			status = PROCESS_M;
		} else {
			throw new MCASException();
		}
	}
	
	public void setDone() throws MCASException {
		if (status < DONE){
			status = DONE;
		} else {
			throw new MCASException();
		}
	}
}