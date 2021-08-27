package org.gepard.common;

import java.io.File;
import java.io.IOException;


// very simple tool which provides a direct command-line interface
// to the Vmatch SA file generator class

public class GenSAFileVmatch {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		if (args.length != 2) {
			System.err.println("Usage: GenSAFileVmatch <sequencefile> <outputfile>");
			System.exit(1);
		}
		
		try {
			VmatchConverter.genSAFileFromVmatch(
					args[0],
					FASTAReader.countNuclSeqLength(args[0]),
					new File(args[1])
			);
			System.err.println("Done.");
		} catch (IOException e) {
			System.err.println("Error. Could not generate suffix array file using VMatch.");
			System.err.println("Message: " + e.getMessage());
			
		}

		
	}

}
