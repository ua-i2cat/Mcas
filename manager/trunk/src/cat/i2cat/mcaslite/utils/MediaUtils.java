package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cat.i2cat.mcaslite.config.dao.DAO;
import cat.i2cat.mcaslite.config.model.Transco;
import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class MediaUtils {
	
	public static void deleteInputFile(String requestId, Integer configId){
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			File fd = new File(FilenameUtils.concat(tConfigDao.findById(configId).getInputWorkingDir(), requestId));
			if (fd.exists()){
				fd.delete();
			}
		} catch (MCASException e) {
			e.printStackTrace();
		}
	}

	public static void toWorkingDir(URI uri, String id, int configId) throws MCASException {
		DAO<TranscoderConfig> tConfigDao = new DAO<TranscoderConfig>(TranscoderConfig.class);
		try {
			if (uri.getScheme().equals("file")) {
				FileUtils.copyFile(new File(uri.getPath()), new File(FilenameUtils.concat(tConfigDao.findById(configId).getInputWorkingDir(), id)));
			} else if (uri.getScheme().equals("http")) {
				//TODO
			} else if (uri.getScheme().equals("https")) {
				//TODO
			} else if (uri.getScheme().equals("ftp")) {
				//TODO
			} else if (uri.getScheme().equals("scp")) {
				//TODO
			}
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
			} else if (uri.getScheme().equals("https")) {
				//TODO
			} else if (uri.getScheme().equals("ftp")) {
				//TODO
			} else if (uri.getScheme().equals("scp")) {
				//TODO
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
	
	public static void clean(List<Transco> transcos){
		for(Transco transco : transcos){
			cleanTransco(transco);
		}
	}

}
