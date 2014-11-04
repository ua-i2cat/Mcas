package net.i2cat.mcas.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import net.i2cat.mcas.config.dao.DAO;
import net.i2cat.mcas.config.model.TProfile;
import net.i2cat.mcas.config.model.Transco;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;

public class TranscoderUtils {
	
	public static List<Transco> transcoBuilder(TranscoderConfig config, String id, URI src, String title) throws MCASException{
		List<Transco> commands = new ArrayList<Transco>();
		for(TProfile profile : config.getProfiles()){
			String input;
			if (config.isLive() && !src.getScheme().equals("file")){
				input = src.toString();
			} else {
				input = getInput(id,config.getInputWorkingDir());
			}
			commands.addAll(profile.commandBuilder(
				input, getOutput(id, config.getOutputWorkingDir(), profile.getName()), 
				config.isLive(), title));
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
	
	private static String getOutput(String id, String outWorkDir, String profile) throws MCASException{
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
