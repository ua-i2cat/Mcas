package cat.i2cat.mcaslite.management;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class Watcher implements Runnable, Cancellable {
	
	private Path path;
	private final FileSystem fileSystem = FileSystems.getDefault();
	private final WatchService watchService;
	private FileEventProcessor fileEP;
	private boolean done = false;
	private boolean cancelled = false;
	
	public Watcher(String path, TranscoderConfig tConfig) throws IOException {
		this.path = Paths.get(path);
		watchService = fileSystem.newWatchService();
		this.path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		this.fileEP = tConfig.getFileEP();
	}

	@Override
	public void run() {
		try {
			WatchKey watchKey;
			do {
				watchKey = watchService.take();
				for (WatchEvent<?> event : watchKey.pollEvents()){
					fileEP.eventHandle(event, path);
				}
			} while(! isDone() && ! isCancelled() && watchKey.reset());
		} catch (InterruptedException e){
			e.printStackTrace();
		} catch (MCASException e) {
			e.printStackTrace();
		}
	}
	
	public void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		try {
			watchService.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setCancelled(true);
		return true;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}

}
