package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Iterator;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class DashManifestManager implements FileEventProcessor {
	
	private static Namespace ns = Namespace.getNamespace("urn:mpeg:DASH:schema:MPD:2011");
	private String output;
	
	public DashManifestManager(){}
	
	public DashManifestManager(String output){
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
			
			changeNamespace(doc, ns);				
			rootNode.getAttribute("profiles").setValue("urn:mpeg:dash:profile:isoff-main:2011");
			rootNode.removeChild("ProgramInformation", ns);	
			changeInitialization(rootNode);
			
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(mpdFile));
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new MCASException();
		} 
	}
						
	private void changeInitialization(Element rootNode) {
		Element representation = rootNode.getChild("Period", ns).getChild("AdaptationSet", ns).getChild("Representation", ns);
		Element segmentList = representation.getChild("SegmentList", ns);
		Element segmentBase = new Element("SegmentBase", ns);
		representation.addContent(segmentBase);
		segmentBase.addContent(segmentList.getChild("Initialization", ns).clone());
		segmentList.removeChild("Initialization", ns);
	}

	public void changeNamespace(Document doc, Namespace ns){
		Iterator<Content> it = doc.getDescendants();
		while (it.hasNext()){
			Content cont = it.next();
			if (cont.getCType().equals(Content.CType.Element)){
				Element el = (Element)cont;
				el.setNamespace(ns);
			}
		}
	}
	
	@Override
	public void eventHandle(WatchEvent<?> event, Path path)
			throws MCASException {
	}	
}
