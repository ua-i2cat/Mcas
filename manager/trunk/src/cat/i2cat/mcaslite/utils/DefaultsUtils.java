package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.List;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TDashOptions;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;

public class DefaultsUtils {
	
	public static final String DEFAULT = "default";
	public static final int MAX_PROCESS = 2;
	
	public static TranscoderConfig tConfigGetDefaults(){
		TranscoderConfig tConfig = new TranscoderConfig();
		List<TLevel> levels = new ArrayList<TLevel>();
		List<TProfile> profiles = new ArrayList<TProfile>();
		TLevel level = new TLevel();
		//TProfile profile = new TProfile();
		TDashOptions dashOptions = new TDashOptions();
		//THLSOptions profile = new THLSOptions(); 
		
		tConfig.setInputWorkingDir("input");
		tConfig.setOutputWorkingDir("output");
		tConfig.setThreads(1);
		tConfig.setTimeout(3600*24);
		tConfig.setTranscoder(1);
		tConfig.setName(DEFAULT);
		
//		level.setaBitrate(128);
//		level.setaChannels(2);
//		level.setName(DEFAULT + "_1080");
//		level.setWidth(1080);
//		level.setQuality(23);
//		levels.add(level);		
//		
//		level = new TLevel();
//		level.setaBitrate(128);
//		level.setaChannels(2);
//		level.setName(DEFAULT);
//		level.setWidth(-1);
//		level.setQuality(23);
//		levels.add(level);
		
		level = new TLevel();
		level.setaBitrate(128);
		level.setaChannels(2);
		level.setName(DEFAULT + "_640");
		level.setWidth(640);
		level.setQuality(33);
		levels.add(level);
		
//		profile.setaCodec("libfaac");
//		profile.setFormat("mpegts");
//		profile.setName(DEFAULT + "HLS");
//		profile.setvCodec("libx264");
//		profile.setAdditionalFlags("-profile:v baseline");
//		profile.setLevels(levels);
//		profile.setSegDuration(2);
//		profile.setWindowLength(3);
//		profiles.add(profile);
		
//		profile = new TProfile();
//		profile.setaCodec("libvorbis");
//		profile.setFormat("webm");
//		profile.setName(DEFAULT + "webm");
//		profile.setvCodec("libvpx");
//		profile.setLevels(levels);
//		profiles.add(profile);
		
		dashOptions = new TDashOptions();
		dashOptions.setSegDuration(10000);
		dashOptions.setFragDuration(5000);
		dashOptions.setaCodec("libvorbis");
		dashOptions.setFormat("webm");
		dashOptions.setName(DEFAULT + "webm");
		dashOptions.setvCodec("libvpx");
		dashOptions.setLevels(levels);
		profiles.add(dashOptions);
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
