package net.i2cat.mcas.management;

import net.i2cat.mcas.utils.Connection;

public class ClassFactory {
	 
	public static Callback getCallback(String callbackClass) {
		
		String className = callbackClass;
	 
	    Callback cb = null;
	 
	    try {
	    	if( className!=null && !className.isEmpty()) {
	    		Class<?> cls = Class.forName(className);
	            cb = (Callback)cls.newInstance();
	        } else {
	        	cb = new Callback();
	        }
	    }catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return cb;
	 }
	
	public static Connection getConnection(String connectionClass){
		String className = connectionClass;
		 
	    Connection conn = null;
	 
	    try {
	    	if( className!=null && !className.isEmpty()) {
	    		Class<?> cls = Class.forName(className);
	            conn = (Connection)cls.newInstance();
	        } else {
	        	conn = new Connection();
	        }
	    }catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return conn;
	 }
}
