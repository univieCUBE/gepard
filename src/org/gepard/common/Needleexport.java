package org.gepard.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gepard.client.ClientGlobals;

public class Needleexport {
	public static void export(Sequence sequence1, Sequence sequence2,int tX,int tY, String path1, int window, boolean complementary, SubstitutionMatrix submat) {
		int backward = window / 2;
		int forward = window / 2;
		byte[] seq1 = sequence1.getSequenceData();
		byte[] seq2 = sequence2.getSequenceData();
		// empty position byte arrays
		char [] strpos1 = new char [window];
		char [] strpos2 = new char [window];
		Arrays.fill(strpos1, ' '); 
		Arrays.fill(strpos2, ' ');
		
		byte[] complmap = Sequence.getComplementaryMap();
		char[] strseq1 = new char[window];
		char[] strseq2 = new char[window];
		char [] strsim = new char [window];
		// iterate through range
		int j=0;
		for (int i=-backward; i<=forward&&j<window; i++) {
			byte a=-1, b=-1;
			// get characters from both sequences
			if (tX+i >= 0 && tX+i < seq1.length) 
				a = seq1[tX+i];
			if (!complementary) {
				if (tY+i >= 0 && tY+i < seq2.length)
					b = seq2[tY+i];
			} else {
				if (tY-i >= 0 && tY-i < seq2.length)
					b = complmap[seq2[tY-i]];
			}
			
				// insert characters into strings
			if (a != -1) 	strseq1[j] = submat.reverseMap(a);
			else 			strseq1[j] = ' ';
			
			if (b != -1) 	strseq2[j] = submat.reverseMap(b);
			else 			strseq2[j] = ' ';
			j++;	
		}
		int [][] scoringmatrix = {
				{5,-3,-3,-3},
				{-3,5,-3,-3},
				{-3,-3,5,-3},
				{-3,-3,-3,5}
			};
		if (!submat.isNucleotideMatrix()) {
			scoringmatrix = submat.getMatrix();
		}
		NeedlemanWunsch matrices = NeedlemanWunsch.GetAlignmentMatrix(String.valueOf(strseq1), String.valueOf(strseq2), scoringmatrix, submat);
		List<String> ans = new ArrayList<String>(NeedlemanWunsch.GetAlignment(String.valueOf(strseq1), String.valueOf(strseq2), scoringmatrix, matrices.align_matrix, matrices.matrix_pointer));
		strseq1 = new char[window];
		strseq2 = new char[window];
		char [] finalstr1 = new char[ans.get(0).length()];
		char [] finalstr2 = new char[ans.get(0).length()];
		for (int a = 0; a<ans.get(0).length(); a ++) {
			finalstr1[a] = (char) ans.get(0).charAt(a);
		}
		for (int a = 0; a<ans.get(1).length(); a ++) {
			finalstr2[a] = (char) ans.get(1).charAt(a);
		}
		for (int a = 0; a<strseq1.length&&a<window; a++) {
			if (strseq1[a] == strseq2[a] && strseq1[a] != ' ') {
				strsim[a] = getNuclSimCode(submat.map((byte) strseq1[a]), submat.map((byte) strseq1[a]), submat);;
			}
			else {
				strsim[a] = ' ';
			}
		}
		int FASTA_COLS = 70;
		// open file
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(path1));
			String rangeString = ", range: " + ((tX -(window / 2)) + Integer.parseInt(ans.get(2))) + "-"+((tX-(window / 2)) + Integer.parseInt(ans.get(4)));
			writer.println(">" + ClientGlobals.cutString(sequence1.getName(), FASTA_COLS -rangeString.length()-1) + rangeString);
			// iterate over range
			int col=0;
			for (int i=0; i<finalstr1.length; i++) {
				writer.print(finalstr1[i]);
				col++;
				if (col % FASTA_COLS == 0) writer.println();
			}
			if (col % FASTA_COLS != 0) writer.println();
			String rangeString2 = ", range: " + ((tY -(window / 2)) + Integer.parseInt(ans.get(3))) + "-"+((tY -(window / 2)) + Integer.parseInt(ans.get(5)));
			writer.println(">" + ClientGlobals.cutString(sequence2.getName(), FASTA_COLS -rangeString2.length()-1) + rangeString2);
			// iterate over range
			col=0;
			for (int i=0; i<finalstr2.length; i++) {
				writer.print(finalstr2[i]);
				col++;
				if (col % FASTA_COLS == 0) writer.println();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Arrays.fill(strseq1, ' '); 
		Arrays.fill(strseq2, ' ');
		Arrays.fill(finalstr1, ' '); 
		Arrays.fill(finalstr2, ' ');
	}

	private static char getNuclSimCode(byte map, byte map2, SubstitutionMatrix submat) {
		if (map != -1 && map2 != -1) {
			if (map==map2)
				return ':';
			else {
				int score = submat.getScore(map,map2);
				if (score > 0)
					return '.';
				else
					return ' ';
			}
		}
		else
			return ' ';
	}
}
