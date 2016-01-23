package org.gepard.common;

public class CompressedDotMatrix {

	public byte[] dotmatrix;
	public int newMaxScore;
	public int newMinScore;
	
	public CompressedDotMatrix(byte[] dotmatrix, int newMaxScore, int newMinScore) {
		this.dotmatrix = dotmatrix;
		this.newMaxScore = newMaxScore;
		this.newMinScore = newMinScore;
	}
	
}
