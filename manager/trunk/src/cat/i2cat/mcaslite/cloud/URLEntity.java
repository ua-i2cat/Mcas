package cat.i2cat.mcaslite.cloud;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class URLEntity extends TableServiceEntity {
	
	private String videoEntityRowKey;
	private String videoEntityPartitionKey;
	private String url;
	
	public URLEntity() {}
	
	public URLEntity(String partitionKey, String rowKey) {
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }

	public String getVideoEntityRowKey() {
		return videoEntityRowKey;
	}

	public void setVideoEntityRowKey(String videoEntityRowKey) {
		this.videoEntityRowKey = videoEntityRowKey;
	}

	public String getVideoEntityPartitionKey() {
		return videoEntityPartitionKey;
	}

	public void setVideoEntityPartitionKey(String videoEntityPartitionKey) {
		this.videoEntityPartitionKey = videoEntityPartitionKey;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url ;
	}
	
}
