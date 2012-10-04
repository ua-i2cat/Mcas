package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.List;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;

public class DefaultsUtils {
	
	public static final String DEFAULT = "default";
	
	public static void tConfigFeedDefaults(){
		TranscoderConfig tConfig = new TranscoderConfig();
		List<TLevel> levels = new ArrayList<TLevel>();
		List<TProfile> profiles = new ArrayList<TProfile>();
		TLevel level = new TLevel();
		TProfile profile = new TProfile();
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class); 
		
		tConfig.setInputWorkingDir("input");
		tConfig.setOutputWorkingDir("output");
		tConfig.setThreads(1);
		tConfig.setTimeout(3600*24);
		tConfig.setTranscoder(1);
		tConfig.setName(DEFAULT);
		
		level.setaBitrate(128);
		level.setaChannels(2);
		level.setName(DEFAULT);
		level.setScreenx(1280);
		level.setScreeny(720);
		level.setvBitrate(1024);
		levels.add(level);
		tConfig.setLevels(levels);
		
		profile.setaCodec("libfaac");
		profile.setFormat("mp4");
		profile.setName(DEFAULT + "mp4");
		profile.setvCodec("libx264");
		profiles.add(profile);
		
		profile = new TProfile();
		profile.setaCodec("libvorbis");
		profile.setFormat("ogg");
		profile.setName(DEFAULT + "ogg");
		profile.setvCodec("libtheora");
		profiles.add(profile);

		profile = new TProfile();
		profile.setaCodec("libvorbis");
		profile.setFormat("webm");
		profile.setName(DEFAULT + "webm");
		profile.setvCodec("libvpx");
		profiles.add(profile);
		tConfig.setProfiles(profiles);
		
		transcoConfigDao.save(tConfig);
	}
	
	public static void applicationFeedDefaults(){
		ApplicationConfig appConf = new ApplicationConfig();
		DAO<ApplicationConfig> appConfDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
		
		appConf.setMaxInMediaH(4);
		appConf.setMaxOutMediaH(4);
		appConf.setMaxTransco(2);
		appConf.setName(DEFAULT);
		
		appConfDao.save(appConf);
	}
	
	public static boolean feedDefaultsNeeded(){
		DAO<ApplicationConfig> appConfDao = new DAO<ApplicationConfig>(ApplicationConfig.class);
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class); 
		try {
			if (appConfDao.findByName(DEFAULT) == null){
				return true;
			}
			if (transcoConfigDao.findByName(DEFAULT) == null){
				return true;
			}
		} catch (Exception e){
			return true;
		}
		return false;
	}
}
