package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import cat.i2cat.mcaslite.exceptions.MCASException;

@Entity
@Table(name = "tProfiles")
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
	
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="tProfiles", referencedColumnName="id")
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
	
	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public List<Transco> transcoBuilder(String srcUri, String id, String dstUri) throws MCASException{
		//return videoOnDemand(config, id, dstUri);
	}
}
