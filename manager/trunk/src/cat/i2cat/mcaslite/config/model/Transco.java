package cat.i2cat.mcaslite.config.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

//import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;

@Entity
@Table(name = "transco")
public class Transco {
	
	private String inputFile;
	private String command;
	private String outputFile;
	private String destinationUri;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	private UUID request;
	
	public Transco(){
		
	}
	
	public Transco(String command, String outputFile, String destinationUri, String inputFile){
		this.command = command;
		this.outputFile = outputFile;
		this.inputFile = inputFile;
		this.destinationUri = destinationUri;
	}
	
	public UUID getRequest(){
		return request;
	}
	
	public void setRequest(UUID request){
		this.request = request;
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
	
	public String getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Transient
	public URI getDestinationUriUri() throws MCASException {
		try {
			URI uri = new URI(destinationUri);
			return uri;
		} catch(URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public String getDestinationUri(){
		return destinationUri;
	}

	public void setDestinationUri(String destinationFile) {
		this.destinationUri = destinationFile;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	@Override
	public boolean equals(Object o){
		try {
			Transco transco = (Transco) o;
			if (transco.getCommand().equals(this.command) || transco.getOutputFile().equals(this.outputFile)){
				return true;
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return false;
	}
}
