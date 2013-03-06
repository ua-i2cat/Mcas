package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TDASHOptions;
import cat.i2cat.mcaslite.config.model.THLSOptions;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;


public class DefaultsLoader {
	
	public static String DEFAULT = "default";
	
	public DefaultsLoader(String path){
		this.path = path;
	}
	
	private String path;
	private List<TranscoderConfig> configs;
	private Map<String, TLevel> allLevels;
	private Map<String, TProfile> allProfiles;
	
	
	public List<TranscoderConfig> getConfigs() throws MCASException{
		if (configs == null){
			configs = new ArrayList<TranscoderConfig>();
			XMLReader reader = new XMLReader(path);
			List<Element> configElements = reader.getConfigs(reader.getDoc("config.xml"));
			for (Element el : configElements){
				configs.add(getConfig(el));
			}
		}
		return configs;
	}
	
	private Map<String,TProfile> getAllProfiles() throws MCASException{
		if (allProfiles == null){
			allProfiles = new HashMap<String,TProfile>();
			XMLReader reader = new XMLReader(path);
			List<Element> profileElements = reader.getRootChildrenElements(reader.getDoc("profiles.xml"));
			for(Element el : profileElements){
				allProfiles.put(el.getAttributeValue("name"), getProfile(el));			
			}
		}
		return allProfiles;
	}
	
	private Map<String,TLevel> getAllLevels() throws MCASException{
		if (allLevels == null){	
			allLevels = new HashMap<String,TLevel>();
			XMLReader reader = new XMLReader(path);
			List<Element> levelElements = reader.getRootChildrenElements(reader.getDoc("levels.xml"));
			for(Element el : levelElements){
				allLevels.put(el.getAttributeValue("name"), getLevel(el));			
			}
		}
		return allLevels;
	}
	
	private TranscoderConfig getConfig(Element config) throws MCASException{
		TranscoderConfig tConfig = new TranscoderConfig();
		tConfig.setName(config.getAttributeValue("name"));
		tConfig.setInputWorkingDir(XMLReader.getStringParameter(config, "workdir.input"));
		tConfig.setOutputWorkingDir(XMLReader.getStringParameter(config, "workdir.output"));
		tConfig.setTimeout(XMLReader.getIntParameter(config, "timeout"));
		tConfig.setLive(Boolean.parseBoolean(XMLReader.getStringParameter(config, "live")));
		tConfig.setProfiles(getConfigProfiles(getConfigProfilesName(config)));
		return tConfig;
	}
	
	private List<TProfile> getConfigProfiles(List<String> configProfiles) throws MCASException{
		List<TProfile> tProfiles = new ArrayList<TProfile>();
		for(String name : configProfiles){
			tProfiles.add(getAllProfiles().get(name));
		}
		return tProfiles;
	}
	
	private TProfile getProfile(Element profile) throws MCASException{
		String classAtr = profile.getAttributeValue("class");
		if (classAtr != null && classAtr.equals("HLS")){
			return getHLSProfile(profile);
		} else if (classAtr != null && classAtr.equals("DASH")){
			return getDASHProfile(profile);
		} else {
			TProfile tProfile = new TProfile();
			setStdProfile(tProfile, profile);
			return tProfile;
		}
	}
	
	private THLSOptions getHLSProfile(Element profile) throws MCASException{
		THLSOptions hProfile = new THLSOptions(); 
		hProfile.setWindowLength(XMLReader.getIntParameter(profile, "windowLength"));
		hProfile.setSegDuration(XMLReader.getIntParameter(profile, "segDuration"));
		setStdProfile(hProfile, profile);
		return hProfile;
	}
	
	private TDASHOptions getDASHProfile(Element profile) throws MCASException{
		TDASHOptions dProfile = new TDASHOptions();
		dProfile.setSegDuration(XMLReader.getIntParameter(profile, "segDuration"));
		dProfile.setFragDuration(XMLReader.getIntParameter(profile, "fragDuration"));
		setStdProfile(dProfile, profile);
		return dProfile;
	}
	
	private void setStdProfile(TProfile tProfile, Element profile) throws MCASException{
		tProfile.setFormat(XMLReader.getStringParameter(profile, "format"));
		tProfile.setaCodec(XMLReader.getStringParameter(profile, "acodec"));
		tProfile.setvCodec(XMLReader.getStringParameter(profile, "vcodec"));
		tProfile.setName(XMLReader.getElementName(profile));
		tProfile.setAdditionalFlags(XMLReader.getStringParameter(profile, "additionalFlags"));
		tProfile.setLevels(getProfileLevels(getProfileLevelsName(profile)));
	}
	
	private List<TLevel> getProfileLevels(List<String> profileLevels) throws MCASException{
		List<TLevel> tLevels = new ArrayList<TLevel>();
		for(String name : profileLevels){
			Map<String, TLevel> levels = getAllLevels();
			TLevel fakeLevel = levels.get(name);
			tLevels.add(fakeLevel);
		}
		return tLevels;
	}
	
	private TLevel getLevel(Element level) throws MCASException{
		TLevel tLevel = new TLevel();
		tLevel.setaBitrate(XMLReader.getIntParameter(level, "abitrate"));
		tLevel.setaChannels(XMLReader.getIntParameter(level, "achannels"));
		tLevel.setName(XMLReader.getElementName(level));
		tLevel.setWidth(XMLReader.getIntParameter(level, "width"));
		tLevel.setQuality(XMLReader.getIntParameter(level, "quality"));
		tLevel.setMaxRate(XMLReader.getIntParameter(level, "maxRate"));
		return tLevel;
	}
	
	private List<String> getConfigProfilesName(Element config){
		List<String> profileNames= new ArrayList<String>();
		List<Element> configProfiles = config.getChild("profiles").getChildren("profile");
		for (Element el : configProfiles){
			profileNames.add(el.getAttributeValue("name"));
		}
		return profileNames;
	}
	
	private List<String> getProfileLevelsName(Element profile){
		List<String> levelNames= new ArrayList<String>();
		List<Element> profileLevels = profile.getChild("levels").getChildren("level");
		for (Element el : profileLevels){
			levelNames.add(el.getAttributeValue("name"));
		}
		return levelNames;
	}
	
	public void tConfigFeedDefaults() throws MCASException{
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		List<TranscoderConfig> transcoList = getConfigs();
		for (TranscoderConfig transco : transcoList){
			transcoConfigDao.save(transco);
		}
	}

}
