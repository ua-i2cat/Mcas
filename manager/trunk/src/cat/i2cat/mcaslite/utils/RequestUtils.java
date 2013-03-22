package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import cat.i2cat.mcaslite.cloud.AzureUtils;
import cat.i2cat.mcaslite.cloud.CloudManager;
import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.Status;


public class RequestUtils {

	public static final String URIseparator = "/";

	public static boolean isValidSrcUri(URI uri) {
		try {
			if (isValidUri(uri)){
				if (uri.getScheme().equals("file")) {
					File file = new File(uri);
					return file.exists();
				} else if (uri.getScheme().equals("http")) {
					HttpURLConnection httpCon = (HttpURLConnection) uri.toURL().openConnection();
					httpCon.setRequestMethod("HEAD");
					return (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK);
				} else if (uri.getScheme().equals("rtp") || uri.getScheme().equals("rtsp")) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static boolean isValidUri(URI uri){
		if (! uri.isAbsolute()) {
			return false;
		} else if (uri.getScheme().equals("file")) {
			if (uri.getAuthority() != null) {
				return false;
			} else {
				return true;
			}
		} else if (uri.getScheme().equals("http")) {
			UrlValidator urlValidator = new UrlValidator();
			return urlValidator.isValid(uri.toString());
		} else if (uri.getScheme().equals("https")) {
			return false;
		} else if (uri.getScheme().equals("ftp")) {
			return false;
		} else if (uri.getScheme().equals("rtp") || uri.getScheme().equals("rtsp")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isValidDestination(URI uri){
		if (! isValidUri(uri) || uri.getPath() == null  || FilenameUtils.getBaseName(uri.getPath()).isEmpty()){
			return false;
		}
		return true;
	}
	
	public static TranscoderConfig getCustomTranscoderConfig(String profile, String level) throws MCASException{
		DAO<TranscoderConfig> configDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		TranscoderConfig config = configDao.findByName(DefaultsLoader.DEFAULT);
		List<TProfile> profiles = new ArrayList<TProfile>();
		profiles.add(getProfile(profile, level));
		config.setProfiles(profiles);
		return config;
	}
	
	private static TProfile getProfile(String profile, String level) throws MCASException {
		DAO<TProfile> profileDao = new DAO<TProfile>(TProfile.class);
		TProfile tProfile = profileDao.findByName(profile);
		List<TLevel> levels = new ArrayList<TLevel>();
		levels.add(getLevel(level));
		tProfile.setLevels(levels);
		return tProfile;
	}
	
	private static TLevel getLevel(String level) throws MCASException{
		DAO<TLevel> levelDao = new DAO<TLevel>(TLevel.class);
		TLevel tLevel = levelDao.findByName(level);
		return tLevel;
	}

	public static void callback(TRequest request) throws MCASException {
		try {
			if (! request.getStatus().hasNext()) {
				if (AzureUtils.updateVideoEntity(request)) {
					if (request.getStatus().equals(Status.ERROR)){
						return;
					} else {
						AzureUtils.deleteQueueMessage(
								CloudManager.getInstance().popCloudMessage(request.getId()),
								XMLReader.getStringParameter("config/config.xml", "cloud.processqueue"));
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
}
