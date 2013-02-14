package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.net.URI;
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
	 public List<Transco> commandBuilder(String input, String output, String dst) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "ffmpeg -i " + input;
		for (TLevel level : getLevels()){
			cmd += " -vf scale="+ level.getWidth() +":-1";
			cmd += " -maxrate " + level.getMaxRate() + " -g 30"; 
			cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
			cmd += " -c:v " + getvCodec() + " -c:a " + getaCodec() + " " + getAdditionalFlags();
			cmd += " -f segment -segment_time_delta 0.03";
			cmd += " -segment_time " + getSegDuration() + " " + output + "_" + level.getName() + "_%d.ts";
		}
		transcos.add(new Transco(cmd, (new File(output)).getParent(), dst, input));
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
	public FileEventProcessor getFileEP(URI dst) throws MCASException{
		return new HLSManifestManager(windowLength, segDuration, dst, getLevels());
	}
}
