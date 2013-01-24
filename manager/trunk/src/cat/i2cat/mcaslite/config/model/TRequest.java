package cat.i2cat.mcaslite.config.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.FileStatus;
import cat.i2cat.mcaslite.management.LiveStatus;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.utils.DefaultsUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

@Entity
@Table(name = "requests")
public class TRequest implements Serializable {
	
	private static final long serialVersionUID = 4594851791163577128L;
	
	@Column(nullable = false, length = 255)
	private String src;
	@Column(nullable = false, length = 255)
	private String dst;
	@Column(length = 100)
	private String usr;
	@Transient
	private String config;
	@ManyToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="tConfig", referencedColumnName="id")
	private TranscoderConfig tConfig;
	@Id
	@Type(type="uuid-char")
	private UUID id = UUID.randomUUID();
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="request", referencedColumnName="id")
	private List<Transco> transcoded = new ArrayList<Transco>();
	@Transient
	private Status status;
	@Column
	private int iStatus;

	public void setIStatus(int iStatus){
		this.iStatus = iStatus;
	}
	
	public int getIStatus(){
		return iStatus;
	}
			
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

	public void increaseStatus() throws MCASException {
		status.increaseStatus();
		iStatus = status.getInt();
	}
	
	@Transient
	public void setCancelled() {
		status.setCancelled();
		iStatus = status.getInt();
	}
	
	@Transient
	public void setError() {
		status.setError();
		iStatus = status.getInt();
	}
	
	@Transient
	public void setPartialError() {
		status.setPartialError();
		iStatus = status.getInt();
	}

	public Status getStatus() {
		return status.getStatus();
	}
	
	public void setStatus(Status status) {
		this.status = status;
		iStatus = status.getInt();
	}
	
	@Transient
	public void initRequest(){
		if (this.isLive()){
			status = new LiveStatus();
		} else {
			status = new FileStatus();
		}
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
		try {
			if (tConfig == null){
				this.initTConf();
			}
			return tConfig;
		} catch (MCASException e) {
			return null;
		}
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
	
	public static TRequest getEqualRequest(UUID id){
		TRequest req = new TRequest();
		req.id = id;
		return req;
	}

	@Override 
	public boolean equals(Object o){
		TRequest req = (TRequest) o;
		return this.id.equals(req.getId());
	}
	
	@Transient
	public void initTConf() throws MCASException {
		try {
			setTConfig(TranscoderUtils.loadConfig(config));
		} catch (MCASException e){
			setTConfig(TranscoderUtils.loadConfig(DefaultsUtils.DEFAULT));
		}
	}

	@Transient
	public boolean isWaiting() {
		if (status.getInt() <= Status.QUEUED){
			return true;
		}
		return false;
	}

	@Transient
	public boolean isProcessing() {
		if (status.getInt() > Status.QUEUED && status.getInt() < Status.DONE){
			return true;
		}
		return false;
	}
}
