package net.i2cat.mcas.management;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import net.i2cat.mcas.exceptions.MCASException;

public interface FileEventProcessor {

	public void eventHandle(WatchEvent<?> event, Path path) throws MCASException;
}
