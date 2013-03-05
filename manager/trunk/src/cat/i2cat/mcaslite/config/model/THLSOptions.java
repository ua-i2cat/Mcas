package cat.i2cat.mcaslite.config.model;

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

@Entity
@DiscriminatorValue("tHLSOptions")
public class THLSOptions extends TProfile {

	private static final long serialVersionUID = 1L;

	@Column
	private int segDuration;
	@Column
	private int windowLength;

	
	@Override
	 public List<Transco> commandBuilder(String input, String output) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "ffmpeg -re -analyzeduration 10 -i " + input;
		for (TLevel level : getLevels()){
			cmd += " -vf scale="+ level.getWidth() +":-1";
			cmd += " -g 50 -r 25 -qmin " + level.getQuality() + " -qmax " + level.getQuality();
			cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
			cmd += " -c:v " + getvCodec() + " -c:a " + getaCodec() + " " + getAdditionalFlags();
			cmd += " -f segment -segment_time_delta 0.03";
			cmd += " -segment_time " + getSegDuration() + " " + output + "/" + this.getName() + "_" + level.getName() + "_%d.ts";
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
	
//	@Transient
//	public double getTimeDelta() {
//		return this.segDuration*0.05;
//	}
	
	public void setWindowLength(int windowLength) {
		this.windowLength = windowLength;
	}
	
	@Override
	public List<String> getUris(URI destination) throws MCASException{
		List<String> uris = new ArrayList<String>();
		try {
			URI dst = new URI(destination.getScheme(), 
				destination.getHost(), 
				Paths.get(destination.getPath(), this.getName() + "." + this.getFormat()).toString(), 
				null);
			uris.add(dst.toString());
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
		return uris;
	}
	
	@Override
	public FileEventProcessor getFileEP(URI dst) throws MCASException{
		return new HLSManifestManager(windowLength, segDuration, dst, getLevels(), this.getName());
	}
}
