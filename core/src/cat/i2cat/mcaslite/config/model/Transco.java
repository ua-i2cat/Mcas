package cat.i2cat.mcaslite.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.management.TranscoStatus;

@Entity
@Table(name = "transcos")
public class Transco {
	
	@Column(nullable = false, length = 255)
	private String inputFile;
	@Column(nullable = false, length = 5000)
	private String command;
	@Column(nullable = false, length = 255)
	private String outputDir;
	@Column(nullable = false, length = 255)
	private String profileName;
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Transient
	private Status status;

	public Transco(){
		
	}
	
	public Transco(String command, String outputDir, String inputFile, String profileName){
		this.command = command;
		this.outputDir = outputDir;
		this.inputFile = inputFile;
		this.profileName = profileName;
		this.status = new TranscoStatus();
	}
	
	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public synchronized void setStatus(int status) throws MCASException{
		switch(status){
			case Status.PROCESS_C:
				this.status.setCopying();
				return;
			case Status.PROCESS_T:
				this.status.setTranscoding();
				return;
			case Status.PROCESS_M:
				this.status.setMoving();
				return;
			case Status.DONE:
				this.status.setDone();
				return;
			case Status.CANCELLED:
				this.status.setCancelled();
				return;
			case Status.ERROR:
				this.status.setError();
				return;
			default:
				throw new MCASException();
		}
	}
	
	public synchronized Status getStatus(){
		return status;
	}
}
