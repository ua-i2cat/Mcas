package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.DashManifestManager;
import cat.i2cat.mcaslite.management.FileEventProcessor;
import cat.i2cat.mcaslite.management.HLSManifestManager;
import cat.i2cat.mcaslite.utils.MediaUtils;

@Entity
@DiscriminatorValue("Dash")

public class TDASHOptions extends TProfile {

	@Column(length = 100)
	private String dashProfile;
	@Column
	private int segDuration;
	@Column
	private int windowLength;

	
	@Override
	public List<Transco> commandBuilder(String input, String output, boolean live, String title) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		boolean fileSrc = false;
		if (live){
			try {
				fileSrc = (new URI(input)).getScheme().equals("file");
				if (fileSrc) {
					input = (new File(new URI(input))).toString();
				}
			} catch (URISyntaxException e) {
				throw new MCASException();
			}
		}
		String cmd = "ffmpeg " + (live && fileSrc ? "-re -i " : "-i ") + input + " -threads 0 ";
		for (TLevel level : getLevels()){
			if ((getvCodec()!= "") && (!live)){
				cmd += " -c:v " + getvCodec() + " " + getAdditionalFlags();
				cmd += " -g 24 -vf scale=\""+ level.getWidth() +":trunc(ow/a/2)*2\"";
				cmd += " -b:v " + level.getMaxRate() + "k -bufsize 10000k -maxrate " + level.getMaxRate() + "k";
				cmd += " -map 0:0 -f segment -segment_time " + getSegDuration() + " " + output + File.separator;
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_video_%d.mp4";
				//cmd += " -qmin 5 -qmax 60 -crf " + level.getQuality();
			}
			else if (getvCodec()!= "") {
				cmd += " -vcodec " + getvCodec() + " " + getAdditionalFlags();
				cmd += " -g 4 -r 4 -b:v " + level.getMaxRate() + "k -bufsize 10000k -maxrate " + level.getMaxRate() + "k";
			    cmd += " -vf scale=\""+ level.getWidth() +":trunc(ow/a/2)*2\"";
			    cmd += " -map 0:0 -f segment -segment_time " + getSegDuration() + " " + output + File.separator;
			    cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_video_%d.mp4";
			}
			if ((getaCodec() != "") && (!live)) {				
				cmd += " -c:a " + getaCodec();
				cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
				cmd += " -map 0:1 -f segment -segment_time " + getSegDuration() +" " + output + File.separator;
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_audio_%d.mp4";
			}
			else if (getaCodec() != "") {				
				cmd += " -c:a " + getaCodec();
				cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
				cmd += " -map 0:1 -f segment -segment_time " + getSegDuration() +" " + output + File.separator;
				cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_audio_%d.mp4";
			}			
		}
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;		
	}
	
	@Override
	public void processManifest(Transco transco, String title) throws MCASException{
		// mpdModifier = new DashManifestManager(transco.getOutputDir());
		//mpdModifier.processManifest();
	}
	
	@Override
	public List<SimpleEntry<String, Integer>> getUris(URI destination, String title, boolean live) throws MCASException{
		List<SimpleEntry<String, Integer>> uris = new ArrayList<SimpleEntry<String, Integer>>();
		try {
			URI dst = new URI(destination.getScheme(), 
				destination.getHost(), 
				destination.getPath() + "/" + MediaUtils.fileNameMakerByProfile(title, getName()) + "." + this.getFormat().toString(), 
				null);
			uris.add(new SimpleEntry<String, Integer>(dst.toString(),null));
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
		return uris;
	}
	
	@Transient
	@Override
	public FileEventProcessor getFileEP(URI dst, String title) throws MCASException{
		return new DashManifestManager(windowLength, segDuration, dst, getLevels(), this.getName(), title);
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


	public int getWindowLength() {
		return windowLength;
	}


	public void setWindowLength(int windowLength) {
		this.windowLength = windowLength;
	}	
	
}
