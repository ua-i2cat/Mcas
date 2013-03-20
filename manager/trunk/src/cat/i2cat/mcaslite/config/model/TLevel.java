package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import cat.i2cat.mcaslite.exceptions.MCASException;

@Entity
@Table(name = "tLevels")
public class TLevel implements Serializable{

	private static final long serialVersionUID = -2998112001342903672L;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false, length = 100)
	private String name;
	@Column(nullable = false)
	private int width;
	@Column(nullable = false)
	private int quality;
	@Column(nullable = false)
	private int aChannels;
	@Column(nullable = false)
	private int aBitrate;
	@Column(nullable = false)
	private int maxRate;
	
	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) throws MCASException {
		if (quality > 50 || quality < 15){
			throw new MCASException();
		}
		this.quality = quality;
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
	
	public void setName(String name) throws MCASException{
		if (name.contains("_")){
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
	
}
