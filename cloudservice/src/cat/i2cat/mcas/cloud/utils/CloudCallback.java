package cat.i2cat.mcas.cloud.utils;

import java.io.File;
import java.nio.file.Paths;

import cat.i2cat.mcas.cloud.AzureUtils;
import cat.i2cat.mcas.cloud.CloudManager;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Callback;
import cat.i2cat.mcaslite.management.Status;
import cat.i2cat.mcaslite.utils.XMLReader;

public class CloudCallback extends Callback {

	@Override
	public void callback(TRequest request) throws MCASException {
		String path = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
		try {
			if (! request.getStatus().hasNext()) {
				if (AzureUtils.updateVideoEntity(request)) {
					if (request.getStatus().getInt()==Status.ERROR){
						return;
					} else {
						AzureUtils.deleteQueueMessage(
								CloudManager.getInstance().popCloudMessage(request.getId()),
								XMLReader.getStringParameter(path, "cloud.processqueue"));
					}
				}
			} else {
				AzureUtils.updateVideoEntity(request);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
		
	}

	@Override
	public String RequestToJson(TRequest request) throws MCASException {
		return null;
	}

}
