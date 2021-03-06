package net.i2cat.mcas.management;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import net.i2cat.mcas.config.model.TranscoderConfig;
import net.i2cat.mcas.exceptions.MCASException;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class Watcher implements Runnable, Cancellable {
	
	private Path path;
	private final FileSystem fileSystem = FileSystems.getDefault();
	private final WatchService watchService;
	private FileEventProcessor fileEP;
	private boolean done = false;
	private boolean cancelled = false;
	
	public Watcher(String path, TranscoderConfig tConfig, URI dst, String profile, String title) throws IOException, MCASException {
		this.path = Paths.get(path, profile);
		watchService = fileSystem.newWatchService();
		this.path.register(watchService, ENTRY_CREATE);
		this.fileEP = tConfig.getFileEP(dst, profile, title);
	}

	@Override
	public void run() {
		try {
			WatchKey watchKey;
			do {
				watchKey = watchService.take();
				for (WatchEvent<?> event : watchKey.pollEvents()){
					try {
						fileEP.eventHandle(event, path);
					} catch (MCASException e) {
						e.printStackTrace();
					}
				}
				watchKey.reset();
			} while(! isDone() && ! isCancelled());
		} catch (InterruptedException e){
			e.printStackTrace();
		} finally {
			try {
				watchService.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		setCancelled(true);
		setDone(true);
		return true;
	}

	private void setDone(boolean done) {
		this.done = done;
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
