package org.gepard.common;

import java.io.IOException;

import org.gepard.client.ClientGlobals;

public class GenSAFile {
	
	public static void main(String[] args) throws IOException, InvalidSubMatFileException, InvalidFASTAFileException {
		
		if (args.length != 2) {
			System.err.println("Usage: GenSAFile <sequencefile> <outputfile>");
			System.exit(1);
		}
		
		System.out.print("Calculating suffix array...");
		System.out.flush();
		
		// load sequence
		SubstitutionMatrix edna = getEDNA();
		Sequence seq = FASTAReader.readFile(args[0], edna);
		
		new SuffixArray(seq, edna.getAlphabetSize(), null).saveToFile(args[1]);
		
		System.err.println("Done.");
		
	}
	
	
	private static SubstitutionMatrix getEDNA() throws IOException, InvalidSubMatFileException {
		return SubstitutionMatrix.loadFromResource(ClientGlobals.PATH_MATRICES + "edna.mat");
	}

}
