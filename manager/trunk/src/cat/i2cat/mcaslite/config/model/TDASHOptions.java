package cat.i2cat.mcaslite.config.model;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
	 public List<Transco> commandBuilder(String input, String output){
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "MP4Box -rap -frag-rap";
		cmd += " -dash " + this.segDuration + " -frag " + this.fragDuration;
		cmd += " -segment-name " + this.getName() + "_seg";
		cmd += " -out " + output + "/" + this.getName() + "." + this.getFormat();
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
	public void setUris(JSONArray jsonAr, String destination) throws MCASException{
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("uri", Paths.get(destination, this.getName() + "." + this.getFormat()));
			jsonAr.put(jsonObj);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MCASException();
		}
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
