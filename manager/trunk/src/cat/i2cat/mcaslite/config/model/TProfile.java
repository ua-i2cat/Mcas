package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tProfiles")
public class TProfile implements Serializable{

	private static final long serialVersionUID = 4031066984726638669L;
	private int id;
	private int tConfig;
	private String name; 
	private String format;
	private String vCodec;
	private String aCodec;
	
	@Column(name = "format", nullable = false, length = 100)
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	@Column(name = "vCodec", nullable = false, length = 100)
	public String getvCodec() {
		return vCodec;
	}
	
	public void setvCodec(String vCodec) {
		this.vCodec = vCodec;
	}
	
	@Column(name = "acodec", nullable = false, length = 100)
	public String getaCodec() {
		return aCodec;
	}
	
	public void setaCodec(String aCodec) {
		this.aCodec = aCodec;
	}
	
	@Column(name = "name", nullable = false, length = 100)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
