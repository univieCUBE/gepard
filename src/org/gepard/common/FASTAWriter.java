package org.gepard.common;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.gepard.client.ClientGlobals;

public class FASTAWriter {
	
	private static final int FASTA_COLS = 70;

	public static void writeFASTAFile(String file, Sequence sequence, 
			int from, int length, SubstitutionMatrix submat) throws IOException {
		writeFASTAFile(file, sequence.getSequenceData(), sequence.getName(), from, length, 0, submat);
	}

	public static void writeFASTAFile(String file, byte[] seq, String seqname, 
			int from, int length, int offset, SubstitutionMatrix submat) throws IOException {

		// open file
		PrintWriter writer = new PrintWriter(new FileWriter(file));

		// write fasta header
		String rangeString = ", range: " + from + "-"+(from+length-1);
		writer.println(">" + ClientGlobals.cutString(seqname, FASTA_COLS-rangeString.length()-1) + rangeString);
		// iterate over range
		int col=0;
		for (int i=from; i<from+length; i++) {
			writer.print(submat.reverseMap(seq[i-offset]));
			col++;
			if (col % FASTA_COLS == 0) writer.println();

		}

		// done
		writer.close();

	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InvalidFASTAFileException, InvalidSubMatFileException {
		Sequence seq = FASTAReader.readFile("/home/jan/scratch/seqtest/humain14.fasta", ClientGlobals.getEDNA());
		
		FASTAWriter.writeFASTAFile("/home/jan/myout", seq, 100, 700, ClientGlobals.getEDNA());
	}

}
