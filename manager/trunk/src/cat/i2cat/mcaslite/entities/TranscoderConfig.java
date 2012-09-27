package cat.i2cat.mcaslite.entities;

import java.util.List;

public class TranscoderConfig {
	
	public static final int FFMPEG 		= 1;
	
	private String name;
	private int threads;
	private int transcoder;
	private List<TProfile> profiles;
	private List<TLevel> levels;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getTranscoder() {
		return transcoder;
	}
	public void setTranscoder(int transcoder) {
		this.transcoder = transcoder;
	}
	public List<TProfile> getProfiles() {
		return profiles;
	}
	public void setProfiles(List<TProfile> profiles) {
		this.profiles = profiles;
	}
	public List<TLevel> getLevels() {
		return levels;
	}
	public void setLevels(List<TLevel> levels) {
		this.levels = levels;
	}

}
