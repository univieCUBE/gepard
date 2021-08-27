package org.gepard.common;

import java.util.List;

// represents one sequences read by the FASTAReader

public class Sequence {
	
	private byte[] sequenceData;
	private String name;
	private boolean likelynucleotides;
	private boolean invalidchars;
	private boolean complementary=false;
	
	private boolean isMulti;
	private String[] multiNames;
	private int[] multiStarts;
	
	public Sequence(byte[] sequence, String name, boolean likelynucleotides, boolean invalidchars) {
		this.name = name;
		this.sequenceData = sequence;
		this.likelynucleotides = likelynucleotides;
		this.invalidchars = invalidchars;
	}
	
	// accessor methods
	public byte[] getSequenceData() { return sequenceData; }
	public String getName() { return ( (complementary) ? "[compl.]" : "").concat(name); }
	public int getLength() { return sequenceData.length; }
	public boolean likelyNucleotides() { return likelynucleotides; }
	public boolean hasInvalidChars() { return invalidchars; }
	
	public void setComplementaryFlag() {
		complementary = true;
	}
	
	
	private static byte[] complement=null;
	public static void complementarizeSequence(Sequence sequence) {
		// initialize table?
		if (complement == null) 
			complement = getComplementaryMap();
		
		
		byte[] seq = sequence.getSequenceData();
		for (int i=0; i<seq.length; i++)
			seq[i] = complement[seq[i]];
		
		
		sequence.setComplementaryFlag();
		
		// 1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17
		// A  T  G  C  S  W  R  Y  K  M  B  V  H  D  N  U  Z
		// T  A  C  G  S  W  Y  R  M  K  V  B  D  H  N  A  Z
		
		/*
		A         A         T
		C         C         G
		G         G         C
		T/U         T         A
		M         A/C         K
		R         A/G         Y
		W         A/T         W
		S         C/G         S
		Y         C/T         R
		K         G/T         M
		V         A/C/G         B
		H         A/C/T         D
		D         A/G/T         H
		B         C/G/T         V
		X/N         A/C/G/T         X
		.         None         .
		*/
	}
	
	public static byte[] getComplementaryMap() {
		byte[] complement = new byte[128];
		complement[1] = 2;
		complement[2] = 1;
		complement[3] = 4;
		complement[4] = 3;
		complement[5] = 5;
		/*
		complement[6] = 6;
		complement[7] = 8;
		complement[8] = 7;
		complement[9] = 10;
		complement[10] = 9;
		complement[11] = 12;
		complement[12] = 11;
		complement[13] = 14;
		complement[14] = 13;
		complement[15] = 15;
		complement[16] = 1;
		complement[17] = 17;*/
		return complement;
	}

	
	// multi-fasta stuff
	public void setMulti(List<String> names, List<Integer> starts) {
		multiNames = names.toArray(new String[0]);
		multiStarts = new int[starts.size()];
		int i=0;
		for (int v : starts)
			multiStarts[i++]=v;
		isMulti = true;
	}
	
	public boolean isMulti() {
		return isMulti;
	}
	
	public String[] getMultiNames() {
		return multiNames;
	}

	public int[] getMultiStarts() {
		return multiStarts;
	}
}
