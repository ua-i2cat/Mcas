package cat.i2cat.mcaslite.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.TranscoderConfigDao;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.entities.ApplicationConfig;
import cat.i2cat.mcaslite.entities.Transco;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderUtils {
	
	public static List<Transco> transcoBuilder(TranscoderConfig config, String id, String dstUri) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		String cmd = null;
		if(config.getTranscoder() == TranscoderConfig.FFMPEG){
			for(TLevel level : config.getLevels()){
				for(TProfile profile : config.getProfiles()){
					cmd = ffCommandBuilder(level, profile, getInput(id), getOutput(id, level.getName(), profile.getFormat()));
					commands.add(new Transco(cmd, 
							getOutput(id, level.getName(), profile.getFormat()), 
							getDestination(dstUri, ((Integer) level.getId()).toString(), profile.getFormat()),
							getInput(id)));
				}
			}
		}
		return commands;
	}
	
	private static String getDestination(String dstUri, String levelId, String extension) throws MCASException {
		String name = null;
		try {
			URI uri = new URI(dstUri);
			name = FilenameUtils.getBaseName(uri.getPath()) + levelId + "." + extension;
			name = FilenameUtils.concat(FilenameUtils.getFullPath(uri.getPath()), name);
			return uri.getScheme() + "://" + ((uri.getAuthority() == null) ? "" : uri.getAuthority())  + name;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	private static String getInput(String id){
		return FilenameUtils.concat(ApplicationConfig.getInputWorkingDir(), id);
	}
	
	private static String getOutput(String id, String levelName, String extension){
		return FilenameUtils.concat(ApplicationConfig.getOutputWorkingDir(), id + levelName + "." + extension);
	}
	
	private static String ffCommandBuilder(TLevel level, TProfile profile, String input, String output){
		String cmd = "ffmpeg -i " + input;
		cmd += " -s " + level.getScreenx() + "x" + level.getScreeny() + " -b " + level.getvBitrate() + " -ac " + level.getaChannels() + " -ab " + level.getaBitrate() + "k ";
		cmd += " -f " + profile.getFormat() + " -vcodec " + profile.getvCodec() + " -acodec " + profile.getaCodec();
		cmd += " -y " + output;
		return cmd;
	}

	public static TranscoderConfig loadConfig(String config) throws MCASException {
		return TranscoderConfigDao.findByName("default");
	}
	


}
