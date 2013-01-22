package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tLiveOptions")
public class TLiveOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false, length = 100)
	private String name;
	@Column(length = 100)
	private String dash_profile;
	@Column
	private int seg_duration;
	@Column
	private int frag_duration;
	@Column
	private int window_length;

	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDash_profile() {
		return dash_profile;
	}
	public void setDash_profile(String dash_profile) {
		this.dash_profile = dash_profile;
	}
	public int getSeg_duration() {
		return seg_duration;
	}
	public void setSeg_duration(int seg_duration) {
		this.seg_duration = seg_duration;
	}
	public int getFrag_duration() {
		return frag_duration;
	}
	public void setFrag_duration(int frag_duration) {
		this.frag_duration = frag_duration;
	}
	public int getWindow_length() {
		return window_length;
	}
	public void setWindow_length(int window_length) {
		this.window_length = window_length;
	}
	
	
}
