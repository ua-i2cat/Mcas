package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
@DiscriminatorValue("HLS")
public class THLSOptions extends TProfile {

	private static final long serialVersionUID = 1L;

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
			cmd += (getGop() > 0 ? " -g " + getGop() : "");
			cmd += (getFps() > 0 ? " -r " + getFps() : "");
			cmd += (level.getWidth() > 0 ? " -vf scale=\"" + level.getWidth() + ":trunc(ow/a/2)*2\"" : "");
			cmd += " -b:v " + level.getMaxRate() + "k -maxrate " + level.getMaxRate() + "k";
			cmd += " -qmin 5 -qmax 60 -crf " + level.getQuality();
			cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
			cmd += " -c:v " + getvCodec() + " -c:a " + getaCodec() + " " + getAdditionalFlags();
			cmd += " -f segment -segment_time_delta 0.03";
			if (! live){
				cmd += " -segment_list "+ output + File.separator + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + ".csv";
			}
			cmd += " -segment_time " + getSegDuration() + " " + output + File.separator;
			cmd += MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "_%d.ts";
		}
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;
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
	
	@Override
	public FileEventProcessor getFileEP(URI dst, String title) throws MCASException{
		return new HLSManifestManager(windowLength, segDuration, dst, getLevels(), this.getName(), title);
	}
	
	@Override
	public void processManifest(Transco transco, String title) throws MCASException{
		HLSManifestManager HLSMngr = new HLSManifestManager(0, segDuration, Paths.get(transco.getOutputDir()).toUri(), getLevels(), this.getName(), title);
		try {
			HLSMngr.createMainManifest();
			HLSMngr.createLevelManifests(transco.getOutputDir());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
}
