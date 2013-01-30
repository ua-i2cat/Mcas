package cat.i2cat.mcaslite.management;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import cat.i2cat.mcaslite.exceptions.MCASException;

public interface FileEventProcessor {

	public void eventHandle(WatchEvent<?> event, Path path) throws MCASException;
	
	public void processManifest(String output) throws MCASException;
}
