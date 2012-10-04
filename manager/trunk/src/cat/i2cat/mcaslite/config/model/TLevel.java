package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tLevels")
public class TLevel implements Serializable{

	private static final long serialVersionUID = -2998112001342903672L;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false, length = 100)
	private String name;
	@Column(nullable = false)
	private int screenx;
	@Column(nullable = false)
	private int screeny;
	@Column(nullable = false)
	private int vBitrate;
	@Column(nullable = false)
	private int aChannels;
	@Column(nullable = false)
	private int aBitrate;
	
	public int getvBitrate() {
		return vBitrate;
	}
	
	public void setvBitrate(int vBitrate) {
		this.vBitrate = vBitrate;
	}

	public int getaChannels() {
		return aChannels;
	}
	
	public void setaChannels(int aChannels) {
		this.aChannels = aChannels;
	}
	
	public int getaBitrate() {
		return aBitrate;
	}
	
	public void setaBitrate(int aBitrate) {
		this.aBitrate = aBitrate;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public int getScreenx() {
		return screenx;
	}

	public void setScreenx(int screenx) {
		this.screenx = screenx;
	}

	public int getScreeny() {
		return screeny;
	}

	public void setScreeny(int screeny) {
		this.screeny = screeny;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
