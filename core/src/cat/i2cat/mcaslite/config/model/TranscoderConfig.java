package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.FileEventProcessor;


@Entity
@Table(name = "transcoConfig")
public class TranscoderConfig implements Serializable {
	
	private static final long serialVersionUID = 5142563434573216847L;
	public static final int FFMPEG 		= 1;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false, length = 100)
	private String name;
	@Column(nullable = false, length = 255)
	private String inputWorkingDir;
	@Column(nullable = false, length = 255)
	private String outputWorkingDir;
	@Column(nullable = false)
	private int timeout;
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="config_profile", joinColumns={@JoinColumn(name="config")}, inverseJoinColumns={@JoinColumn(name="profile")})
	private List<TProfile> profiles;
	@Column
	private boolean live = false;
	
	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
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
	
	public int getTimeout() {
		return timeout;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public List<TProfile> getProfiles() {
		return profiles;
	}
	
	public void setProfiles(List<TProfile> profiles) {
		this.profiles = profiles;
	}

	public String getInputWorkingDir() {
		return inputWorkingDir;
	}

	public void setInputWorkingDir(String inputWorkigDir) {
		this.inputWorkingDir = inputWorkigDir;
	}

	public String getOutputWorkingDir() {
		return outputWorkingDir;
	}

	public void setOutputWorkingDir(String outputWorkigDir) {
		this.outputWorkingDir = outputWorkigDir;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Transient
	public int getNumOutputs(){
		return profiles.size();
	}
	
	@Transient
	public FileEventProcessor getFileEP(URI dst, String profileName, String title) throws MCASException{
		for(TProfile profile : profiles){
			if (profile.getName().equals(profileName)){
				return profile.getFileEP(dst, title);
			}
		}
		throw new MCASException();
	}
	
}
