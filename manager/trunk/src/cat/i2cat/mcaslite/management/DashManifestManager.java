package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import cat.i2cat.mcaslite.exceptions.MCASException;

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
		// TODO Auto-generated method stub
	}
}
