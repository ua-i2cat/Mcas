package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.List;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TLiveOptions;
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
		TProfile profile = new TProfile();
		TLiveOptions liveOptions = new TLiveOptions();
		
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
		
//		profile = new TProfile();
//		profile.setaCodec("libvorbis");
//		profile.setFormat("ogg");
//		profile.setName(DEFAULT + "ogg");
//		profile.setvCodec("libtheora");
//		profiles.add(profile);

		profile = new TProfile();
		profile.setaCodec("libvorbis");
		profile.setFormat("webm");
		profile.setName(DEFAULT + "webm");
		profile.setvCodec("libvpx");
		profiles.add(profile);
		tConfig.setProfiles(profiles);
		
		liveOptions.setDash_profile("onDemand");
		liveOptions.setSeg_duration(5000);
		liveOptions.setFrag_duration(1000);
		tConfig.setLiveOptions(liveOptions);
		
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
