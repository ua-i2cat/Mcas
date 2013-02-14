package cat.i2cat.mcaslite.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.management.XMLReader;


public class DefaultsLoader {
	
	public DefaultsLoader(String path){
		this.path = path;
	}
	
	private String path;
	private List<TranscoderConfig> configs;
	private Map<String, TLevel> allLevels;
	private Map<String, TProfile> allProfiles;
	
	
	public List<TranscoderConfig> getConfigs(){
		if (configs == null){
			configs = new ArrayList<TranscoderConfig>();
			XMLReader reader = new XMLReader(path);
			List<Element> configElements = reader.getConfigs(reader.getDoc("config.xml"));
			for (Element el : configElements){
				configs.add(setConfig(el));
			}
		}
		return configs;
	}
	
	private Map<String,TProfile> getAllProfiles(){
		if (allProfiles == null){
			allProfiles = new HashMap<String,TProfile>();
			XMLReader reader = new XMLReader(path);
			List<Element> profileElements = reader.getRootChildrenElements(reader.getDoc("profiles.xml"));
			for(Element el : profileElements){
				allProfiles.put(el.getAttributeValue("name"), setProfile(el));			
			}
		}
		return allProfiles;
	}
	
	private Map<String,TLevel> getAllLevels(){
		if (allLevels == null){	
			allLevels = new HashMap<String,TLevel>();
			XMLReader reader = new XMLReader(path);
			List<Element> levelElements = reader.getRootChildrenElements(reader.getDoc("levels.xml"));
			for(Element el : levelElements){
				allLevels.put(el.getAttributeValue("name"), setLevel(el));			
			}
		}
		return allLevels;
	}
	
	private TranscoderConfig setConfig(Element config){
		
		TranscoderConfig tConfig = new TranscoderConfig();
		tConfig.setName(config.getAttributeValue("name"));
		tConfig.setInputWorkingDir(XMLReader.getParameter(config, "workdir.input"));
		tConfig.setOutputWorkingDir(XMLReader.getParameter(config, "workdir.output"));
		tConfig.setTimeout(Integer.parseInt(XMLReader.getParameter(config, "timeout")));
		tConfig.setLive(Boolean.parseBoolean(XMLReader.getParameter(config, "live")));
		
		tConfig.setProfiles(setConfigProfiles(getConfigProfilesName(config)));
		
		return tConfig;
	}
	
	private List<TProfile> setConfigProfiles(List<String> configProfiles){
		List<TProfile> tProfiles = new ArrayList<TProfile>();
		for(String name : configProfiles){
			tProfiles.add(getAllProfiles().get(name));
		}
		return tProfiles;
	}
	
	private TProfile setProfile(Element profile){
		TProfile tProfile = new TProfile();
		
		tProfile.setFormat(XMLReader.getParameter(profile, "format"));
		tProfile.setaCodec(XMLReader.getParameter(profile, "acodec"));
		tProfile.setvCodec(XMLReader.getParameter(profile, "vcodec"));
		tProfile.setName(XMLReader.getElementName(profile) + XMLReader.getParameter(profile, "format"));
		tProfile.setAdditionalFlags(XMLReader.getParameter(profile, "additionalflags"));
		tProfile.setLevels(setProfileLevels(getProfileLevelsName(profile)));
		
		return tProfile;
	}
	
	private List<TLevel> setProfileLevels(List<String> profileLevels){
		List<TLevel> tLevels = new ArrayList<TLevel>();
		for(String name : profileLevels){
			Map<String, TLevel> levels = getAllLevels();
			TLevel fakeLevel = levels.get(name);
			tLevels.add(fakeLevel);
		}
		return tLevels;
	}
	
	private TLevel setLevel(Element level){
		TLevel tLevel = new TLevel();
		tLevel.setaBitrate(Integer.parseInt(XMLReader.getParameter(level, "abitrate")));
		tLevel.setaChannels(Integer.parseInt(XMLReader.getParameter(level, "achannels")));
		tLevel.setName(XMLReader.getElementName(level));
		tLevel.setWidth(Integer.parseInt(XMLReader.getParameter(level, "width")));
		tLevel.setQuality(Integer.parseInt(XMLReader.getParameter(level, "quality")));
		
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
	
	public void tConfigFeedDefaults(){
		DAO<TranscoderConfig> transcoConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		List<TranscoderConfig> transcoList = getConfigs();
		for (TranscoderConfig transco : transcoList){
			transcoConfigDao.save(transco);
		}
	}

}
