package cat.i2cat.mcaslite.management;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.NewUploader;

public class HLSManifestManager implements FileEventProcessor {
	private int windowLength;
	private int segDuration;
	private NewUploader uploader;
	private Map<String, TLevel> levels = new HashMap<String, TLevel>();
	private String profileName;
	private boolean mainCreated = false;
		
	public HLSManifestManager(int windowLength, int segDuration, URI dst, List<TLevel> levels, String profileName) throws MCASException{
		try {
			this.windowLength = windowLength;
			for(TLevel level : levels){
				this.levels.put(level.getName(), level);
			}
			this.segDuration = segDuration;
			this.uploader = new NewUploader(dst);
			this.profileName = profileName;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void createMainManifest() throws MCASException, IOException {
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		try {
			data.write("#EXTM3U\n");
			for(String level : levels.keySet()){
				data.write("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=" + (levels.get(level).getMaxRate()*1000) + "\n");
				data.write(profileName + ".m3u8\n");
			}
			data.flush();
			uploader.uploadLive(bufferedBytes.toByteArray(), profileName + ".m3u8");
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		} finally {
			data.close();
		}
	}

	@Override
	public void eventHandle(WatchEvent<?> event, Path path) throws MCASException {
		try {
			String file = event.context().toString();
			if (event.kind().equals(ENTRY_CREATE) && file.contains(".ts")){
				updateVideoFiles(path, file);
			}
		} catch(Exception e){
			throw new MCASException();
		}
	}

	private byte[] createManifest(int seg, String filename) throws MCASException, IOException {
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		if (seg >= windowLength){
			seg = seg - windowLength + 1;
		} else {
			seg = 0;
		}
		try {
			data.write("#EXTM3U\n");
			data.write("#EXT-X-VERSION:3\n");
			data.write("#EXT-X-MEDIA-SEQUENCE:" + seg + "\n");
			data.write("#EXT-X-ALLOWCACHE:0\n");
			data.write("#EXT-X-TARGETDURATION:" + segDuration + "\n");
			appendPlaylist(data, seg, filename);
			data.flush();
			return bufferedBytes.toByteArray();
		} catch (IOException e){
			e.printStackTrace();
			throw new MCASException();
		} finally {
			data.close();
		}
	}
	
	private void updateVideoFiles(Path path, String file) throws MCASException{
		try {
			String[] parsedName = file.split("_");
			String profile = parsedName[0];
			String level = parsedName[1];
			String filename = profile + "_" + level;
			int seg = Integer.parseInt(parsedName[2].substring(0, parsedName[2].lastIndexOf(".")));
			if (seg > 0){
				Path segment = Paths.get(path.toString(), filename + (--seg) + ".ts");
				uploader.upload(segment);
				segment.toFile().delete();
				if (seg >= windowLength){
					uploader.uploadLive(createManifest(seg, filename), filename + ".m3u8");
		//TODO		uploader.deleteContent(filename + "_" + (seg - windowLength) + ".ts");
				} else {
					uploader.uploadLive(createManifest(seg, filename), filename + ".m3u8");
				}
			} else if (! mainCreated) {
				createMainManifest();
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void appendPlaylist(BufferedWriter data, int seg, String filename) throws IOException {
		for(int i = seg; i < seg + windowLength; i++){
			data.write("#EXTINF:"+ segDuration + ",\n");
			data.write(filename + "_" + i + ".ts\n");
		}
	}
	
}
