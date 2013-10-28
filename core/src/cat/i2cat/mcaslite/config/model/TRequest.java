package cat.i2cat.mcaslite.config.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;

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

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.FileStatus;
import cat.i2cat.mcaslite.management.LiveStatus;
import cat.i2cat.mcaslite.management.ProcessQueue;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.utils.DefaultsLoader;
import cat.i2cat.mcaslite.utils.RequestUtils;
import cat.i2cat.mcaslite.utils.TranscoderUtils;

@Entity
@Table(name = "requests")
public class TRequest {
	
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
	private List<Transco> transcos;
	@Transient
	private Status status;
	@Column
	private int iStatus;
	
	@Column(nullable = true, length = 1000)
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
        transcos = new ArrayList<Transco>();
    }
	
	synchronized public void setIStatus(int iStatus){
		this.iStatus = iStatus;
	}
	
	synchronized public int getIStatus(){
		return iStatus;
	}
			
	@Transient
	synchronized public boolean isTranscodedEmpty(){
		return transcos.isEmpty();
	}
	
	@Transient
	synchronized public boolean isLive(){
		return getTConfig().isLive();
	}
	
	synchronized public List<Transco> getTranscos(){
		return transcos;
	}
	
	synchronized public void setTranscos(List<Transco> transcos){
		this.transcos = transcos;
	}
	
	synchronized public void increaseStatus(boolean callback) throws MCASException {
		status.increaseStatus();
		iStatus = status.getInt();
		if (callback){
			callback();
		}
	}
	
	@Transient
	synchronized public void setCancelled() {
		status.setCancelled();
		iStatus = status.getInt();
		callback();
	}
	
	@Transient
	synchronized public boolean isCancelled() {
		return (status.getInt() == Status.CANCELLED);
	}
	
	@Transient
	synchronized public void setError() {
		status.setError();
		iStatus = status.getInt();
		callback();
	}
	
	@Transient
	synchronized public void setPartialError() {
		status.setPartialError();
		iStatus = status.getInt();
		callback();
	}

	synchronized public Status getStatus() {
		return status.getStatus();
	}
	
	synchronized public void setStatus(Status status) {
		this.status = status;
		iStatus = status.getInt();
	}
	
	@Transient
	synchronized public void initRequest(){
		if (getTConfig().isLive()){
			status = new LiveStatus();
		} else {
			status = new FileStatus();
		}
	}
	
	synchronized public String getSrc() {
		return src;
	}

	synchronized public void setSrc(String src) {
		this.src = src;
	}

	synchronized public String getDst() {
		return dst;
	}

	synchronized public void setDst(String dst) {
		this.dst = dst;
	}

	synchronized public TranscoderConfig getTConfig() {
		if (tConfig == null){
			this.initTConf();
		}
		return tConfig;
	}

	synchronized public void setTConfig(TranscoderConfig config) {
		this.tConfig = config;
	}
	
	synchronized public String getConfig() {
		return config;
	}

	synchronized public void setConfig(String config) {
		this.config = config;
	}

	synchronized public String getTitle() {
		return title;
	}

	synchronized public void setTitle(String title) throws MCASException {
		if (title == null || title.contains("_")){
			throw new MCASException();
		}
		this.title = title;
	}

	synchronized public String getId() {
		return id;
	}
	
	synchronized public void setId(String id){
		this.id = id;
	}


	synchronized public static TRequest getEqualRequest(String id){
		TRequest req = new TRequest();
		req.id = id;
		return req;
	}
	
	synchronized public String getOrigin()
	{
		return origin;
	}
	
	synchronized public void setOrigin(String callback){
		this.origin = callback;
	}

	@Override 
	synchronized public boolean equals(Object o){
		TRequest req = (TRequest) o;
		return this.id.equals(req.getId());
	}
	
	@Transient
	synchronized public void initTConf() {
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
	synchronized public boolean isWaiting() {
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
	private void callback() {
		try {
			RequestUtils.callback(this);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public List<SimpleEntry<String, Integer>> getUris() throws MCASException{
		List<SimpleEntry<String, Integer>> uris = new ArrayList<SimpleEntry<String, Integer>>();
		try {
			for (TProfile profile : this.getTConfig().getProfiles()){
				Transco transco = getTranscoByProfile(profile.getName());
				if (transco != null && transco.getStatus().isDone()){
					uris.addAll(profile.getUris(new URI(dst), getTitle(), this.isLive()));
				}
			}
		} catch (URISyntaxException e){
			throw new MCASException();
		}
		return uris;
	}
	
	@Transient
	private Transco getTranscoByProfile(String profileName){
		for (Transco transco : transcos){
			if(transco.getProfileName().equals(profileName)){
				return transco;
			}
		}
		return null;
	}

	@Transient
	public Transco getSingleTransco(int transco) {
		return transcos.get(transco);
	}
	
	@Transient
	synchronized public void setTranscoStatus(Transco transco, int status) {
		try {
			transcos.get(transcos.indexOf(transco)).setStatus(status);
		} catch (MCASException e) {
			e.printStackTrace();
		}
	}
	
	@Transient
	synchronized public void updateStatus() {
		boolean cancelled = false;
		boolean done = false;
		boolean error = false;
		for (Transco transco : transcos){
			if (transco.getStatus().hasNext()){
				return;
			} else {
				cancelled = cancelled || transco.getStatus().isCancelled();
				done = done || transco.getStatus().isDone();
				error = error || transco.getStatus().isError();
			}
		}
		if (! error && ! cancelled){
			try {
				increaseStatus(true);
			} catch (MCASException e) {
				e.printStackTrace();
				setError();
			}
		} else if (! cancelled && error && done) {
			setPartialError();
		} else if (cancelled){
			setCancelled();
		} else {
			setError();
		}
		DAO<TRequest> requestDao = new DAO<TRequest>(TRequest.class);
		if (ProcessQueue.getInstance().remove(this)){
			requestDao.save(this);
		}
	}
}
