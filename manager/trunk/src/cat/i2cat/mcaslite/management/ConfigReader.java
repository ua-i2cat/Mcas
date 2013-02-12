package cat.i2cat.mcaslite.management;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class ConfigReader {
		
	private static String inputFile = FilenameUtils.concat(System.getProperty("mcas.home"),"META-INF/config.xml");
		
	public static String configGetter(String configAttribute){
			
	String returnItem = null;
			
	try { 
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(inputFile);
	 
			Document doc = (Document) builder.build(xmlFile);
			
			Element el = doc.getRootElement();
			String[] children = configAttribute.split("\\.");
			
			for (String child : children){
					el = el.getChild(child);
			}
			
			returnItem =  el.getText();
				
		  } catch (IOException io) {
			io.printStackTrace();
		  } catch (JDOMException e) {
			e.printStackTrace();
		  }
		
	return returnItem;
		
	}
}
	
	
}