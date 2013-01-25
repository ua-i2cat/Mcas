package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

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
			cmd += " -vf scale="+ level.getWidth() +":-1" + " -qmin " + level.getQuality() + " -qmax " + level.getQuality() + " -ac "; 
			cmd += level.getaChannels() + " -b:a " + level.getaBitrate() + "k " + " -f " + getFormat() + " ";
			cmd += getAdditionalFlags() + " -codec:v " + getvCodec() + " -codec:a " + getaCodec();
			cmd += " -y - | m3u8-segmenter -i - -d " + getSegDuration() + " -p " + output + "_" + level.getName(); 
			cmd += " -m " + output + "_" + level.getName() + ".m3u8 -u '' -n " + getWindowLength();
			cmd = "/bin/sh -c '" + cmd.replaceAll("'", "'\''") + "'";
			transcos.add(new Transco(cmd, (new File(output)).getParent(), 
					TranscoderUtils.pathToUri((new File(dst)).getParent()), input));
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
}
