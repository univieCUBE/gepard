package org.gepard.client;

// Class for handling Gepard client configuration XML files

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import org.gepard.common.SAXFinishedException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

public class Config extends DefaultHandler {
	
	private static Config instance=null;
	private static String configFile=null;
	
	private HashMap<String,String> settings;
	
	public static void setConfigFile(String file) throws Exception {
		if (instance == null) {
			configFile = file; 
			// catch exception for now
			try {
				instance = new Config();
			} catch (Exception e) {
				// create Config instance without any backing XML file
				instance = new Config(true);
				// re-throw exception
				throw e;
			}
		}
	}
	
	public static String getConfigFileName() {
		return configFile;	                                  
	}
	
	public static Config getInstance() {
		
		return instance;
	}

	
	public String getStringVal(String name, String defval) {
		String temp = settings.get(name);
		if (temp != null)
			return temp;
		else
			return defval;
	}
	
	
	public int getIntVal(String name, int defval) {
		String temp = settings.get(name);
		if (temp != null)
			return Integer.parseInt(temp);
		else
			return defval;
			
	}
	
	public void setIntVal(String name, int val) {
		setVal(name, val+"");
	}
	
	public void setVal(String name, String val) {
		settings.remove(name);
		settings.put(name,val);
	}
	
	public void storeConfig() throws FileNotFoundException {
		// open output file
		PrintStream p = new PrintStream(
                new FileOutputStream(configFile));
		// write initial line
		p.println("<settings>");
		// iterate through hashmap
		Iterator<String> iterator = settings.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = settings.get(key);

			p.println("\t<setting name=\"" + key + "\" value=\"" + value + "\" />");
		}
		// write ending line
		p.println("</settings>");
		p.close();

	}
	
	private Config() throws Exception {
		// create XML parser
		SAXParser p = new SAXParser();
		// set object itsself as content handler
		p.setContentHandler(this);
		// create hashmap
		settings = new HashMap<String,String>();
		// start parsing
		p.parse(configFile);
	}
	
	private Config(boolean noFile) {
		// constructor with overloading dummy variable
		
		// create hashmap
		settings = new HashMap<String,String>();
	}
	
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXFinishedException {
		if (localName.toLowerCase().equals("setting")) 
			// add to hashmap
			settings.put(atts.getValue("name"), atts.getValue("value"));
	}
}