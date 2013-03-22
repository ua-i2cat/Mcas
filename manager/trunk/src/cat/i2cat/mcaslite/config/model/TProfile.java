package cat.i2cat.mcaslite.config.model;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.FileEventProcessor;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.utils.RequestUtils;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type", 
    discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("Default")
public class TProfile implements Serializable{

	private static final long serialVersionUID = 4031066984726638669L;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false, length = 100)
	private String name;
	@Column(nullable = false, length = 100)
	private String format;
	@Column(nullable = false, length = 100)
	private String vCodec;
	@Column(nullable = false, length = 100)
	private String aCodec;
	@Column(nullable = false, length = 100)
	private String additionalFlags;
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="profile_level", joinColumns={@JoinColumn(name="profile")}, inverseJoinColumns={@JoinColumn(name="level")})
	private List<TLevel> levels;
	
	public List<TLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<TLevel> levels) {
		this.levels = levels;
	}

	public String getAdditionalFlags() {
		return additionalFlags;
	}

	public void setAdditionalFlags(String additionalFlags) {
		this.additionalFlags = additionalFlags;
	}

	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getvCodec() {
		return vCodec;
	}
	
	public void setvCodec(String vCodec) {
		this.vCodec = vCodec;
	}
	
	public String getaCodec() {
		return aCodec;
	}
	
	public void setaCodec(String aCodec) {
		this.aCodec = aCodec;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) throws MCASException{
		if (name == null || name.contains("_")){
			throw new MCASException();
		}
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public List<Transco> commandBuilder(String input, String output, boolean live, String title) throws MCASException{
		List<Transco> transcos = new ArrayList<Transco>();
		String cmd = "ffmpeg -i " + input;
		for (TLevel level : levels){
			cmd += " -vf scale=\"" + level.getWidth() + ":trunc(ow/a/2)*2\"" + " -b:v " + level.getMaxRate();
			cmd += "k -bufsize 10000k -maxrate " + level.getMaxRate() + "k" + " -qmin 5 -qmax 60 -crf " + level.getQuality();
			cmd += " -ac " + level.getaChannels() + "k -b:a " + level.getaBitrate() + "k ";
			cmd += getAdditionalFlags() + " -c:v " + getvCodec() + " -c:a " + getaCodec() + " -f " + getFormat();
			cmd += " -y " + output + File.separator + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "." + getFormat();
		}
		transcos.add(new Transco(cmd, output, input, this.getName()));
		return transcos;
	}
	
	public List<String> getUris(URI destination, String title) throws MCASException{
		List<String> uris = new ArrayList<String>();
		try {
			for (TLevel level : this.getLevels()){
				URI dst = new URI(destination.getScheme(), 
						destination.getHost(), 
						destination.getPath() + RequestUtils.URIseparator + MediaUtils.fileNameMakerByLevel(title, getName(), level.getName()) + "." + this.getFormat(), 
						null);
				uris.add(dst.toString());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
		return uris;
	}
	
	public void processManifest(Transco transco, String title) throws MCASException{
		
	}
	
	public FileEventProcessor getFileEP(URI dst, String title) throws MCASException{
		return null;
	}
}
