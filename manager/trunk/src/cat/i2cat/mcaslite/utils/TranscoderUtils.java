package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.TProfile;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderUtils {
	
	public static List<Transco> transcoBuilder(TranscoderConfig config, String id, URI dst, URI src) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		for(TProfile profile : config.getProfiles()){
			commands.addAll(profile.commandBuilder(
				(config.isLive()) ? src.toString() : getInput(id,config.getInputWorkingDir()), 
				getOutputFile(id, config.getOutputWorkingDir(), src.getPath()),
				getDestination(id, src.getPath(), dst)));
		}
		return commands;
	}
	
	private static String getDestination(String id, String src, URI dst) throws MCASException {
		try {
			String path = FilenameUtils.concat(getDestinationDir(dst, id).getPath(), FilenameUtils.getBaseName(src));
			return (new URI(dst.getScheme(), dst.getHost(), path, null)).toString();
		} catch (URISyntaxException e){
			throw new MCASException();
		}
	}
	
	public static URI getDestinationDir(URI dst, String id) throws MCASException {
		try {
			if (dst.getScheme().equals("file")){
				File file = new File(dst);
				if (! file.exists() && file.getParentFile().isDirectory() && file.getParentFile().canWrite()){
					return new URI("file", dst.getHost() , file.getPath(), null);
				} else if (file.exists() && file.isDirectory() && file.canWrite()) {
					file = new File(FilenameUtils.concat(dst.getPath(), id));
					return new URI("file", dst.getHost() , file.getPath(), null);
				} else {
					throw new MCASException();
				}
			} else {
				return new URI(dst.getScheme(), dst.getHost() , Paths.get(dst.getPath(), id).toString(), null);
			}
		} catch (URISyntaxException e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	private static String getInput(String id, String inWorkDir) throws MCASException{
		return FilenameUtils.concat(MediaUtils.getWorkDir(inWorkDir), id);
	}
	
	public static String getOutputFile(String id, String outWorkDir, String src) throws MCASException{
		return FilenameUtils.concat(
				FilenameUtils.concat(MediaUtils.getWorkDir(outWorkDir), id), FilenameUtils.getBaseName(src));
	}
	
	public static String getOutputDir(String id, String outWorkDir){
		return FilenameUtils.concat(MediaUtils.getWorkDir(outWorkDir), id);
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
