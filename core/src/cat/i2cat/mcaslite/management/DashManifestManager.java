package cat.i2cat.mcaslite.management;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.utils.MediaUtils;

public class DashManifestManager implements FileEventProcessor {

	private String output;

	public DashManifestManager() {
	}

	public DashManifestManager(String output) {
		this.output = output;
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
			if (event.kind().equals(ENTRY_CREATE) && file.contains(".mp4")){
				//CALL MP4Box
				System.out.println("File name: " + file);
				encapsulateISOM(path, file);
				//updateVideoFiles(path, file);
			}
		} catch(Exception e){
			throw new MCASException();
		}
	}

	private void encapsulateISOM(Path path, String file) {
		/*try {
			String[] parsedName = file.split("_");
			String level;
			if (levels.containsKey(parsedName[2])){
				level = parsedName[2];
			} else {
				throw new MCASException();
			}
			String filename = MediaUtils.fileNameMakerByLevel(title, profileName, level);
			int seg = Integer.parseInt(parsedName[3].substring(0, parsedName[3].lastIndexOf(".")));
			if (seg > 0){
				Path segment = Paths.get(path.toString(), filename + "_" + (--seg) + ".ts");
				uploader.upload(segment);
				segment.toFile().delete();
				if (seg >= windowLength){
					uploader.upload(createManifest(seg, filename), filename + ".m3u8");
					uploader.deleteContent(filename + "_" + (seg - windowLength) + ".ts");
				} else {
					uploader.upload(createManifest(seg, filename), filename + ".m3u8");
				}
			} else if (! mainCreated) {
				createMainManifest();
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}*/
	}
}
