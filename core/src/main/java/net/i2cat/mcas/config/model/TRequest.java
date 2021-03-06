package net.i2cat.mcas.config.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.FileStatus;
import net.i2cat.mcas.management.LiveStatus;
import net.i2cat.mcas.management.Status;
import net.i2cat.mcas.utils.DefaultsLoader;
import net.i2cat.mcas.utils.RequestUtils;
import net.i2cat.mcas.utils.TranscoderUtils;

@Entity
@Table(name = "requests")
public class TRequest implements Serializable {
	
	private static final long serialVersionUID = 4594851791163577128L;
	
	@Column(nullable = false, length = 255)
	private String src;
	@Column(nullable = false, length = 255)
	private String dst;
	@Column(length = 100)
	private String title;
	@Transient
	private String config;
	@Transient
	private TranscoderConfig tConfig;
	@Id
	private String id = UUID.randomUUID().toString();
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="request", referencedColumnName="id")
	private List<Transco> transcoded;
	@Transient
	private Status status;
	@Column
	private int iStatus;
	
	@Column(nullable = false, length = 1000)
	private String origin;
	@Column
	private Date lastModified;
	@Column 
	private Date dateCreated;
	
	@PreUpdate
	@PrePersist
	public void updateTimeStamps(){
		lastModified = new Date(iStatus);
		if (dateCreated==null){
			dateCreated = new Date(iStatus);
		}
	}
		
    public TRequest(){
        transcoded = new ArrayList<Transco>();
    }
	
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
	
	@Transient
	public boolean isLive(){
		return getTConfig().isLive();
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
	public boolean isCancelled() {
		return (status.getInt() == Status.CANCELLED);
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
		if (getTConfig().isLive()){
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
		if (tConfig == null){
			this.initTConf();
		}
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) throws MCASException {
		if (title == null || title.contains("_")){
			throw new MCASException();
		}
		this.title = title;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}


	public static TRequest getEqualRequest(String id){
		TRequest req = new TRequest();
		req.id = id;
		return req;
	}
	
	public String getOrigin()
	{
		return origin;
	}
	
	public void setOrigin(String callback){
		this.origin = callback;
	}

	@Override 
	public boolean equals(Object o){
		TRequest req = (TRequest) o;
		return this.id.equals(req.getId());
	}
	
	@Transient
	public void initTConf() {
		try {
			setTConfig(TranscoderUtils.loadConfig(config));
		} catch (MCASException e){
			try {
				setTConfig(TranscoderUtils.loadConfig(DefaultsLoader.DEFAULT));
			} catch (MCASException e1) {
				e1.printStackTrace();
			}
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

	@Transient
	public void callback() {
		try {
			RequestUtils.callback(this);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public List<String> getUris() throws MCASException{
		List<String> uris = new ArrayList<String>();
		try {
			for (TProfile profile : this.getTConfig().getProfiles()){
				uris.addAll(profile.getUris(new URI(dst), getTitle(), this.isLive()));
			}
		} catch (URISyntaxException e){
			throw new MCASException();
		}
		return uris;
	}
}
