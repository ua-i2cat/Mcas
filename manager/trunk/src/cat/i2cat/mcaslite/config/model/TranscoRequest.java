package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

@Entity
@Table(name = "requests")
public class TranscoRequest implements Serializable{

	private static final long serialVersionUID = 4636926585290770053L;

	public enum State {
		CREATED(1, "CREATED"){
			public State next(){return M_QUEUED;}
		}, 
		M_QUEUED(2, "M_QUEUED"){
			public State next(){return M_PROCESS;}
		}, 
		M_PROCESS(3, "M_PROCESS"){
			public State next(){return T_QUEUED;}
		}, 
		T_QUEUED(4, "T_QUEUED"){
			public State next(){return T_PROCESS;}
		}, 
		T_PROCESS(5, "T_PROCESS"){
			public State next(){return T_TRANSCODED;}
		}, 
		T_TRANSCODED(6, "T_TRANSCODED"){
			public State next(){return MOVING;}
		}, 
		MOVING(7, "MOVING"){
			public State next(){return DONE;}
		},
		DONE(8, "DONE"){
			public State next() throws MCASException{throw new MCASException();}
		},
		CANCELLED(50, "CANCELLED"){
			public State next() throws MCASException{throw new MCASException();}
		},
		PARTIAL_ERROR(101, "PARTIAL_ERROR"){
			public State next() throws MCASException{throw new MCASException();}
		},
		ERROR(100, "ERROR"){
			public State next() throws MCASException{throw new MCASException();}
		};

		private final int id;
		private final String name;
		
		public abstract State next() throws MCASException;
		
		State(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public int getId(){
			return this.id;
		}
		
		public String getName(){
			return this.name;
		}
	}

	@Column(nullable = false, length = 255)
	private String src;
	@Column(nullable = false, length = 255)
	private String dst;
	@Column(length = 100)
	private String usr;
	@Transient
	private String config;
	//TODO: switch to optional true and fetch eager in order to be able to delete configurations safely
	@ManyToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="tConfig", referencedColumnName="id")
	private TranscoderConfig tConfig;
	@Id
	@Type(type="uuid-char")
	private UUID id = UUID.randomUUID();
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="request", referencedColumnName="id")
	private List<Transco> transcoded = new ArrayList<Transco>();
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private State state = State.CREATED;
			
	public void addTrancoded(Transco transco){
		transcoded.add(transco);
	}
	
	public void deleteTranscoded(Transco transco){
		transcoded.remove(transco);
	}
	
	@Transient
	public boolean isTranscodedEmpty(){
		return transcoded.isEmpty();
	}
	
	public List<Transco> getTranscoded(){
		return transcoded;
	}
	
	public void setTranscoded(List<Transco> transcoded){
		this.transcoded = transcoded;
	}
	
	@Transient
	public int getNumOutputs() {
		return tConfig.getNumOutputs();
	}

	public void increaseState() throws MCASException {
		state = state.next();
	}
	
	@Transient
	public void setCancelled() {
		state = State.CANCELLED;
	}
	
	@Transient
	public void setError() {
		state = State.ERROR;
	}
	
	@Transient
	public void setPartialError() {
		state = State.PARTIAL_ERROR;
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDst() {
		return dst;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}

	public TranscoderConfig getTConfig() {
		return tConfig;
	}

	public void setTConfig(TranscoderConfig config) {
		this.tConfig = config;
	}
	
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getUsr() {
		return usr;
	}

	public void setUsr(String usr) {
		this.usr = usr;
	}

	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id){
		this.id = id;
	}

	@Transient
	public String getIdStr() {
		return id.toString();
	}
	
	public static TranscoRequest getEqualRequest(UUID id){
		TranscoRequest req = new TranscoRequest();
		req.id = id;
		return req;
	}

	@Override 
	public boolean equals(Object o){
		TranscoRequest req = (TranscoRequest) o;
		return this.id.equals(req.getId());
	}
	
	@Transient
	public void initTConf() throws MCASException{
		setTConfig(TranscoderUtils.loadConfig(config));
	}
}
