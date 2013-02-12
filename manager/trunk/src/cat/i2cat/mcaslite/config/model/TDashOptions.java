package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.DashManifestManager;
import cat.i2cat.mcaslite.management.FileEventProcessor;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

@Entity
@DiscriminatorValue("tDashOptions")

public class TDashOptions extends TProfile {

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
	 public List<Transco> commandBuilder(String input, String output, String dst){
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "MP4Box -rap -frag-rap";
		cmd += " -dash " + this.segDuration + " -frag " + this.fragDuration;
		cmd += " -segment-name " + FilenameUtils.getBaseName(output);
		cmd += " -out " + output +".mpd";
		cmd += " " + input;
		
		transcos.add(new Transco(cmd, (new File(output)).getParentFile().getPath(), 
					((new File(dst)).getParentFile().getPath()), input));
		
		return transcos;
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
