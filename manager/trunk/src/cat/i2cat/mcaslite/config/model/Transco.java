package cat.i2cat.mcaslite.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transcos")
public class Transco {
	
	@Column(nullable = false, length = 255)
	private String inputFile;
	@Column(nullable = false, length = 1000)
	private String command;
	@Column(nullable = false, length = 255)
	private String outputDir;
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	public Transco(){
		
	}
	
	public Transco(String command, String outputDir, String inputFile){
		this.command = command;
		this.outputDir = outputDir;
		this.inputFile = inputFile;
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
	
//	@Override
//	public boolean equals(Object o){
//		try {
//			Transco transco = (Transco) o;
//			if (transco.getCommand().equals(this.command) || transco.getOutputFile().equals(this.outputFile)){
//				return true;
//			}
//		} catch (Exception e){
//			e.printStackTrace();
//			return false;
//		}
//		return false;
//	}
}
