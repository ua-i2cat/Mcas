package cat.i2cat.mcaslite.config.model;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class FileStatus implements TranscoStatus{

	private int status = CREATED;
	
	private int next() throws MCASException{
		switch (status){
			case CREATED: 
				return QUEUED;
			case QUEUED:
				return M_PROCESS;
			case M_PROCESS:
				return T_QUEUED;
			case T_QUEUED:
				return PROCESS;
			case PROCESS: 
				return TRANSCODED;
			case TRANSCODED:
				return MOVING;
			case MOVING:
				return DONE;
			case DONE:
				throw new MCASException();
			case CANCELLED:
				throw new MCASException();
			case ERROR:
				throw new MCASException();
			case PARTIAL_ERROR:
				throw new MCASException();
			default:
				throw new MCASException();
		}
	}
	
	public FileStatus(int status){
		this.status = status;
	}
			
	public void increaseStatus() throws MCASException {
		status = next();
	}
	
	public void setCancelled() {
		status = CANCELLED;
	}
	
	public void setError() {
		status = ERROR;
	}
	
	public void setPartialError() {
		status = PARTIAL_ERROR;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getId() {
		return status;
	}

	@Override
	public String toString(){
		return ((Integer) status).toString();
	}
	
	@Override
	public boolean equals(Object o){
		TranscoStatus status = (TranscoStatus) o;
		return status.getId() == this.status;
	}
}
