package org.gepard.client;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.gepard.common.InvalidSubMatFileException;
import org.gepard.common.SubstitutionMatrix;

// global constants, variables and functions for the Gepard client

public class ClientGlobals {
	
	public static boolean useVmatch=true;
	public static String vmatchCommand;
	
	public static final String VERSION = "1.40 final";
	public static final String AUTHOR_EMAIL = "contact.cube@univie.ac.at";
	public static final String WEB_ADRESS = "http://cube.univie.ac.at/gepard";
	
	public static String SETTINGS_DIR = System.getProperty("user.home") + 
		System.getProperty("file.separator") + ".gepard/"; 
	
	public static final int CONTROLPANEL_WIDTH = 260;
	public static final int MINWORDLEN = 1;
	public static final int MAXWORDLEN = 32767;
	public static final int MINWINDOWSIZE = 0;
	public static final int MAXWINDOWSIZE = 10000;
	public static final int MINZOOM = 0;
	public static final int MAXZOOM = 1000000;
	public static final int MAXNAMELEN_TITLE = 50;
	
	
	
	public static final int MINVMATCHLENGTH = 50000;

	public static final float ZOOMINFACTOR =0.5f;
	public static final float ZOOMOUTFACTOR =2f;

	public static final int BIGKEYBOARDSTEP = 25;
	
	public static String APPNAME = "Gepard";
	
	public static final String PATH_MATRICES = "/resources/matrices/";
	public static final String FILE_MATRICES = "matrices.xml";
	public static final String AUTO_NUCL_MATRIX = "edna.mat";
	public static final String AUTO_NUCL_MATRIX_NAME = "DNA";
	public static final String AUTO_PROT_MATRIX = "blosum62.mat";
	public static final String AUTO_PROT_MATRIX_NAME = "BLOSUM62";

	public static final String FILE_HELP = "/resources/help/help.xml";
	public static final String FILE_USERMSG = "/resources/help/usermsg.txt";
	public static final String FILE_REFERENCE = "/resources/help/reference.txt";

	public static final String CONFIG_FILE = "config.xml";
	
	public static final int LOCALCLICK_SHOWALIGN = 0;
	public static final int LOCALCLICK_EXPORT = 1;
	public static final int LOCALCLICK_NOTHING = 2;
	
	public static final String NOGENE_STRING = "-";
	
	public static final String RES_APPICON = "/resources/images/gepard.gif";
	
	public static final String GENE_NAME_HOR_PREFIX  = "Hor : ";
	public static final String GENE_NAME_VERT_PREFIX = "Vert: ";
	

	public static String cutString(String str, int maxlen) {
		
		if (maxlen <= 3)
			return "";
		else if (str.length() > maxlen)
			return str.substring(0, maxlen - 3) + "...";
		else
			return str;
	}

	public static SubstitutionMatrix getEDNA() throws IOException, InvalidSubMatFileException {
//		return SubstitutionMatrix.debugTrivialMatrix();
		return SubstitutionMatrix.loadFromResource(ClientGlobals.PATH_MATRICES + "edna.mat");
	}

	public static void errMessage(String msg) {
		JOptionPane.showMessageDialog( 
				null, msg, "Error", JOptionPane.ERROR_MESSAGE );
	}
	
	public static void warnMessage(String msg) {
		JOptionPane.showMessageDialog( 
				null, msg, "Warning", JOptionPane.WARNING_MESSAGE );
	}
	
	public static void infoMessage(String msg) {
		JOptionPane.showMessageDialog( 
				null, msg, "Information", JOptionPane.INFORMATION_MESSAGE );
	}
	
	public static void copyTextToClipboard(String text) {
		Clipboard systemClipboard =
			Toolkit.getDefaultToolkit().getSystemClipboard();
		// create transferrable text object and set clipboard text
		Transferable transferableText =	new StringSelection(text);
		systemClipboard.setContents(transferableText,null);
	}
	
	
	private static NumberFormat FMT_FILESIZE = new DecimalFormat("###0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static String convenientFileSize(long fileSize) {
		if (fileSize >= 1073741824) 
			return FMT_FILESIZE.format((double)fileSize / (double)1073741824) + " gb";
		else if (fileSize >= 1048576) 
			return FMT_FILESIZE.format((double)fileSize / (double)1048576) + " mb";
		else if (fileSize >= 1024) 
			return FMT_FILESIZE.format((double)fileSize / (double)1024) + " kb";
		else
			return fileSize + " b";
		
	}
	
	private static NumberFormat FMT_NUMBERS = new DecimalFormat("###,###,###,###", new DecimalFormatSymbols(Locale.ENGLISH));

	public static String formatNumber(int number) {
		return FMT_NUMBERS.format(number);
	}
	
	
	public static void unexpectedError(Exception e, Controller ctrl) {
		
		// handles any unexpected errors
		// asks the user to send an error report to the developer
		
		String guidump = ctrl.getGUIDump();
		String msg;
		msg = "An unexpected error occured!\nStack trace & GUI details copied to clipboard.";
		JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
		
		String report = stack2string(e) + "\n\n\n\n" + guidump;
		StringSelection stringSelection = new StringSelection (report);
		Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
		clpbrd.setContents (stringSelection, null);
	
	}
	
	
	public static String stack2string(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "------\r\n" + sw.toString() + "------\r\n";
		} catch (Exception e2) {
			return "bad stack2string";
		}
	}
	
	
	public static String extractFilename(String filename) {
		return filename.substring(filename.lastIndexOf(File.separator)+1);
	}
	
	public static String extractDirectory(String filename) {
		return filename.substring(0,filename.lastIndexOf(File.separator)+1);
	}
	
	public static String createFilenameFromName(String name, int maxlen) {
		
		name = name.replaceAll(" ", "_");
		if (name.length() > maxlen)
			name = name.substring(0, maxlen);
		
		return name + ".fa";
	}
	
	public static String insertBeforeExtension(String filename, String insert) {
		int lastDot = filename.lastIndexOf('.');
		int lastSep = filename.lastIndexOf(File.separator);

		if (lastDot > lastSep && lastDot >= 0) 
			return filename.substring(0, lastDot) + insert+ filename.substring(lastDot);
		else
			return filename + insert;
	}
	
}
