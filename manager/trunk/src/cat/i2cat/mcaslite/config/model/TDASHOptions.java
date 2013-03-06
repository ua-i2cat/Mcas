package cat.i2cat.mcaslite.config.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.DashManifestManager;
import cat.i2cat.mcaslite.management.FileEventProcessor;

@Entity
@DiscriminatorValue("tDashOptions")

public class TDASHOptions extends TProfile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String dashProfile;
	@Column
	private int segDuration;
	@Column
	private int fragDuration;
	@Column
	private int windowLength;

	
	@Override
	 public List<Transco> commandBuilder(String input, String output, boolean live, String title){
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "MP4Box -rap -frag-rap";
		cmd += " -dash " + this.segDuration + " -frag " + this.fragDuration;
		cmd += " -segment-name " + this.getName() + "_seg";
		cmd += " -out " + output + "/" + title + "_" + this.getName() + "." + this.getFormat();
		cmd += " " + input;
		
		transcos.add(new Transco(cmd, output, input, this.getName()));
		
		return transcos;
	}
	
	@Override
	public void processManifest(Transco transco) throws MCASException{
		DashManifestManager mpdModifier = new DashManifestManager(transco.getOutputDir());
		mpdModifier.processManifest();
	}
	
	@Override
	public List<String> getUris(URI destination, String title) throws MCASException{
		List<String> uris = new ArrayList<String>();
		try {
			URI dst = new URI(destination.getScheme(), 
				destination.getHost(), 
				Paths.get(destination.getPath(), title + "_" + this.getName() + "." + this.getFormat()).toString(), 
				null);
			uris.add(dst.toString());
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
		return uris;
	}
	
	@Transient
	@Override
	public FileEventProcessor getFileEP(URI dst){
		return new DashManifestManager();
	}
	
	public String getDashProfile() {
		return dashProfile;
	}


	public void setDashProfile(String dashProfile) {
		this.dashProfile = dashProfile;
	}


	public int getSegDuration() {
		return segDuration;
	}


	public void setSegDuration(int segDuration) {
		this.segDuration = segDuration;
	}


	public int getFragDuration() {
		return fragDuration;
	}


	public void setFragDuration(int fragDuration) {
		this.fragDuration = fragDuration;
	}


	public int getWindowLength() {
		return windowLength;
	}


	public void setWindowLength(int windowLength) {
		this.windowLength = windowLength;
	}	
	
}
