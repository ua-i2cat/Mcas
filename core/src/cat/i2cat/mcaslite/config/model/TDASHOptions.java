package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.DashManifestManager;
import cat.i2cat.mcaslite.management.FileEventProcessor;
import cat.i2cat.mcaslite.utils.MediaUtils;

@Entity
@DiscriminatorValue("Dash")

public class TDASHOptions extends TProfile {

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
	public List<Transco> commandBuilder(String input, String output, boolean live, String title) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		String COMMAND = "script.sh ";
		String inp = "-y -i " + input + " -threads 0 ";
		String pro = "-c:v " + getvCodec() + " -c:a " + getaCodec() + " -f mp4 ";
		String nam = MediaUtils.fileNameMakerByProfile(title, getName()) + "_level ";
		String mp4 = "-dash " + this.segDuration + " -frag " + this.fragDuration + " -rap -frag-rap -dash-profile main -segment-name %s_ " + " -out " + output + File.separator + MediaUtils.fileNameMakerByProfile(title, getName()) + "." + this.getFormat();
		String out = File.separator + output + File.separator;
		String OUTPUT = "\"" + out + "\" ";
		String INPUT = "\"" + inp + "\" ";
		String PROFILE = "\"" + pro + "\" ";
		String NUMLVL = "\"" + levels.size() + "\" ";
		String MP4BOX = "\"" + mp4 + "\" ";
		String NAME = "\"" + nam + "\" ";
		
		String LEVELS = "";
		for (int i = 0; i<levels.size(); i++){
			TLevel level = levels.get(i);
			LEVELS += " -vf scale=\"" + level.getWidth() + ":trunc(ow/a/2)*2\"" + " -b:v " + level.getMaxRate();
			LEVELS += "k -bufsize 10000k -maxrate " + level.getMaxRate() + "k" + " -qmin 5 -qmax 60 -crf " + level.getQuality();
			LEVELS += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k " + getAdditionalFlags();
			LEVELS += " -y " + output + File.separator + i + ".mp4 ";
		}
		String cmd = COMMAND + INPUT + PROFILE + NUMLVL + OUTPUT + MP4BOX + NAME + "\"" + LEVELS + "\"";
		System.out.println(cmd);
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;		
	}
	
	@Override
	public void processManifest(Transco transco, String title) throws MCASException{
		DashManifestManager mpdModifier = new DashManifestManager(transco.getOutputDir());
		mpdModifier.processManifest();
	}
	
	@Override
	public List<String> getUris(URI destination, String title, boolean live) throws MCASException{
		List<String> uris = new ArrayList<String>();
		try {
			URI dst = new URI(destination.getScheme(), 
				destination.getHost(), 
				destination.getPath() + "/" + MediaUtils.fileNameMakerByProfile(title, getName()) + "." + this.getFormat().toString(), 
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
	public FileEventProcessor getFileEP(URI dst, String title){
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
