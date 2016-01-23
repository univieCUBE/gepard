package org.gepard.client;

import org.gepard.common.ParameterSet;

public class GeneNames {
	
	String[] genes1, genes2;
	
	public GeneNames(ParameterSet params) {
		
		// calc array sizes
		int len1 = (int) ( ((double)(params.seq1Stop - params.seq1Start + 1) / (double)params.zoom) + 1 );
		int len2 = (int) ( ((double)(params.seq2Stop - params.seq2Start + 1) / (double)params.zoom) + 1 );
		// and create the array
		genes1 = new String[len1];
		genes2 = new String[len2];
	}
	
	public void setGeneName(boolean firstseq, int from, int to, String name) {
		
		// shortcut array
		String[] names = (firstseq) ? genes1 : genes2;
		// and fill in gene name
		for (int i=from; i<=to; i++)
			names[i] = name;

	}
	
	public String getGeneName(boolean firstseq, int pos) {
		// shortcut array
		String[] names = (firstseq) ? genes1 : genes2;
		// return name or no-gene string
		
		if (names[pos] != null) 
			return names[pos];
		else
			return ClientGlobals.NOGENE_STRING;
	}

}
