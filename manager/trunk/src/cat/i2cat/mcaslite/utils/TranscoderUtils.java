package cat.i2cat.mcaslite.utils;

import java.io.File;
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
		return videoOnDemand(config, id, dstUri);
	}
	
	private static List<Transco> videoOnDemand(TranscoderConfig config, String id, String dstUri) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		String cmd = null;
		if(config.getTranscoder() == TranscoderConfig.FFMPEG){
			for(TLevel level : config.getLevels()){
				for(TProfile profile : config.getProfiles()){
					cmd = ffCommandBuilder(level, profile, 
							getInput(id, config.getInputWorkingDir()), getOutput(id, level.getName(), 
							profile.getFormat(), config.getOutputWorkingDir()));
					commands.add(new Transco(cmd, 
							getOutput(id, level.getName(), profile.getFormat(), config.getOutputWorkingDir()), 
							getDestination(dstUri, level.getName(), profile.getFormat(), id),
							getInput(id, config.getInputWorkingDir())));
				}
			}
		}
		return commands;
	}
	
	private static String getDestination(String dstUri, String levelName, String extension, String id) throws MCASException {
		String name = null;
		try {
			URI uri = new URI(dstUri);
			name = getDestinationBaseName(uri, id) + "_" + levelName + "." + extension;
			name = FilenameUtils.concat(FilenameUtils.getFullPath(uri.getPath()), name);
			return uri.getScheme() + "://" + ((uri.getAuthority() == null) ? "" : uri.getAuthority())  + name;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private static String getDestinationBaseName(URI uri, String id) throws MCASException {
		File file = new File(uri.getPath());
		if (file.exists() && file.isDirectory() && file.canWrite()){
			return FilenameUtils.concat(file.getName(), id);
		} else if (! file.exists() && file.getParentFile().exists() 
			&& file.getParentFile().isDirectory() && file.getParentFile().canWrite()){
			return file.getName();
		} else {
			throw new MCASException();
		}
	}
	

	private static String getInput(String id, String inWorkDir) throws MCASException{
		return FilenameUtils.concat(MediaUtils.getWorkDir(inWorkDir), id);
	}
	
	private static String getOutput(String id, String levelName, String extension, String outWorkDir) throws MCASException{
		return FilenameUtils.concat(MediaUtils.getWorkDir(outWorkDir), id + levelName + "." + extension);
	}
	
	private static String ffCommandBuilder(TLevel level, TProfile profile, String input, String output){
		String cmd = "ffmpeg -i " + input;
		cmd += " -vf scale="+ level.getWidth() +":-1" + " -qmin " + level.getQuality() + " -qmax " + level.getQuality() + " -ac "; 
		cmd += level.getaChannels() + " -b:a " + level.getaBitrate() + "k ";
		cmd += " -f " + profile.getFormat() + " -codec:v " + profile.getvCodec() + " -codec:a " + profile.getaCodec();
		cmd += " -y " + output;
		return cmd;
	}

	public static TranscoderConfig loadConfig(String config) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			return tConfigDao.findByName(config);
		}catch (Exception e){
			e.printStackTrace();
			return tConfigDao.findByName(DefaultsUtils.DEFAULT);
		}
	}
}
