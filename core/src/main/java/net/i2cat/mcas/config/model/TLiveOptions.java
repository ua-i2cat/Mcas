package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.FileEventProcessor;
import cat.i2cat.mcaslite.management.HLSManifestManager;
import cat.i2cat.mcaslite.utils.MediaUtils;

@Entity
@DiscriminatorValue("Live")
public class TLiveOptions extends TProfile {
	
	private static final long serialVersionUID = 1L;

	@Column
	private int segDuration;
	@Column
	private int windowLength;
	@Column
	private String domain;
	@Column
	private String application;
	@Column
	private boolean record;
	
	public void setRecord(boolean record){
		this.record = record;
	}
	
	public boolean getRecord(){
		return record;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
	
	public int getSegDuration() {
		return segDuration;
	}
	
	public void setSegDuration(int segDuration) {
		this.segDuration = segDuration;
	}
	
	public int getWindowLength() {
		return windowLength;
	}
	
	public void setWindowLength(int windowLength) {
		this.windowLength = windowLength;
	}
	
	private boolean isRTMP(){
		return !domain.isEmpty() && !application.isEmpty(); 
	}
	
	private boolean isHLS(){
		//TODO: validate segduration and framerate and gop coherence
		if (segDuration > 0 && windowLength > 0){
			return true;
		}
		return false;
	}
	
	@Override
	public List<Transco> commandBuilder(String input, String output, boolean live, String title) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		boolean fileSrc = false;
		boolean previous = false;
		if (live){
			//TODO: is it enough to ensure it is not an URL
			fileSrc = input.startsWith("/");
		}
		String cmd = "ffmpeg " + (fileSrc ? "-re -i " : "-i ") + input + " ";
		for (TLevel level : getLevels()){
			cmd += (getGop() > 0 ? " -g " + getGop() : "");
			cmd += (getFps() > 0 ? " -r " + getFps() : "");
			cmd += (level.getWidth() > 0 ? " -vf scale=\"" + level.getWidth() + ":trunc(ow/a/2)*2\"" : "");
			cmd += " -b:v " + level.getMaxRate() + "k -maxrate " + level.getMaxRate() + "k";
			cmd += " -qmin 5 -qmax 60 -crf " + level.getQuality();
			cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
			cmd += " -c:v " + getvCodec() + " -c:a " + getaCodec() + " " + getAdditionalFlags();
			cmd += " -f tee -map 0:v -map 0:a \"";
			if (isHLS()){
				cmd += "[f=ssegment:segment_time=" + getSegDuration() + "]"; 
				cmd += output + File.separator;
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_%d.ts";
				previous = true;
			}
			if (isRTMP()){
				cmd += (previous ? "|": "");
				cmd += "[f=flv]rtmp://" + getDomain() + "/" + getApplication() + "/"; 
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName());
				previous = true;
			}
			if (record){
				cmd += (previous ? "|": "");
				cmd += "[f=mp4]" + output + File.separator; 
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + ".mp4";
			}
			if (!previous){
				throw new MCASException();
			} else {
				cmd += "\"";
			}
		}
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;
	}
	
	@Override
	public List<String> getUris(URI destination, String title, boolean live) throws MCASException{
		List<String> uris = new ArrayList<String>();
		URI dst;
		try {
			if (isHLS()){
				dst = new URI(destination.getScheme(), 
					destination.getHost(), 
					destination.getPath() + "/" + MediaUtils.fileNameMakerByProfile(title, getName()) + "." + "m3u8", 
					null);
				uris.add(dst.toString());
			}
			if (isRTMP()){
				for (TLevel level : this.getLevels()){
					dst = new URI("rtmp", 
							getDomain(), "/" + getApplication() + "/" + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()), 
							null);
					uris.add(dst.toString());
				}
			}
			if(record){
				for (TLevel level : this.getLevels()){
					dst = new URI(destination.getScheme(), 
							destination.getHost(),
							destination.getPath() + "/" + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "." + "mp4", 
							null);
					uris.add(dst.toString());
				}
			}
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
		return uris;
	}
	
	@Override
	public FileEventProcessor getFileEP(URI dst, String title) throws MCASException{
		return new HLSManifestManager(windowLength, segDuration, dst, getLevels(), this.getName(), title);
	}
}
