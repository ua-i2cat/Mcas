package cat.i2cat.mcaslite.cloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class VideoEntity extends TableServiceEntity {
	
    private String scheme;
    private String host;
    private String fileName;
    private String description;
    private String category;
    private String title;
    private String videoUploadedUrl;
    private String videoConvertedUrlMp4;
    private String videoConvertedUrlWebM;
    private String status;
    private String cancelId;
    private String tenantContainer;
    
    
    public VideoEntity() {}
    
    public VideoEntity(String partitionKey, String rowKey) {
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
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
			throw new MCASException();
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
			request.setDst((new URI("blob", getHost(), Paths.get("/",tenantContainer).toString(), null)).toString());
			request.setTitle(title);
			return request;
    	} catch (URISyntaxException e) {
    		throw new MCASException();
    	}
    }
    
    public void updateFromRequest(TRequest request) throws MCASException{
    	this.status = request.getStatus().toString();
    	this.cancelId = request.getId();
    	this.videoConvertedUrlMp4 = getVideoBySuffix(request, ".mp4");
    	this.videoConvertedUrlWebM = getVideoBySuffix(request, ".webm");
    }
    
    private String getVideoBySuffix(TRequest request, String suffix) throws MCASException{
    	try {
	    	List<Transco> transcos = request.getTranscoded();
	    	for (Transco transco : transcos){
	    		if (transco.getDestinationUri().endsWith(suffix)){
	    			URI uri = transco.getDestinationUriUri();
	    			return (new URI(getScheme(), uri.getHost(), uri.getPath(), null)).toString();
	    		}
	    	}
    	} catch (URISyntaxException e) {
    		throw new MCASException();
    	}
    	throw new MCASException();
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
