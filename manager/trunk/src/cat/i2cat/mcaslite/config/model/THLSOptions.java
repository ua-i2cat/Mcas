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
		for (TLevel level : getLevels()){
			String cmd = "ffmpeg -i " + input;
			cmd += " -vf scale="+ level.getWidth() +":-1" + " -qmin " + level.getQuality() + " -qmax " + level.getQuality(); 
			cmd += " -ac " + level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
			cmd += " -codec:v " + getvCodec() + " -codec:a " + getaCodec() + " " + getAdditionalFlags();
			cmd += " -f segment -segment_list_flags live -segment_list " + output + "_" + level.getName() + ".csv";
			cmd += " -segment_time " + getSegDuration() + " " + output + "_" + level.getName() + "_%d.ts";
			transcos.add(new Transco(cmd, (new File(output)).getParent(), dst, input));
		}
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
	public FileEventProcessor getFileEP(URI dst){
		return new HLSManifestManager(windowLength, segDuration, dst);
	}
}
