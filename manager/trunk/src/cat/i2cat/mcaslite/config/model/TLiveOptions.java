package cat.i2cat.mcaslite.config.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.utils.TranscoderUtils;

@Entity
@DiscriminatorValue("tLiveOptions")

public class TLiveOptions extends TProfile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String dash_profile;
	@Column
	private int seg_duration;
	@Column
	private int frag_duration;
	@Column
	private int window_length;

	
	@Override
	 public List<Transco> commandBuilder(String input, String output, String dst){
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "MP4Box -rap -frag-rap -url-template";
		cmd += " -dash " + this.seg_duration + " -frag " + this.frag_duration;
		cmd += " -segment-name " + FilenameUtils.getBaseName(output);
		cmd += " -out " + output +".mpd";
		cmd += " " + input;
		
		transcos.add(new Transco(cmd, output, TranscoderUtils.pathToUri(dst), input));
		return transcos;
	}
	
	
	public String getDash_profile() {
		return dash_profile;
	}
	public void setDash_profile(String dash_profile) {
		this.dash_profile = dash_profile;
	}
	public int getSeg_duration() {
		return seg_duration;
	}
	public void setSeg_duration(int seg_duration) {
		this.seg_duration = seg_duration;
	}
	public int getFrag_duration() {
		return frag_duration;
	}
	public void setFrag_duration(int frag_duration) {
		this.frag_duration = frag_duration;
	}
	public int getWindow_length() {
		return window_length;
	}
	public void setWindow_length(int window_length) {
		this.window_length = window_length;
	}
	
	
}
