package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.List;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.THLSOptions;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;

public class DefaultsUtils {
	
	public static final String DEFAULT = "default";
	public static final String LIVE = "live";
	public static final int MAX_PROCESS = 2;
	
	public static TranscoderConfig tConfigGetDefaults(){
		TranscoderConfig tConfig = new TranscoderConfig();
		List<TLevel> levels = new ArrayList<TLevel>();
		List<TProfile> profiles = new ArrayList<TProfile>();
		TLevel level = new TLevel();
		//TProfile profile = new TProfile();
		THLSOptions lProfile = new THLSOptions(); 
				
		tConfig.setInputWorkingDir("input");
		tConfig.setOutputWorkingDir("output");
		tConfig.setThreads(1);
		tConfig.setTimeout(3600*24);
		tConfig.setTranscoder(1);
		tConfig.setName(LIVE);
		tConfig.setLive(true);
	
//		level.setaBitrate(128);
//		level.setaChannels(2);
//		level.setName(DEFAULT + "_1080");
//		level.setWidth(1080);
//		level.setQuality(23);
//		levels.add(level);		
		
		level = new TLevel();
		level.setaBitrate(128);
		level.setaChannels(2);
		level.setName(DEFAULT);
		level.setWidth(-1);
		level.setQuality(23);
		levels.add(level);
		
//		level = new TLevel();
//		level.setaBitrate(128);
//		level.setaChannels(2);
//		level.setName(DEFAULT + "_640");
//		level.setWidth(640);
//		level.setQuality(33);
//		levels.add(level);

		
		lProfile.setaCodec("libfaac");
		lProfile.setFormat("mp4");
		lProfile.setName(DEFAULT + "mp4");
		lProfile.setvCodec("libx264");
		lProfile.setLevels(levels);
		lProfile.setAdditionalFlags("-profile:v baseline -map 0 -flags -global_header");
		lProfile.setSegDuration(2);
		lProfile.setWindowLength(3);
		profiles.add(lProfile);
		
	
		tConfig.setProfiles(profiles);
		
		return tConfig;
	}
	
	public static void tConfigFeedDefaults(){
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		transcoConfigDao.save(tConfigGetDefaults());
	}
	
	public static boolean feedDefaultsNeeded(){
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class); 
		try {
			if (transcoConfigDao.findByName(DEFAULT) == null){
				return true;
			}
		} catch (Exception e){
			return true;
		}
		return false;
	}
}
