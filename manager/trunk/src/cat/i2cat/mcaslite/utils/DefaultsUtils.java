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
		TProfile profile = new TProfile();
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
		level.setName("HLShigh");
		level.setWidth(-1);
		level.setQuality(23);
		level.setMaxRate(800);
		levels.add(level);
		
		level = new TLevel();
		level.setaBitrate(128);
		level.setaChannels(2);
		level.setName("HLSmedium");
		level.setWidth(-1);
		level.setQuality(30);
		level.setMaxRate(256);
		levels.add(level);
		
		level = new TLevel();
		level.setaBitrate(128);
		level.setaChannels(2);
		level.setName("HLSlow");
		level.setWidth(640);
		level.setQuality(40);
		level.setMaxRate(96);
		levels.add(level);

		profile.setaCodec("libfdk_aac");
		profile.setFormat("mp4");
		profile.setName(DEFAULT + "mp4");
		profile.setvCodec("libx264");
		profile.setLevels(levels);
		profile.setAdditionalFlags("-profile:v baseline");
		
		lProfile.setaCodec("libfdk_aac");
		lProfile.setFormat("mp4");
		lProfile.setName(LIVE + "mp4");
		lProfile.setvCodec("libx264");
		lProfile.setLevels(levels);
		lProfile.setAdditionalFlags("-profile:v baseline -map 0 -flags -global_header");
		lProfile.setSegDuration(2);
		lProfile.setWindowLength(3);
		profiles.add(lProfile);
		
	
		tConfig.setProfiles(profiles);
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		transcoConfigDao.save(tConfig);
		
		profiles = new ArrayList<TProfile>();
		profiles.add(profile);
		
		profile = new TProfile();
		profile.setaCodec("libvorbis");
		profile.setFormat("webm");
		profile.setName(DEFAULT + "webm");
		profile.setvCodec("libvpx");
		profile.setLevels(levels);
		profile.setAdditionalFlags("");
		
		profiles.add(profile);
		
		tConfig = new TranscoderConfig();
		tConfig.setInputWorkingDir("input");
		tConfig.setOutputWorkingDir("output");
		tConfig.setThreads(1);
		tConfig.setTimeout(3600*24);
		tConfig.setTranscoder(1);
		tConfig.setName(DEFAULT);
		tConfig.setLive(false);
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
