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
import cat.i2cat.mcaslite.utils.MediaUtils;

@Entity
@DiscriminatorValue("RTMP")
public class TRTMPOptions extends TProfile {

	private static final long serialVersionUID = 1L;
	
	@Column
	private String domain;
	@Column
	private String application;
	
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
		for (TLevel level : levels){
			cmd += (getGop() > 0 ? " -g " + getGop() : "");
			cmd += (getFps() > 0 ? " -r " + getFps() : "");
			cmd += (level.getWidth() > 0 ? " -vf scale=\"" + level.getWidth() + ":trunc(ow/a/2)*2\"" : "");
			cmd += " -b:v " + level.getMaxRate() + "k";
			cmd += " -maxrate " + level.getMaxRate() + "k" + " -qmin 5 -qmax 60 -crf " + level.getQuality();
			cmd += " -ac " + level.getaChannels() + "k -b:a " + level.getaBitrate() + "k ";
			cmd += getAdditionalFlags() + " -c:v " + getvCodec() + " -c:a " + getaCodec() + " -f " + getFormat();
			if (! live){
				cmd += " -y " + output + File.separator + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "." + getFormat();
			} else {
				cmd += " \"rtmp://\"" + getDomain() + "/" + getApplication() + "/" + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName());
			}
		}
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;
	}
	
	@Override
	public List<String> getUris(URI destination, String title, boolean live) throws MCASException{
		if (live){
			List<String> uris = new ArrayList<String>();
			try {
				for (TLevel level : this.getLevels()){
					URI dst = new URI("rtmp", 
							getDomain(), "/" + getApplication() + "/" + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()), 
							null);
					uris.add(dst.toString());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new MCASException();
			}
			return uris;
		} else {
			return super.getUris(destination, title, live);
		}
	}

}
