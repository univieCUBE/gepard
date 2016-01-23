package org.gepard.client;

import java.io.IOException;
import java.util.Vector;

import org.gepard.common.SAXFinishedException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

// the substitution matrix XML list reader 

public class SubstMatrixList extends DefaultHandler {
	
	private static SubstMatrixList instance=null;
	
	Vector<SubstMatrixFile> vec;

	public static SubstMatrixList getInstance() throws SAXException, IOException {
		if (instance == null)
			instance = new SubstMatrixList();
		return instance;
	}
	
	private SubstMatrixList() throws SAXException, IOException {
		// create XML parser
		SAXParser p = new SAXParser();
		// set object itsself as content handler
		p.setContentHandler(this);
		// create vector		
		vec = new Vector<SubstMatrixFile>();
		// start parsing
//		System.out.println(ClientGlobals.PATH_MATRICES + ClientGlobals.FILE_MATRICES);
		p.parse(new InputSource(this.getClass().getResourceAsStream(ClientGlobals.PATH_MATRICES + ClientGlobals.FILE_MATRICES)));
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
