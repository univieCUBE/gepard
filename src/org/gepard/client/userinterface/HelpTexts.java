package org.gepard.client.userinterface;

import java.util.HashMap;

import javax.swing.JOptionPane;

import org.gepard.client.ClientGlobals;
import org.gepard.common.SAXFinishedException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// reads and serves all help tooltips from help/help.xml

public class HelpTexts extends DefaultHandler {
	
	private static HelpTexts instance=null;
	
	private String curID=null;
	private String curText=null;
	private HashMap<String,String> texts;
	

	public static HelpTexts getInstance() throws ParserConfigurationException, SAXException {
		if (instance == null)
			instance = new HelpTexts();
		return instance;
	}
	
	public String getHelpText(String id) {
		String ret = texts.get(id);
		if (ret == null)
			return "HELP TEXT WITH ID '" + id+ "' DOES NOT EXIST!";
		else
			return ret;
	}
	
	private HelpTexts() throws ParserConfigurationException, SAXException {
		// create XML parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser p = factory.newSAXParser();
		// set object itsself as content handler	    
		DefaultHandler handler = this;
		// create hashmap
		texts = new HashMap<String,String>();
		// start parsing
		try {
			p.parse(new InputSource(this.getClass().getResourceAsStream(ClientGlobals.FILE_HELP)), handler);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Could not load help file '"+ClientGlobals.FILE_HELP+"'.\n\nError:\n" + e.getMessage(),"Error", JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXFinishedException {
		
		if (localName.toLowerCase().equals("helpitem")) {
			curID = atts.getValue("id");
		}		
	}
	
	public void endElement(String uri, String localName, String qName) 
			throws SAXFinishedException {
		
		if (localName.toLowerCase().equals("helpitem")) {
			// helpitem complete, add to hashtable
			texts.put(curID, "<html>"+curText.trim().replace(new StringBuffer("\n"),new StringBuffer("<br>"))+"</html>");
			curID = null;
			curText = null;
		}		
	}
	
	public void characters(char[] ch, int start, int length) {
		// if within a helpitem -> append to text
		if (curID != null) {
			if (curText == null) curText ="";
			curText += new String(ch,start,length).trim() + "\n";
		}
	}


}