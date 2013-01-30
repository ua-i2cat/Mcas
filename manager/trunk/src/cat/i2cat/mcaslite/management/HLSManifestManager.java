package cat.i2cat.mcaslite.management;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FilenameUtils;



import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class HLSManifestManager implements FileEventProcessor {

	private int windowLength;
	
	public HLSManifestManager(int windowLength){
		this.windowLength = windowLength;
	}
	
	@Override
	public void eventHandle(WatchEvent<?> event, Path path) throws MCASException {
		try {
			String file = event.context().toString();
			int seg = Integer.parseInt(file.substring(file.lastIndexOf("_")));
			if (event.kind().equals(ENTRY_CREATE) && file.contains(".ts")){
				if (seg >= windowLength){
					file = file.substring(0, file.lastIndexOf("_")).concat("_" + seg + ".ts");
					Files.deleteIfExists(Paths.get(file));
					file = file.substring(0, file.lastIndexOf("_")).concat(".m3u8");
					List<String> lines =  Files.readAllLines(Paths.get(path.toString(), file), Charset.defaultCharset());
					ListIterator<String> it = lines.listIterator();
					while(it.hasNext()){
						String line = it.next();
						if (line.contains("#EXTINF")){
							it.remove();
							it.next();
							it.remove();
						} else if (! line.startsWith("#")){
							it.set(FilenameUtils.getBaseName(line));
						}
					}
					Files.write(Paths.get(path.toString(), file), lines, Charset.defaultCharset());
				}
			}
		} catch(Exception e){
			throw new MCASException();
		}
	}

	@Override
	public void processManifest(String output) throws MCASException {
		// TODO Auto-generated method stub
		
	}

}
