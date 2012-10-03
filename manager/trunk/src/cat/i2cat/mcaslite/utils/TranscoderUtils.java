package cat.i2cat.mcaslite.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderUtils {
	
	public static List<Transco> transcoBuilder(TranscoderConfig config, String id, String dstUri) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		String cmd = null;
		if(config.getTranscoder() == TranscoderConfig.FFMPEG){
			for(TLevel level : config.getLevels()){
				for(TProfile profile : config.getProfiles()){
					cmd = ffCommandBuilder(level, profile, getInput(id, config.getId()), getOutput(id, level.getName(), profile.getFormat(), config.getId()));
					commands.add(new Transco(cmd, 
							getOutput(id, level.getName(), profile.getFormat(), config.getId()), 
							getDestination(dstUri, ((Integer) level.getId()).toString(), profile.getFormat()),
							getInput(id, config.getId())));
				}
			}
		}
		return commands;
	}
	
	private static String getDestination(String dstUri, String levelId, String extension) throws MCASException {
		String name = null;
		try {
			URI uri = new URI(dstUri);
			name = FilenameUtils.getBaseName(uri.getPath()) + "_" + levelId + "." + extension;
			name = FilenameUtils.concat(FilenameUtils.getFullPath(uri.getPath()), name);
			return uri.getScheme() + "://" + ((uri.getAuthority() == null) ? "" : uri.getAuthority())  + name;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	private static String getInput(String id, int configId) throws MCASException{
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		return FilenameUtils.concat(tConfigDao.findById(configId).getInputWorkingDir(), id);
	}
	
	private static String getOutput(String id, String levelName, String extension, int configId) throws MCASException{
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		return FilenameUtils.concat(tConfigDao.findById(configId).getOutputWorkingDir(), id + levelName + "." + extension);
	}
	
	private static String ffCommandBuilder(TLevel level, TProfile profile, String input, String output){
		String cmd = "ffmpeg -i " + input;
		cmd += " -s " + level.getScreenx() + "x" + level.getScreeny() + " -b " + level.getvBitrate() + " -ac " + level.getaChannels() + " -ab " + level.getaBitrate() + "k ";
		cmd += " -f " + profile.getFormat() + " -vcodec " + profile.getvCodec() + " -acodec " + profile.getaCodec();
		cmd += " -y " + output;
		return cmd;
	}

	public static TranscoderConfig loadConfig(String config) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			return tConfigDao.findByName(config);
		}catch (Exception e){
			e.printStackTrace();
			return tConfigDao.findByName("default");
		}
	}
	
	public static int getConfigId(String configName) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		return tConfigDao.findByName("default").getId();
	}
}
