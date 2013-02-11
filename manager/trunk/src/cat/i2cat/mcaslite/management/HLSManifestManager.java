package cat.i2cat.mcaslite.management;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import org.apache.commons.io.FilenameUtils;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.Uploader;

public class HLSManifestManager implements FileEventProcessor {

	private int windowLength;
	private int segDuration;
	private Uploader uploader;
	private byte[] manifest;
	
	public HLSManifestManager(int windowLength, int segDuration, URI dst) {
		this.windowLength = windowLength;
		this.uploader = new Uploader(dst);
	}
	
	@Override
	public void eventHandle(WatchEvent<?> event, Path path) throws MCASException {
		try {
			String file = event.context().toString();
			if (event.kind().equals(ENTRY_CREATE) && file.contains(".ts")){
				updateVideoFiles(path, file);
			} else {
				System.out.println("ELSE" + file);
			}
		} catch(Exception e){
			throw new MCASException();
		}
	}

	private void createManifest(String file) throws MCASException, IOException {
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		try {
			data.write("#EXTM3U\n");
			data.write("#EXT-X-VERSION:3\n");
			data.write("#EXT-X-MEDIA-SEQUENCE:0\n");
			data.write("#EXT-X-ALLOWCACHE:0\n");
			data.write("#EXT-X-TARGETDURATION:" + segDuration + "\n");
			appendPlaylist(file, data, 0);
			data.flush();
			manifest = bufferedBytes.toByteArray();
			uploader.toDestinationUri(manifest, Paths.get(file.substring(0, file.lastIndexOf(".")) + ".m3u8").getFileName().toString());
		} catch (IOException e){
			e.printStackTrace();
			throw new MCASException();
		} finally {
			data.close();
		}
	}
	
	private void updateVideoFiles(Path path, String file) throws MCASException{
		try {
			int sIdx = file.lastIndexOf("_");
			int seg = Integer.parseInt(file.substring(sIdx + 1, file.lastIndexOf(".")));
			if (seg > 0){
				File segment = new File(path.toString(), file.substring(0, sIdx) + "_" + (--seg) + ".ts");
				uploader.toDestinationUri(segment);
				segment.delete();
				if (seg == 0){
					createManifest(Paths.get(path.toString(), file.substring(0, sIdx)).toString() + ".csv");	
				} if (seg >= windowLength){
					updateManifest(Paths.get(path.toString(), file.substring(0, sIdx)).toString() + ".csv", true, seg);
					uploader.deleteContent(file.substring(0, sIdx) + "_" + (seg - windowLength) + ".ts");
				} else {
					updateManifest(Paths.get(path.toString(), file.substring(0, sIdx)).toString() + ".csv", false, seg);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void updateManifest(String file, boolean ignoreLines, int seg) throws MCASException, IOException{
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(manifest), Charset.defaultCharset()));
		try {
			String line = reader.readLine();
			while(line != null){
				if (ignoreLines && line.startsWith("#EXTINF:")){
					line = reader.readLine();
					ignoreLines = false;
				} else {
					data.write(line + "\n");
				}
				line = reader.readLine();
			}
			appendPlaylist(file, data, seg);
			data.flush();
			manifest = bufferedBytes.toByteArray();
			uploader.toDestinationUri(manifest, Paths.get(file.substring(0, file.lastIndexOf(".")) + ".m3u8").getFileName().toString());
		} catch (IOException e){
			e.printStackTrace();
			throw new MCASException();
		} finally{
			data.close();
			reader.close();
		}
	}

	private void appendPlaylist(String file, BufferedWriter data, int seg) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			String line = reader.readLine();
			while(line != null) {
				if (line.contains("_" + seg + ".ts")){
					data.write(transformLine(line.trim()));
					break;
				}
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}
	}
	
	private String transformLine(String str){
		String line = new String();
		String[] data = str.split(",");
		line = "#EXTINF:" + (Float.parseFloat(data[2]) - Float.parseFloat(data[1])) + "\n";
		line += FilenameUtils.getName(data[0]) + "\n";
		return line;
	}

	@Override
	public void processManifest(String output) throws MCASException {
		// TODO Auto-generated method stub
		
	}
}
