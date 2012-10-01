package cat.i2cat.mcaslite.entities;

import java.io.IOException;
import java.util.Properties;

public class ApplicationConfig {

	public static String getInputWorkingDir() {
		Properties prop = new Properties();
		try {
			prop.load(ApplicationConfig.class.getResourceAsStream("application.properties"));
			return prop.getProperty("InputWorkingDir");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getOutputWorkingDir() {
		Properties prop = new Properties();
		try {
			prop.load(ApplicationConfig.class.getResourceAsStream("application.properties"));
			return prop.getProperty("OutputWorkingDir");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int getMaxInMediaH() {
		Properties prop = new Properties();
		try {
			prop.load(ApplicationConfig.class.getResourceAsStream("application.properties"));
			return Integer.parseInt(prop.getProperty("MaxInMediaH"));
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static int getMaxOutMediaH() {
		Properties prop = new Properties();
		try {
			prop.load(ApplicationConfig.class.getResourceAsStream("application.properties"));
			return Integer.parseInt(prop.getProperty("MaxOutMediaH"));
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static int getMaxTransco() {
		Properties prop = new Properties();
		try {
			prop.load(ApplicationConfig.class.getResourceAsStream("application.properties"));
			return Integer.parseInt(prop.getProperty("MaxTransco"));
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
