package org.gepard.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

// represents a substitution matrix and contains 
// the file loading methods

public class SubstitutionMatrix {

	private static final int PROTEIN_THRESHOLD = 20;
	
	private int[][] scoreMatrix;
	private byte[] charMapping;
	private char[] revMapping;
	private boolean bNucleotideMatrix=true;

	private SubstitutionMatrix(int[][] scorematrix, byte[] charMapping, char[] revMapping) {
		this.scoreMatrix = scorematrix;
		this.charMapping = charMapping;
		this.revMapping = revMapping;
	}

	public int getScore(byte a, byte b) {
		return scoreMatrix[a][b];
	}
	
	public int[][] getMatrix() {
		return scoreMatrix;
	}
	
	public byte map(byte input) {
		return charMapping[input];
	}
	
	public char reverseMap(byte input) {
		return revMapping[input];
	}
	
	public int getAlphabetSize() {
		return scoreMatrix[0].length-1;
	}
	
	public boolean isNucleotideMatrix() {
		return bNucleotideMatrix;
	}
	
	public void setNucleotideMatrix(boolean b) {
		bNucleotideMatrix = b;
	}

	public static SubstitutionMatrix loadFromResource(String file)
			throws IOException, InvalidSubMatFileException {
		
		// open file
		BufferedReader br = new BufferedReader(new InputStreamReader(
				SubstitutionMatrix.class.getResourceAsStream(file)));
		
		return doLoad(br, file);
	}
	
	public static SubstitutionMatrix loadFromFile(String file)
			throws IOException, InvalidSubMatFileException {
		
		// open file
		BufferedReader br = new BufferedReader(new FileReader(file));

		return doLoad(br, file);
	}
	
		
	private static SubstitutionMatrix doLoad(BufferedReader br, String file) throws IOException, InvalidSubMatFileException {

		String curLine;
		boolean firstLine = true;
		// create character mapping array
		byte[] revMapping = new byte[256];
		// initialize with -2 -> invalid character
		//Arrays.fill(charMapping,(byte)-2);
		// set CR and LF to ignore -> -1
		revMapping[13] = -1;
		revMapping[10] = -1;
		// backmapping array
		char[] backMapping = new char[256];
		
		int[][] matrix = null;
		int mapPos = 1;
		int linenum=0;
		int reallinenum=0;
		// parse, read line by line
		while ((curLine = br.readLine()) != null) {
			reallinenum++;
			// ignore comments
			if ((curLine.length() > 0 ) && (curLine.charAt(0) != '#')) {
				if (firstLine) {
					// first line containing data -> table heading
					// e.g. A R N D C Q E G H I L K M F P S T W Y V B Z X *
					// create character mapping
					for (int i = 0; i < curLine.length(); i++) {
						if (curLine.charAt(i) != ' ') {
							// throw exception if character already defined
							if (revMapping[curLine.charAt(i)] != 0)
								throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - character defined twice: " + curLine.charAt(i));
							else {
								revMapping[curLine.charAt(i)] = (byte)mapPos++;
								// if this is not the last character a whitespace must follow
								if ( (i<(curLine.length()-1)) && (curLine.charAt(i+1)!=' '))
									throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid expression: " + curLine.substring(i,i+2) );
								
							}
						}						
						
					}
					// create backward mapping 
					for (int i=0; i<256; i++)
						if (revMapping[i] >= 0)
							backMapping[revMapping[i]] = (char)i;
					// create scorematrix array
					matrix = new int[mapPos][mapPos];
					// set flag
					firstLine = false;

				} else {
					// all the other lines contain data seperated by spaces
					// e.g. A 2 -2 0 0 -2 0 0 1 -1 -1 -2 -1 -1 -3 1 1 1 -6 -3 0
					// 0 0 0 -8
					// first token is ignored; the rest is parsed and written
					// into the array
					
					// check if we already have all needed lines
					if (linenum == (mapPos-1))
						throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - too many lines");
					int toknum = 0;
					int poslast = 0;
					boolean lastchar=false;
					boolean foundnonspace = false;
					for (int i = 0; i < curLine.length(); i++) {
						// last character ?
						if (i == curLine.length()-1) {
							if (curLine.charAt(i) != ' ') {
								lastchar=true;
								i++;
							}
						}
						
						if (lastchar || ((curLine.charAt(i) == ' ') && foundnonspace)) {
							if (toknum > 0) {
								// check if there are too many tokens
								if (toknum >= mapPos)
									throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - too many tokens");
								// extract integer value and insert into array
								try {
									matrix[toknum][linenum+1] = Integer.parseInt(curLine.substring(poslast, i).trim());
								} catch (NumberFormatException nfe) {
									throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid integer value: " + curLine.substring(poslast, i).trim());
								}

							}
							else {
								// first token: check if correct character is given (matrix must be symmetric)
								if (revMapping[curLine.trim().charAt(0)] != (linenum+1)) 
									throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid character: " + curLine.trim().charAt(0) + ", exptected: " + backMapping[linenum+1]);
								// a white space must follow now!
								if (curLine.trim().charAt(1) != ' ')
									throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid expression: " + curLine.substring(0,2) );
									

							}

							toknum++;
							poslast = i;
							foundnonspace = false;
						} else if (curLine.charAt(i) != ' ')
							foundnonspace = true;
					}
					// decrease toknum by one if last characters where whitespaces
					if (!foundnonspace)
						toknum--;
					if (toknum > 0) {  // any tokens found or empty line?
						if (toknum < (mapPos-1)) // incomplete line
							throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - incomplete line");
						else
							linenum++;
					} 
				}
			}
		}
		// check if we are missing lines
		if (linenum < (mapPos-1)) 
			throw new InvalidSubMatFileException("Error in " + file + " - there are lines missing");
		
		
		return new SubstitutionMatrix(matrix,revMapping,backMapping);
	}
	
	public void guessIfSubstitutionMatrix() {
		bNucleotideMatrix = (getAlphabetSize() < PROTEIN_THRESHOLD);
	}
	
}