package cat.i2cat.mcaslite.entities;

import java.net.URI;
import java.net.URISyntaxException;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class Transco {
	
	private String inputFile;
	private String command;
	private String outputFile;
	private String destinationUri;
	
	public Transco(String command, String outputFile, String destinationUri, String inputFile){
		this.command = command;
		this.outputFile = outputFile;
		this.inputFile = inputFile;
		this.destinationUri = destinationUri;
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

	public URI getDestinationUri() throws MCASException {
		try {
			URI uri = new URI(destinationUri);
			return uri;
		} catch(URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public String getDestinationUriStr(){
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
