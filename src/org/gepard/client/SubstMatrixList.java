package org.gepard.client;

import java.io.IOException;
import java.util.Vector;

import org.gepard.common.SAXFinishedException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// the substitution matrix XML list reader 

public class SubstMatrixList extends DefaultHandler {
	
	private static SubstMatrixList instance=null;
	
	Vector<SubstMatrixFile> vec;

	public static SubstMatrixList getInstance() throws SAXException, IOException, ParserConfigurationException {
		if (instance == null)
			instance = new SubstMatrixList();
		return instance;
	}
	
	private SubstMatrixList() throws SAXException, IOException, ParserConfigurationException {
		// create XML parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
	    SAXParser p = factory.newSAXParser();
		// set object itsself as content handler	    
		DefaultHandler handler = this;
		// create vector		
		vec = new Vector<SubstMatrixFile>();
		// start parsing
//		System.out.println(ClientGlobals.PATH_MATRICES + ClientGlobals.FILE_MATRICES);
		p.parse(new InputSource(this.getClass().getResourceAsStream(ClientGlobals.PATH_MATRICES + ClientGlobals.FILE_MATRICES)), handler);
	}
	
	public SubstMatrixFile[] getMatrixFiles() {
		// create and fill array
		SubstMatrixFile[] ret = new SubstMatrixFile[vec.size()];
		vec.toArray(ret);
		// return array
		return ret;
	}

	
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXFinishedException {
		
		// check for <matrix> tag
		if (localName.toLowerCase().equals("matrix"))
			// add to vector
			vec.add(new SubstMatrixFile(
					atts.getValue("name"),
					atts.getValue("file"),
					atts.getValue("nucleotide").equals("1")
				));
	}
	
	
}
