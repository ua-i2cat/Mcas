package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class MediaUtils {
	
	public static void deleteInputFile(String requestId, Integer configId){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			File fd = new File(FilenameUtils.concat(getWorkDir(tConfigDao.findById(configId).getInputWorkingDir()), requestId));
			if (fd.exists()){
				fd.delete();
			}
		} catch (MCASException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteInputFile(String requestId, String configName){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			File fd = new File(FilenameUtils.concat(getWorkDir(tConfigDao.findByName(configName).getInputWorkingDir()), requestId));
			if (fd.exists()){
				fd.delete();
			}
		} catch (MCASException e) {
			e.printStackTrace();
		}
	}
	
	public static String getWorkDir(String workDir){
		File file = new File(workDir);
		if (! file.isAbsolute()){
			file = new File(FilenameUtils.concat(System.getProperty("mcas.home"), workDir));
		}
		return file.getPath();
	}
	
	private static String createWorkDir(String workDir) throws MCASException{
		File file = new File(getWorkDir(workDir));
		if (! file.exists()){
			file.mkdirs();
		} else if (! file.isDirectory()) {
			throw new MCASException();
		}
		return file.getPath();
	}

	public static File setInFile(String id, int configId) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			String inputDir = createWorkDir(tConfigDao.findById(configId).getInputWorkingDir());
			createWorkDir(tConfigDao.findById(configId).getOutputWorkingDir());
			return new File(FilenameUtils.concat(inputDir, id));
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void toDestinationUri(String file, URI uri) throws MCASException {
		try {
			if (uri.getScheme().equals("file")) {
				if ((new File(file)).exists()) {
					FileUtils.copyFile(new File(file), new File(uri.getPath()));
				} else {
					throw new MCASException();
				}
			} else if (uri.getScheme().equals("http")) {
				//TODO
				throw new MCASException();
			} else if (uri.getScheme().equals("https")) {
				//TODO
				throw new MCASException();
			} else if (uri.getScheme().equals("ftp")) {
				//TODO
				throw new MCASException();
			} else if (uri.getScheme().equals("scp")) {
				//TODO
				throw new MCASException();
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void deleteFile(String file){
		File fd = new File(file);
		if (fd.exists()){
			fd.delete();
		}
	}

	private static void cleanTransco(Transco transco){
		File input = new File(transco.getInputFile());
		File output = new File(transco.getOutputFile());
		if (input.exists()){
			input.delete();
		}
		if (output.exists()){
			output.delete();
		}
	}
	
	private static void clean(List<Transco> transcos){
		for(Transco transco : transcos){
			cleanTransco(transco);
		}
	}
	
	public static synchronized void clean(TranscoRequest request){
		if (request.getTranscoded().size() > 0){
			clean(request.getTranscoded());
		} else {
			deleteInputFile(request.getIdStr(), request.getConfig());
		}
	}

}
