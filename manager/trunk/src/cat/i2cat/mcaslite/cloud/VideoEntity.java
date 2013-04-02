package cat.i2cat.mcaslite.cloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.RequestUtils;

import com.microsoft.windowsazure.services.queue.client.CloudQueueMessage;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class VideoEntity extends TableServiceEntity {
	
	private String title; 
	private String fileName; 
	private String uniqueFileName; 
	private String description; 
	private String videoUploadedUrl; 
	private String videoConvertedUrlMp4; 
	private String videoConvertedUrlWebM; 
	private String tenantContainer; 
	private String category; 
	private String status; 
	private String cancelId; 
	private Date startConvertion; 
	private Date endConvertion; 
	private String durationConvertion; 
	private Date startJob; 
	private Date endJob; 
	private String durationJob; 
	private boolean urlEntities;

    
	private String scheme;
    private String host;
    
    public VideoEntity() {
    	urlEntities = false;
    }
    
    public VideoEntity(String partitionKey, String rowKey) {
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }
    
    public boolean getUrlEntities() throws MCASException{
    	return urlEntities;
    }

	public void setUrlEntities(boolean hasUrlEntities) throws MCASException {
		urlEntities = hasUrlEntities;
	}
    
	public String getUniqueFileName() {
		return uniqueFileName;
	}

	public void setUniqueFileName(String uniqueFileName) {
		this.uniqueFileName = uniqueFileName;
	}

	public Date getStartConvertion() {
		return startConvertion;
	}

	public void setStartConvertion(Date startConvertion) {
		this.startConvertion = startConvertion;
	}

	public Date getEndConvertion() {
		return endConvertion;
	}

	public void setEndConvertion(Date endConvertion) {
		this.endConvertion = endConvertion;
	}

	public String getDurationConvertion() {
		return durationConvertion;
	}

	public void setDurationConvertion(String durationConvertion) {
		this.durationConvertion = durationConvertion;
	}

	public Date getStartJob() {
		return startJob;
	}

	public void setStartJob(Date startJob) {
		this.startJob = startJob;
	}

	public Date getEndJob() {
		return endJob;
	}

	public void setEndJob(Date endJob) {
		this.endJob = endJob;
	}

	public String getDurationJob() {
		return durationJob;
	}

	public void setDurationJob(String durationJob) {
		this.durationJob = durationJob;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVideoUploadedUrl() {
		return videoUploadedUrl;
	}

	public void setVideoUploadedUrl(String videoUploadedUrl) throws MCASException {
		try {
			URI uri = new URI(videoUploadedUrl);
			this.scheme = uri.getScheme();
			this.host = uri.getHost();
			this.videoUploadedUrl = videoUploadedUrl;
		} catch (URISyntaxException e){
			this.videoUploadedUrl = videoUploadedUrl;
		}
	}

	public String getVideoConvertedUrlMp4() {
		return videoConvertedUrlMp4;
	}

	public void setVideoConvertedUrlMp4(String videoConvertedUrlMp4) {
		this.videoConvertedUrlMp4 = videoConvertedUrlMp4;
	}

	public String getVideoConvertedUrlWebM() {
		return videoConvertedUrlWebM;
	}

	public void setVideoConvertedUrlWebM(String videoConvertedUrlWebM) {
		this.videoConvertedUrlWebM = videoConvertedUrlWebM;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCancelId() {
		return cancelId;
	}

	public void setCancelId(String cancelId) {
		this.cancelId = cancelId;
	}

	public String getTenantContainer() {
		return tenantContainer;
	}

	public void setTenantContainer(String tenantContainer) {
		this.tenantContainer = tenantContainer;
	}

    public TRequest toRequest() throws MCASException{
    	try {
	    	TRequest request = new TRequest();
	    	request.setConfig("default");
			request.setSrc(uriToBlob(new URI(videoUploadedUrl)));
			request.setDst((new URI("blob", getHost(), RequestUtils.URIseparator + tenantContainer, null)).toString());
			request.setTitle(uniqueFileName);
			return request;
    	} catch (URISyntaxException e) {
    		throw new MCASException();
    	}
    }
    
    public void updateFromRequest(TRequest request) throws MCASException{
    	this.status = request.getStatus().toString();
    	this.cancelId = request.getId();
    	AzureUtils.insertEntities(URLEntity.class.getSimpleName(), urlEntitiesFromRequest(request));
    	this.videoConvertedUrlMp4 = getVideoBySuffix(request, ".mp4");
    	this.videoConvertedUrlWebM = getVideoBySuffix(request, ".webm");	
    }
    
    private List<URLEntity> urlEntitiesFromRequest (TRequest request) throws MCASException{
    	List<URLEntity> urlEntities = new ArrayList<URLEntity>();
	    	if (request != null && (request.getStatus().getInt()==Status.DONE || request.getStatus().getInt()==Status.P_ERROR)) {
		    	try {
			    	List<String> uris = request.getUris();
			    	for (String uriStr : uris){
			    		URLEntity urlEntity = new URLEntity(new Date().toString(), UUID.randomUUID().toString());
			    		URI uri = new URI(uriStr);
			    		urlEntity.setUrl(new URI(getScheme(), uri.getHost(), uri.getPath(), null).toString());
			    		urlEntity.setVideoEntityPartitionKey(this.partitionKey);
			    		urlEntity.setVideoEntityRowKey(this.rowKey);
			    		urlEntities.add(urlEntity);
			    	}
			    	setUrlEntities(true);
		    	} catch (Exception e) {
		    		throw new MCASException();
		    	}
	    	} 
    	return urlEntities;
    }
    
    private TRequest searchRequestFromEntity() throws MCASException {
		try{
			Map<String, CloudQueueMessage> messages = CloudManager.getInstance().getMessages();
	    	for (CloudQueueMessage message : messages.values()){
	    		String[] keys = message.getMessageContentAsString().split("\\*");
	    		if (keys[0].equals(this.partitionKey) && keys[1].equals(this.rowKey)){
	    			for (String requestId : messages.keySet()){
	    				if(messages.get(requestId).equals(message)){
	    					return TranscoHandler.getInstance().getRequest(requestId);
	    				}
	    			}
	    		}
	    	}
	    	return null;	
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
    
    
	private String getVideoBySuffix(TRequest request, String suffix) throws MCASException{
    	if (! request.getStatus().hasNext()) {
	    	try {
		    	List<String> uris = request.getUris();
		    	for (String uriStr : uris){
		    		if (uriStr.endsWith(suffix)){
		    			URI uri = new URI(uriStr);
		    			return (new URI(getScheme(), uri.getHost(), uri.getPath(), null)).toString();
		    		}
		    	}
	    	} catch (URISyntaxException e) {
	    		throw new MCASException();
	    	}
	    	throw new MCASException();
    	} else {
    		return "";
    	}
    }
    
    private String uriToBlob(URI uri) throws URISyntaxException{
			return (new URI("blob", uri.getHost(), uri.getPath(), null)).toString();
    }
    
    private String getHost() throws MCASException{
    	if (host == null){
    		throw new MCASException();
    	} else {
    		return host;
    	}
    }
    
    private String getScheme() throws MCASException{
    	if (scheme == null){
    		throw new MCASException();
    	} else {
    		return scheme;
    	}
    }
}
