package cat.i2cat.mcaslite.management;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import cat.i2cat.mcaslite.utils.Uploader;

public class HLSManifestManager implements FileEventProcessor {
	private int windowLength;
	private int segDuration;
	private Uploader uploader;
	private Map<String, TLevel> levels = new HashMap<String, TLevel>();
	private boolean mainCreated = false;
		
	public HLSManifestManager(int windowLength, int segDuration, URI dst, List<TLevel> levels) throws MCASException{
		try {
			this.windowLength = windowLength;
			for(TLevel level : levels){
				this.levels.put(level.getName(), level);
			}
			this.segDuration = segDuration;
			this.uploader = new Uploader(dst);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	private void createMainManifest(String filename) throws MCASException, IOException {
		ByteArrayOutputStream bufferedBytes = new ByteArrayOutputStream();
		BufferedWriter data = new BufferedWriter(new OutputStreamWriter(bufferedBytes, Charset.defaultCharset()));
		try {
			data.write("#EXTM3U\n");
			for(String level : levels.keySet()){
				data.write("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=" + (levels.get(level).getMaxRate()*1000) + "\n");
				data.write(Paths.get(level, filename).toString() + ".m3u8\n");
			}
			data.flush();
			uploader.toDestinationUri(bufferedBytes.toByteArray(), filename + ".m3u8");
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
			int lastIdx = file.lastIndexOf("_");
			int seg = Integer.parseInt(file.substring(lastIdx + 1, file.lastIndexOf(".")));
			String level = file.substring(file.lastIndexOf("_", lastIdx - 1) + 1, lastIdx);
			String filename = file.substring(0, file.lastIndexOf("_", lastIdx - 1));
			if (seg > 0){
				File segment = Paths.get(path.toString(), filename + "_" + level + "_" + (--seg) + ".ts").toFile();
				uploader.toDestinationUri(segment, level);
				segment.delete();
				if (seg >= windowLength){
					uploader.toDestinationUri(createManifest(seg, filename + "_" + level), filename + ".m3u8", level);
					uploader.deleteContent(filename + "_" + level + "_" + (seg - windowLength) + ".ts", level);
				} else {
					uploader.toDestinationUri(createManifest(seg, filename + "_" + level), filename + ".m3u8", level);
				}
			} else if (! mainCreated) {
				createMainManifest(filename);
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
