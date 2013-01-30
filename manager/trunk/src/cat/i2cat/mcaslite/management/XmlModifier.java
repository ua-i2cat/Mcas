package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlModifier {
	
	private static Namespace ns = Namespace.getNamespace("urn:mpeg:DASH:schema:MPD:2011");
	private static String inputFile = "/home/marc/apachePublic/7528e2ad-77bb-4259-a493-65e817ed1467/bunny_original.mpd";
	private static String outputFile = "/home/marc/apachePublic/7528e2ad-77bb-4259-a493-65e817ed1467/bunnyModified.mpd";
	public static void main(String[] args) {
	 
	try {
	 
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(inputFile);
	 
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			
			Iterator<Content> it = doc.getDescendants();
			while (it.hasNext()){
				Content cont = it.next();
				if (cont.getCType().equals(Content.CType.Element)){
					Element el = (Element)cont;
					el.setNamespace(ns);
				}
			}
			
			rootNode.getAttribute("profiles").setValue("urn:mpeg:dash:profile:isoff-main:2011");
			rootNode.removeChild("ProgramInformation", ns);
			
			Element representation = rootNode.getChild("Period", ns).getChild("AdaptationSet", ns).getChild("Representation", ns);
			Element segmentList = representation.getChild("SegmentList", ns);
			Element segmentBase = new Element("SegmentBase", ns);
			representation.addContent(segmentBase);
			segmentBase.addContent(segmentList.getChild("Initialization", ns).clone());
			segmentList.removeChild("Initialization", ns);
			

			XMLOutputter xmlOutput = new XMLOutputter();
	 
			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(outputFile));
	 
//			System.out.println("File updated!");
			
		  } catch (IOException io) {
			io.printStackTrace();
		  } catch (JDOMException e) {
			e.printStackTrace();
		  }
	}
}
