package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tLevels")
public class TLevel implements Serializable{

	private static final long serialVersionUID = -2998112001342903672L;
	private int id;
	private String name;
	private int screenx;
	private int screeny;
	private int vBitrate;
	private int aChannels;
	private int aBitrate;
	private int tConfig;
	
	@Column(name = "vBitrate", nullable = false)
	public int getvBitrate() {
		return vBitrate;
	}
	
	public void setvBitrate(int vBitrate) {
		this.vBitrate = vBitrate;
	}
	
	@Column(name = "aChannels", nullable = false)
	public int getaChannels() {
		return aChannels;
	}
	
	public void setaChannels(int aChannels) {
		this.aChannels = aChannels;
	}
	
	@Column(name = "aBitrate", nullable = false)
	public int getaBitrate() {
		return aBitrate;
	}
	
	public void setaBitrate(int aBitrate) {
		this.aBitrate = aBitrate;
	}
	
	@Column(name = "name", nullable = false, length = 100)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "screenx", nullable = false)
	public int getScreenx() {
		return screenx;
	}

	public void setScreenx(int screenx) {
		this.screenx = screenx;
	}

	@Column(name = "screeny", nullable = false)
	public int getScreeny() {
		return screeny;
	}

	public void setScreeny(int screeny) {
		this.screeny = screeny;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "tConfig", nullable = false)
	public int gettConfig() {
		return tConfig;
	}

	public void settConfig(int tConfig) {
		this.tConfig = tConfig;
	}
}
