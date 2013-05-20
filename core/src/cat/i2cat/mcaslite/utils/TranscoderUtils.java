package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderUtils {
	
	public static List<Transco> transcoBuilder(TranscoderConfig config, String id, URI src, String title) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		for(TProfile profile : config.getProfiles()){
			commands.addAll(profile.commandBuilder(
				(config.isLive()) ? src.toString() : getInput(id,config.getInputWorkingDir()), 
				getOutput(id, config.getOutputWorkingDir(), profile.getName()), 
				config.isLive(),
				title));
		}
		return commands;
	}
	
	public static URI getDestinationDir(URI dst, String id) throws MCASException {
		try {
			if (dst.getScheme().equals("file")){
				File file = new File(dst);
				if (! file.exists() && file.getParentFile().isDirectory() && file.getParentFile().canWrite()){
					return new URI("file", dst.getHost() , file.getPath(), null);
				} else if (file.exists() && file.isDirectory() && file.canWrite()) {
					file = new File(new URI(dst.getPath() + "/" + id));
					return new URI("file", dst.getHost() , file.getPath(), null);
				} else {
					throw new MCASException();
				}
			} else {
				return new URI(dst.getScheme(), dst.getHost() , dst.getPath() + "/" + id, null);
			}
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	private static String getInput(String id, String inWorkDir) throws MCASException{
		return FilenameUtils.concat(MediaUtils.getWorkDir(inWorkDir), id);
	}
	
	public static String getOutput(String id, String outWorkDir, String profile) throws MCASException{
		return MediaUtils.getWorkDir(FilenameUtils.concat(
			MediaUtils.getWorkDir(FilenameUtils.concat(MediaUtils.getWorkDir(outWorkDir), id)), profile));
	}


	public static TranscoderConfig loadConfig(String config) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			return tConfigDao.findByName(config);
		}catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
}
