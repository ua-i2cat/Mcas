package net.i2cat.mcas.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TLevel;
import net.i2cat.mcas.config.model.TProfile;
import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.Callback;
import net.i2cat.mcas.management.ClassFactory;


public class RequestUtils {
	
	public static final String PATH = Paths.get(System.getProperty("mcas.home") == null ? "" : System.getProperty("mcas.home"), "config" + File.separator + "config.xml").toString();
	public static final String CALLBACK = XMLReader.getStringParameter(PATH, "callback");
	//public static final String URIseparator = "/";

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
	
	public static void callback(TRequest request) throws MCASException{
		Callback cb = ClassFactory.getCallback(CALLBACK);
		cb.callback(request);
	}
}
