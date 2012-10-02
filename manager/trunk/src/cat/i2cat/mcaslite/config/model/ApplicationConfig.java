package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "applicationConfig")
public class ApplicationConfig implements Serializable {
	
	private static final long serialVersionUID = 5947611381361764581L;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(nullable = false, unique = true, length = 100)
	private String name;
	@Column(nullable = false)
	private int maxTransco;
	@Column(nullable = false)
	private int maxInMediaH;
	@Column(nullable = false)
	private int maxOutMediaH;
		
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
	
	public int getMaxTransco() {
		return maxTransco;
	}
	
	public void setMaxTransco(int maxTransco) {
		this.maxTransco = maxTransco;
	}
	
	public int getMaxInMediaH() {
		return maxInMediaH;
	}
	
	public void setMaxInMediaH(int maxInMediaH) {
		this.maxInMediaH = maxInMediaH;
	}
	
	public int getMaxOutMediaH() {
		return maxOutMediaH;
	}
	
	public void setMaxOutMediaH(int maxOutMediaH) {
		this.maxOutMediaH = maxOutMediaH;
	}
}
