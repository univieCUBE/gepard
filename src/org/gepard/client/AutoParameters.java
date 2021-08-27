package org.gepard.client;

import org.gepard.common.ParameterSet;

public class AutoParameters {
	
	private static final long WORDLENZERO_AUTOTHRESHOLD = 1500 * 1500;
	
	private static final long WARNING_THRESHOLD = 100 * (1000) * (1000);
	
	private static final int FIXED_WORDLENGTH = 10;
	
	private static final int CRITICAL_WORDLEN = 5;
	
	private static final int MAXSIZE_FOR_SEQDATA = 500000;
	
	// reads the coordinate data of a ParameterSet and 
	// sets the wordlen and winsize values automatically
	public static void setAutoParameters(ParameterSet params) {
		long dots = (long)(params.seq1Stop - params.seq1Start + 1) * (long)(params.seq2Stop - params.seq2Start + 1);
		
		if (dots > WORDLENZERO_AUTOTHRESHOLD) {
			params.wordLength = FIXED_WORDLENGTH;
			params.windowSize = 0;
		}
		else {
			params.wordLength = 0;
			params.windowSize = 25;//params.zoom * 20 + 30;
		}
	}
	
	// returns if the current parameters are critical 
	// (concerning computation time)
	public static boolean areCriticalParameters(ParameterSet params) {
		if (params.wordLength == 0) {
			long dots = (long)(params.seq1Stop - params.seq1Start + 1) * (long)(params.seq2Stop - params.seq2Start + 1);
			if (dots > WARNING_THRESHOLD)
				return true;
			else		
				return false;
		} else {
			if (params.wordLength <= CRITICAL_WORDLEN)
				return true;
			else
				return false;
		}
	}
	
	public static boolean mayRequestSequenceData(ParameterSet params) {
		return (!((params.seq1Stop - params.seq1Start + 1 > MAXSIZE_FOR_SEQDATA)
				|| (params.seq2Stop - params.seq2Start + 1 > MAXSIZE_FOR_SEQDATA)));
	
	}
	
	public static int getMaxSizeForSeqData() {
		return MAXSIZE_FOR_SEQDATA;
	}
	
}
