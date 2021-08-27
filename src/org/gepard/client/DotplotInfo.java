package org.gepard.client;

import org.gepard.common.ParameterSet;
import org.gepard.common.Sequence;
import org.gepard.common.SubstitutionMatrix;
import org.gepard.common.SuffixArray;

// represents information about the currently display dotpot

public class DotplotInfo {

	public int seq1len, seq2len;
	public int dpWidth, dpHeight;
	public SuffixArray suffixArray;
	public boolean SAforSecondSeq;
	public ParameterSet params;
	
	public int compseq;
	
	public SubstitutionMatrix submat;
	
	// local-only information
	public String seqFile1;
	public String seqFile2;
	
	// actual sequence data
	public Sequence sequence1, sequence2;
	
	// constructor for quick attribute settings
	public DotplotInfo(int seq1len, int seq2len, int dpWidth, int dpHeight,
			SuffixArray suffixArray, boolean SAforSecondSeq, ParameterSet params, SubstitutionMatrix submat,
			boolean isLocalPlot, int compseq, String seqFile1, String seqFile2) {
		
		this.seq1len = seq1len;
		this.seq2len = seq2len;
		this.dpWidth = dpWidth;
		this.dpHeight = dpHeight;
		this.suffixArray = suffixArray;
		this.SAforSecondSeq = SAforSecondSeq;
		this.params = params;
		this.submat = submat;
		this.compseq = compseq;
		this.seqFile1 = seqFile1;
		this.seqFile2 = seqFile2;
	}
	
	
}
