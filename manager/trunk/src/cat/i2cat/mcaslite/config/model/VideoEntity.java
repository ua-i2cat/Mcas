package cat.i2cat.mcaslite.config.model;

import cat.i2cat.mcaslite.utils.AzureUtils;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class VideoEntity extends TableServiceEntity {
	
	//TODO: filename caseSensitive?
	private String Title;
    private String FileName;
    private String Description;
    private String VideoUploadedUrl;
    private String VideoConvertedUrl;
    private String Category;
    
    //TODO: ensure it is not saved
    private TRequest request;
    
    public VideoEntity() { }
    
    public VideoEntity(String lastName, String firstName) {
        this.partitionKey = lastName;
        this.rowKey = firstName;
    } 

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getVideoUploadedUrl() {
		return VideoUploadedUrl;
	}

	public void setVideoUploadedUrl(String videoUploadedUrl) {
		VideoUploadedUrl = videoUploadedUrl;
	}

	public String getVideoConvertedUrl() {
		return VideoConvertedUrl;
	}

	public void setVideoConvertedUrl(String videoConvertedUrl) {
		VideoConvertedUrl = videoConvertedUrl;
	}

	public String getCategory() {
		return Category;
	}

	public void setCategory(String category) {
		Category = category;
	}
	
	public TRequest videoEntityToTrequest(){
		//TODO: validate
		if (request == null){
			request = new TRequest();
			request.setConfig(Category);
			request.setDst(AzureUtils.uriToAzureUrl("fixed"));
			request.setSrc(AzureUtils.azureUrlToUri(VideoUploadedUrl));
		} else {
			return request;
		}
	}
}
