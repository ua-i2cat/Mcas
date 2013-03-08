package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class MediaUtils {
	
	private static String fileNameMaker(String... pieces) throws MCASException{
		String fileName = "";
		boolean first = true;
		for (String piece : pieces){
			if (! piece.contains("_")){
				fileName += (first ? piece : "_" + piece);
			} else {
				throw new MCASException();
			}
			first = false;
		}
		return fileName; 
	}
	
	public static String fileNameMakerByProfile(String title, String profile) throws MCASException{
		return fileNameMaker(title, profile);
	}
	
	public static String fileNameMakerByLevel(String title, String profile, String level) throws MCASException{
		return fileNameMaker(title, profile, level);
	}
	
	public static String createOutputWorkingDir(String id, String outputWorkingDir) throws MCASException {
		String path = FilenameUtils.concat(outputWorkingDir, id);
		File file = new File(path);
		if (file.isDirectory() && file.canWrite()){
			return path;
		} else if(!file.exists() && file.mkdirs()){
			return path;
		} 
		throw new MCASException();
	}
	
	public static boolean deleteInputFile(String requestId, String inputWorkingDir) {
		try {
			return deleteFile(FilenameUtils.concat(getWorkDir(inputWorkingDir), requestId));
		} catch (MCASException e) {
			return false;
		}
	}
	
	public static String getWorkDir(String workDir) throws MCASException{
		File file = new File(workDir);
		if (file.isDirectory() && file.canWrite()){
			return workDir;
		} else if(!file.exists() && file.mkdirs()){
			return workDir;
		}
		throw new MCASException();
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

	public static File setInFile(String id, TranscoderConfig tConfig) throws MCASException {
		try {
			String inputDir = createWorkDir(tConfig.getInputWorkingDir());
			createWorkDir(tConfig.getOutputWorkingDir());
			return new File(FilenameUtils.concat(inputDir, id));
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static boolean deleteFile(String file){
		File fd = new File(file);
		if(fd.isDirectory() && fd.exists()){
			try {
				FileUtils.deleteDirectory(fd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (fd.exists()){
			return fd.delete();
		}
		return false;
	}

	private static void cleanTransco(Transco transco){
		deleteFile(transco.getInputFile());
		deleteFile(Paths.get(transco.getOutputDir()).getParent().toString());
	}
	
	private static void cleanTranscos(List<Transco> transcos){
		for(Transco transco : transcos){
			cleanTransco(transco);
		}
	}
	
	public static synchronized void clean(TRequest request) {
		if (request.getTranscoded().size() > 0){
			cleanTranscos(request.getTranscoded());
		} else {
			deleteInputFile(request.getId(), request.getTConfig().getInputWorkingDir());
		}
	}

}
