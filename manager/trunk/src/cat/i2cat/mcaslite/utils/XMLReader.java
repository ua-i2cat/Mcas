package cat.i2cat.mcaslite.utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
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
			File xmlFile = new File(Paths.get(path, docName).toString());
			doc = (Document) builder.build(xmlFile);
		} catch (Exception e) {
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
	
	private static String getParameter(Element el, String configAttribute){	
		String[] children = configAttribute.split("\\.");	
		for (String child : children){
			el = el.getChild(child);
		}
		if (el != null) {
			return el.getText();
		} else {
			return null;
		}
	}
	
	public static int getIntParameter(Element el, String configAttribute){
		String str = getParameter(el, configAttribute);
		if (str != null && ! str.isEmpty()){
			try {
				return Integer.parseInt(str);
			} catch (Exception e) {}
		}
		return 0;
	}
	
	public static String getStringParameter(Element el, String configAttribute){
		String str = getParameter(el, configAttribute);
		if (str == null){
			return "";
		} else {
			return str;
		}
	}
	
	public static String getXMLParameter(String path, String param){
		String returnItem = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(path);
			Document doc;
			doc = (Document) builder.build(xmlFile);
			Element el = doc.getRootElement();
			String[] children = param.split("\\.");	
			for (String child : children){
				el = el.getChild(child);
			}	
			returnItem =  el.getText();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnItem;
	}	
}
	
	
