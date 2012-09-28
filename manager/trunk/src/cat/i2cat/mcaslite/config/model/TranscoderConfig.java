package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "transcoConfig")
public class TranscoderConfig implements Serializable {
	
	private static final long serialVersionUID = 5142563434573216847L;
	public static final int FFMPEG 		= 1;
	
	private int id;
	private String name;
	private int threads;
	private int transcoder;
	private Set<TProfile> profiles;
	private Set<TLevel> levels;
	
	@Column(name = "name", nullable = false, unique = true, length = 100)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "threads", nullable = false)
	public int getThreads() {
		return threads;
	}
	
	public void setThreads(int threads) {
		this.threads = threads;
	}
	
	@Column(name = "transcoder", nullable = false)
	public int getTranscoder() {
		return transcoder;
	}
	
	public void setTranscoder(int transcoder) {
		this.transcoder = transcoder;
	}
	
	@OneToMany
	@JoinColumn(name="tConfig", referencedColumnName="id")
	public Set<TProfile> getProfiles() {
		return profiles;
	}
	
	public void setProfiles(Set<TProfile> profiles) {
		this.profiles = profiles;
	}
	
	@OneToMany
	@JoinColumn(name="tConfig", referencedColumnName="id")
	public Set<TLevel> getLevels() {
		return levels;
	}
	
	public void setLevels(Set<TLevel> levels) {
		this.levels = levels;
	}

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
