package cat.i2cat.mcaslite.management;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;
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
			if (event.kind().equals(ENTRY_CREATE) && file.contains(".mp4")) {
				// CALL MP4Box
				System.out.println("File name: " + file);
				encapsulateISOM(path, file);
				// updateVideoFiles(path, file);
			}
		} catch (Exception e) {
			throw new MCASException();
		}
	}

	private void encapsulateISOM(Path path, String file) throws MCASException {
		// former updateVideoFiles HLS
		try {
			String[] parsedName = file.split("_");
			String level;
			if (levels.containsKey(parsedName[2])) {
				level = parsedName[2];
			} else {
				throw new MCASException();
			}
			// TODO change dashManifestOption constructor to acquiere title and
			// profileName
			String filename = MediaUtils.fileNameMakerByLevel(title,
					profileName, level);
			int seg = Integer.parseInt(parsedName[3].substring(0,
					parsedName[3].lastIndexOf(".")));
			if (seg == 0) {
				// TODO init generation
				Path init = Paths.get(path.toString(), filename + "_init.mp4");

				String cmd = "i2test " + init.toString();
				CommandLine commandLine = CommandLine.parse(cmd.trim());
				System.out.println(commandLine.toString());
				try {
					executor_init.execute(commandLine);
				} catch (Exception e) {
					e.printStackTrace();
					throw new MCASException();
				}
				uploader.upload(init);
				init.toFile().delete();
			}
			if (seg > 0) {
				Path segment = Paths.get(path.toString(), filename + "_"
						+ (--seg) + ".mp4");
				// TODO isom generation
				Path segment_isom = Paths.get(path.toString(), filename + "_"
						+ (--seg) + ".m4s");
				uploader.upload(segment_isom);
				segment.toFile().delete();
				segment_isom.toFile().delete();
				// TODO descomentar
				// if (seg >= windowLength /*TODO && request.getTconfig() name
				// == live*/ )
				// uploader.deleteContent(filename + "_" + (seg - windowLength)
				// + ".m4s");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}
}
