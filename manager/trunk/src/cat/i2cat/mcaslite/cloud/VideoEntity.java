package cat.i2cat.mcaslite.cloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class VideoEntity extends TableServiceEntity {
	
    //TODO: ensure it is not saved
    private TRequest request;
    private String scheme;
    
    public VideoEntity() {
    	request = new TRequest();
    }
    
    public VideoEntity(String partitionKey, String rowKey) {
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
        this.request = new TRequest();
    }
    
    public VideoEntity(TRequest request, String partitionKey, String rowKey) {
    	this.request = request;
    	this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }

	public String getTitle() {
		return request.getTitle();
	}

	public void setTitle(String title) {
		request.setTitle(title);
	}

	public String getVideoUploadedUrl() throws MCASException {
		try {
			URI src = new URI(request.getSrc());
			return (new URI((scheme != null)? "http" : scheme, src.getHost(), src.getPath(), null)).toString();
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
	}

	public void setVideoUploadedUrl(String videoUploadedUrl) throws MCASException {
		//TODO:configuration output dir
		try {
			URI src = new URI(videoUploadedUrl);
			scheme = src.getScheme();
			request.setSrc((new URI("blob", src.getHost(), src.getPath(), null)).toString());
			request.setDst((new URI("blob", src.getHost(), Paths.get("/output", request.getId()).toString(), null)).toString());
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
	}
	
	public String getVideoConvertedUrl() throws MCASException{
		try {
			URI src = new URI(request.getSrc());
			return (new URI((scheme != null)? "http" : scheme, src.getHost(), src.getPath(), null)).toString();
		} catch (URISyntaxException e) {
			throw new MCASException();
		}
	}
	
//	public void setVideoConvertedUrl(String videoConvertedUrl) {
//		try {
//			URI src = new URI(videoConvertedUrl);
//			request.setDst((new URI("blob", src.getHost(), Paths.get("output", request.getId()).toString(), null)).toString());
//		} catch (URISyntaxException e) {
//			
//		}
//	}

	public String getConfig() {
		return request.getConfig();
	}

	public void setConfig(String config) {
		request.setConfig(config);
	}

	public TRequest videoEntityToTrequest() {
		return request;
	}

	public static String tableName(){
		//TODO: get it from configuration
		return "";
	}
}
