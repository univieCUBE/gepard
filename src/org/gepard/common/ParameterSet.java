package org.gepard.common;

import java.io.Serializable;

// represents one set of dotplot paramters

public class ParameterSet implements Serializable {

	private static final long serialVersionUID = 2991408205215417902L;

	public int zoom;
	public int wordLength;
	public int windowSize;
	public int seq1Start, seq1Stop;
	public int seq2Start, seq2Stop;
	public String submatName;
	
	
	private static final String paramDivider = "&";

	public ParameterSet(int iratio, int iwordlen, int iwinsize, int iseq1start,
			int iseq1stop, int iseq2start, int iseq2stop, String submatName) {
		
		this.zoom = iratio;
		this.wordLength = iwordlen;
		this.windowSize = iwinsize;
		this.seq1Start = iseq1start;
		this.seq1Stop = iseq1stop;
		this.seq2Start = iseq2start;
		this.seq2Stop = iseq2stop;
		this.submatName = submatName;
	}
	
	
	
	public ParameterSet() {
	}

	public ParameterSet getClone() {
		return new ParameterSet(zoom, wordLength, windowSize, seq1Start,
				seq1Stop, seq2Start, seq2Stop, submatName);
	}
	
	public String serialize() {
		return zoom + paramDivider + wordLength + paramDivider + windowSize + paramDivider +
			seq1Start + paramDivider + seq1Stop + paramDivider + seq2Start + 
			paramDivider + seq2Stop + paramDivider + submatName + paramDivider;
	}
	
	public String toString() {
		return "Zoom: " + zoom+ ", wordLength: " + wordLength+ ", windowSize: " + windowSize + ", " +
			"seq1: " + this.seq1Start + "-" + this.seq1Stop + ", " + 
			"seq2: " + this.seq2Start + "-" + this.seq2Stop + "  ";
	}
	
	// constructor which creates a parameter set from serialized data
	public ParameterSet(String serialized) {
		String[] values = serialized.split(paramDivider);

		zoom = Integer.parseInt(values[0]);
		wordLength = Integer.parseInt(values[1]);
		windowSize = Integer.parseInt(values[2]);
		seq1Start = Integer.parseInt(values[3]);
		seq1Stop = Integer.parseInt(values[4]);
		seq2Start = Integer.parseInt(values[5]);
		seq2Stop = Integer.parseInt(values[6]);
		submatName = values[7];
	}

}
