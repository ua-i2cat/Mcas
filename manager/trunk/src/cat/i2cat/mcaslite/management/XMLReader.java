package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLReader {
	
	private String path;
	
	public XMLReader(String input){
		this.path = input;
	}
	
	public Document getDoc(String docName){
 		Document doc=null;
		try {
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(path.concat(docName));
			doc = (Document) builder.build(xmlFile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
 		return doc;
	}
	
	public HashMap<String, Element> mapElements(List<Element> elements){
		HashMap<String, Element> map = new HashMap<String, Element>();
		for(Element el : elements){
			map.put(el.getAttributeValue("name"), el);
		}
		return map;
	} 
	
	public List<Element> getConfigs(Document doc){
		Element el = doc.getRootElement();
		return (el.getChild("transcoconfigs").getChildren());
	}
	
	public List<Element> getRootChildrenElements(Document doc){
		Element el = doc.getRootElement();
		return el.getChildren();
	}
	
	public static String getElementName(Element el){
		return el.getAttributeValue("name");
	}
	
	public static String getParameter(Element el, String configAttribute){
			
	String returnItem = null;
	
	String[] children = configAttribute.split("\\.");	
	for (String child : children){
		el = el.getChild(child);
	}	
	returnItem =  el.getText();
		
	return returnItem;
		
	}
	
	
	
	
	
	
}
	
	
