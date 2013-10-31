package cat.i2cat.mcaslite.management;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;
import cat.i2cat.mcaslite.config.model.TLevel;
import cat.i2cat.mcaslite.utils.Uploader;
import cat.i2cat.mcaslite.config.model.*;

public class DashManifestManager implements FileEventProcessor {

    private String output;
    private Map<String, TLevel> levels = new HashMap<String, TLevel>();
    private Uploader uploader;
    private String profileName;
    private String title;
    private DefaultExecutor executor_init;
    private DefaultExecutor executor_isom;
    private int windowLength;
    private int segDuration;
    private DefaultExecutor executor_mpd;

    public DashManifestManager() {
    }

    public DashManifestManager(int windowLength, int segDuration, URI dst,
	    List<TLevel> levels, String profileName, String title)
	    throws MCASException {
	try {
	    this.windowLength = windowLength;
	    for (TLevel level : levels) {
		this.levels.put(level.getName(), level);
	    }
	    this.segDuration = segDuration;
	    this.uploader = new Uploader(dst);
	    this.profileName = profileName;
	    this.title = title;
	    this.executor_init = new DefaultExecutor();
	    this.executor_isom = new DefaultExecutor();
	    this.executor_mpd = new DefaultExecutor();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new MCASException();
	}
    }

    public void processManifest() throws MCASException {
	try {
	    SAXBuilder builder = new SAXBuilder();
	    File folder = new File(output);
	    String mpdFile = null;

	    for (File file : folder.listFiles()) {
		if (file.getName().endsWith((".mpd"))) {
		    mpdFile = file.getPath();
		}
	    }

	    Document doc = (Document) builder.build(mpdFile);
	    Element rootNode = doc.getRootElement();

	    parseMpd(rootNode);

	    XMLOutputter xmlOutput = new XMLOutputter();
	    xmlOutput.setFormat(Format.getPrettyFormat());
	    xmlOutput.output(doc, new FileWriter(mpdFile));

	} catch (Exception e) {
	    e.printStackTrace();
	    throw new MCASException();
	}
    }

    private void parseMpd(Element rootNode) {
	Namespace ns = rootNode.getNamespace();
	Element period = rootNode.getChild("Period", ns);
	Element adaptationSet2 = new Element("AdaptationSet", ns);
	Element contentComponent = new Element("ContentComponent", ns);
	adaptationSet2.addContent(contentComponent);

	for (Element adaptationSet : period.getChildren("AdaptationSet", ns)) {
	    for (Element contComponent : adaptationSet.getChildren(
		    "ContentComponent", ns)) {
		adaptationSet.removeChild("ContentComponent", ns);
	    }
	    contentComponent.setAttribute("id", "1");
	    contentComponent.setAttribute("contentType", "video");

	    for (Element representation : adaptationSet.getChildren(
		    "Representation", ns)) {
		representation.removeAttribute("audioSamplinRate");
		representation.removeChildren("AudioChannelConfiguration", ns);
		adaptationSet.removeChild("Representation", ns);
		adaptationSet2.addContent(representation);
	    }
	}
	for (Attribute aS : period.getChild("AdaptationSet", ns)
		.getAttributes()) {
	    Attribute aS2 = aS.clone();
	    period.getChild("AdaptationSet", ns).removeAttribute(aS2);
	    adaptationSet2.setAttribute(aS2);
	}
	period.removeChildren("AdaptationSet", ns);
	period.addContent(adaptationSet2);
    }

    @Override
    public void eventHandle(WatchEvent<?> event, Path path)
	    throws MCASException {
	try {
	    String file = event.context().toString();
	    if (event.kind().equals(ENTRY_CREATE) && file.contains(".mp4")
		    && (!(file.contains("init")))) {
		encapsulateISOM(path, file);
	    }
	} catch (Exception e) {
	    throw new MCASException();
	}
    }

    private void encapsulateISOM(Path path, String file) throws MCASException {
	try {
	    String[] parsedName = file.split("_");
	    String level;
	    if (levels.containsKey(parsedName[2])) {
		level = parsedName[2];
	    } else {
		throw new MCASException();
	    }
	    String filename = "";
	    for (int i = 0; i < (parsedName.length - 1); i++) {
		filename += parsedName[i] + "_";
	    }
	    int seg = Integer
		    .parseInt(parsedName[parsedName.length - 1].substring(0,
			    parsedName[parsedName.length - 1].lastIndexOf(".")));
	    if (seg == 0) {
		System.out.println("Genero Init");
		Path init = Paths.get(path.toString(), filename);

		String type = "-t live ";
		String framerate = "-r 24 ";
		String time = "-d 0 ";
		;
		String fmt = "-f video ";
		String audioStreams = "-a 0 ";
		int levnum = 0;
		String levtmp = "";
		for (String levelname : this.levels.keySet()) {
		    this.levels.get(levelname);
		    levnum++;
		    levtmp += levelname + " ";
		}
		String lvls = "-l " + levnum + " " + levtmp;
		String mpd_cmd = "i2mpd " + type + framerate + time + lvls
			+ fmt + "-n " + this.title + "_" + this.profileName + " " + audioStreams + "-D "
			+ path.toString() + "/";
		CommandLine commandMpd = CommandLine.parse(mpd_cmd.trim());
		System.out.println(commandMpd);
		try {
		    executor_mpd.execute(commandMpd);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new MCASException();
		}
		Path mpd_file = Paths.get(path.toString(), this.title + "_" + this.profileName + ".mpd");
		uploader.upload(mpd_file);
		mpd_file.toFile().delete();

		String cmd = "i2test " + init.toString();
		CommandLine commandLine = CommandLine.parse(cmd.trim());
		System.out.println(commandLine.toString());
		try {
		    executor_init.execute(commandLine);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new MCASException();
		}

		Path init_file = Paths.get(init.toString() + "init.mp4");
		uploader.upload(init_file);
		init_file.toFile().delete();
	    }
	    if (seg > 0) {
		Path segment = Paths.get(path.toString(), filename + (--seg)
			+ ".mp4");
		String cmd = "MP4Box -dash 1 -frag 1 -rap -frag-rap -dash-profile main -segment-name %s_ -out "
			+ path.toString() + "/out.mpd " + segment.toString();

		CommandLine commandLine = CommandLine.parse(cmd.trim());
		try {
		    executor_isom.execute(commandLine);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new MCASException();
		}
		Path segment_isom = Paths.get(path.toString(), filename + (seg)
			+ "_1.m4s");
		uploader.upload(segment_isom);

		segment.toFile().delete();
		segment_isom.toFile().delete();
		// TODO descomentar, funciona
		/*
		 * if (seg >= windowLength) { uploader.deleteContent(filename +
		 * (seg - windowLength) + "_1.m4s");
		 * 
		 * }
		 */
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new MCASException();
	}
    }
}
