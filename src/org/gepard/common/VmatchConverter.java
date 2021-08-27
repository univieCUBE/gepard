package org.gepard.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

// use Vmatch to create Gepard suffix array files

public class VmatchConverter {
	
	private static final int PREFIX_THRESHOLD = 8388608;

	private static final String[] MAPPING =
	//{"aA", "tT", "gG", "cC", "sS", "wW", "rR", "yY", "kK", "mM", "bB", "vV", "hH", "dD", "nN", "uU", "zZ", "xX",  "*"};
	{"aA", "tTuU", "gG", "cC", "nsywrkvbdhmNSYWRKVBDHM"};
	
	public static void genSAFileFromVmatch(String strinfile, int seqlen, File outfile) throws IOException, InterruptedException {
		genSAFileFromVmatch(strinfile, seqlen, outfile, "mkvtree");
	}
	
	public static void genSAFileFromVmatch(String strinfile, int seqlen, File outfile, String command) throws IOException, InterruptedException {
		
		String alifile = (System.currentTimeMillis() + "") + (seqlen + "");
		
		// write mapping file
		PrintWriter mapwriter = new PrintWriter(new FileWriter(alifile));
		for (int i=0; i<MAPPING.length; i++)
			mapwriter.println(MAPPING[i]);
		mapwriter.close();
		
		String filename = extractFilename(strinfile);
		
		// call the vmatch program
		String execute = command + " -db " + strinfile + " -lcp -suf -smap " + alifile;
		if (seqlen > PREFIX_THRESHOLD)
			execute += " -pl";
		
		System.out.println("Running: " + execute);
		
		final Process p = Runtime.getRuntime().exec(execute);
		p.waitFor();
		
		processVMatchFiles(filename, seqlen, outfile);
		
		// delete all temp files
		new File(filename + ".suf").delete();
		new File(filename + ".lcp").delete();
		new File(filename + ".prj").delete();
		new File(filename + ".llv").delete();
		new File(filename + ".al1").delete();
		new File(filename + ".des").delete();
		new File(filename + ".sds").delete();
		new File(alifile).delete();
		new File(alifile).deleteOnExit();
		
	}
	
	private static void processVMatchFiles(String filename, int seqlen, File outfile) throws IOException {
		int mkvtreesuflen = (int)new File(filename + ".suf").length();
		
		// check which version of mkvtree was used using the file size
		boolean bit64=false;
		if (mkvtreesuflen > (seqlen*4+4)) 
			bit64 = true;
		
		
		// create gzip stream to our suffix array file
		LEDataOutputStream safile =
			new LEDataOutputStream(
					new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outfile))));
		
		// write type 1 (vmatch suffix array)
		safile.write(1);
		
		
		// write sequence length
		safile.writeInt(seqlen);
	
		
		// copy suffix array data
		LEDataInputStream suffile =
			new LEDataInputStream(new BufferedInputStream(new FileInputStream(filename + ".suf")));
		for (int i=0; i<seqlen; i++) {
			safile.writeInt(suffile.readInt());
			if (bit64) suffile.readInt(); // leave out 4 bytes in 64 bit mode
		}
		suffile.close();
	
		
		// now transfer LCP data, leave out first and last byte
		LEDataInputStream lcpfile =
			new LEDataInputStream(new BufferedInputStream(new FileInputStream(filename + ".lcp")));
		lcpfile.skipBytes(1);
		short[] lcptab = new short[seqlen-1];
		for (int i=0; i<seqlen-1; i++) {
			byte test = lcpfile.readByte();
			lcptab[i] = byte2unsignedshort(test);
			safile.writeByte(test);
		}
		lcpfile.close();
		
		
		// open stream to LLV array file
		LEDataInputStream llv = new LEDataInputStream(
				new BufferedInputStream(new FileInputStream(filename + ".llv")));
		// read everything
		boolean eof=false;
		while (!eof) {
			try {
				// write big lcp information
				safile.writeInt(llv.readInt()); 	// suffix
				if (bit64) llv.readInt(); // leave out 4 bytes in 64 bit mode
				safile.writeInt(llv.readInt());		// lcp
				if (bit64) llv.readInt(); // leave out 4 bytes in 64 bit mode
				
			} catch (EOFException e) {
				eof = true;
			}
		}
		
		llv.close();
		
		
		safile.close();
		
	
	
	
	}

	
	private static String extractFilename(String path) {
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}
	
	private static short byte2unsignedshort(byte i) {
		if (i < 0) {
			return (short)(256 + i);
		} else {
			return i;
		}
	}
	
	

}
