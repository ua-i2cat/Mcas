package cat.i2cat.mcaslite.config.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "association")
public class TProjectTLevelAssociation {

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
	@JoinColumn(name = "profile")
	private TProfile profile;
	
	@ManyToMany
	@JoinTable(name="association_level", joinColumns={@JoinColumn(name="association")}, inverseJoinColumns={@JoinColumn(name="level")})
	private List<TLevel> levels;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TProfile getProfile() {
		return profile;
	}

	public void setProfile(TProfile profile) {
		this.profile = profile;
	}

	public List<TLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<TLevel> levels) {
		this.levels = levels;
	}
}
