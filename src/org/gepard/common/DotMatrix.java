package org.gepard.common;

import java.io.Serializable;

// represents a dotmatrix and contains
// the methods for dotmatrix calculation

public class DotMatrix implements Serializable {

	private static final long serialVersionUID = 5086505082934770263L;

	// members
	private float[][] dotmatrix = null;

	private int width = 0, height = 0;

	private float maxdotscore, mindotscore;
	private float avgdotscore;
	
	private float GCratio1=0, GCratio2;

	private int seq1Length, seq2Length;

	private ParameterSet params;

	private String seq1Name, seq2Name;

	private SuffixArray sa;

	private SubstitutionMatrix submat;
	
	private boolean bFuncats=false;
	
	private boolean nuclmatrix;

	// simple accessor methods
	public float[][] getDotMatrix() {
		return dotmatrix;
	}

	public boolean isNucleotideMatrix() {
		return nuclmatrix;
	}


	public boolean usingFuncats() {
		return bFuncats;
	}
	
	public float getGCratio1() {
		return GCratio1;
	}
	
	public float getGCratio2() {
		return GCratio2;
	}
	
	public SuffixArray getSuffixArray() {
		return sa;
	}

	public String getSeq1Name() {
		return seq1Name;
	}

	public String getSeq2Name() {
		return seq2Name;
	}

	// public float[][]getNoiseMatrix(){return noisematrix;}

	public ParameterSet getParameterSet() {
		return params;
	}

	// public int getRatio(){return ratio;}
	public int getSeq1Length() {
		return seq1Length;
	}

	public int getSeq2Length() {
		return seq2Length;
	}

	// public int getWordLength(){return wordlen;}
	// public int getWindowSize(){return windowsize;}
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getMaxDotScore() {
		return maxdotscore;
	}

	public float getMinDotScore() {
		return mindotscore;
	}
	
	public float getAvgDotScore() {
		return avgdotscore;
	}

	// additional parameters method
	public DotMatrix(byte[] s1, byte[] s2, String seq1n, String seq2n,
			SuffixArray sa, ParameterSet params, SubstitutionMatrix submat, 
			DotMatrixCallback callback, AbortionChecker ac, boolean SAforSecondSeq ) {
		
		
		// store sequences suffix array, word length, ratio & char distribution
		// flag
		// seq1 = s1;
		// seq2 = s2;
		seq1Length = s1.length;
		seq2Length = s2.length;
		seq1Name = seq1n;
		seq2Name = seq2n;
		this.sa = sa;
		this.submat = submat;
		// wordlen = params.wordLength;
		// ratio = params.ratio;
		// windowsize = params.windowSize;
		this.params = params;
		//int x = params.windowSize;
		// if coordinates of one sequence are both 0 -> full sequence
		if ((params.seq1Start == 0) && (params.seq1Stop == 0))
			params.seq1Stop = s1.length - 1;
		if ((params.seq2Start == 0) && (params.seq2Stop == 0))
			params.seq2Stop = s2.length - 1;
		// calculate dimensions
		width = ((params.seq1Stop - params.seq1Start + 1) / params.zoom);
		height = ((params.seq2Stop - params.seq2Start + 1) / params.zoom);

		nuclmatrix = submat.isNucleotideMatrix();
		// compute GC ratio
		if (nuclmatrix)
			calcGCRatios(s1,s2,params);

		// calculate dot matrix
		if (params.wordLength != 0)
			calcDotMatrix(sa, s1, s2, callback, ac, SAforSecondSeq );
		else 
			calcDotMatrixWindowMode(sa, s1, s2, callback, ac);
		
	}
	
	// matrix calculation
	private void calcDotMatrix(SuffixArray sa, byte[] seq1, byte[] seq2, DotMatrixCallback callback, AbortionChecker ac, boolean SAforSecondSeq ) {

		// determine callback step
		int callbackStep = (callback != null) ? callback.tellCallbackStep(params.wordLength, params.windowSize ) : 2147483647;
		// create dot matrix array
		dotmatrix = new float[width][height];
		
		// set correct sequence order
		byte[] nonSAsequence;
		int from, to;
		int SAseqStart, SAseqStop;
		int nonSAseqStart, nonSAseqStop;
		if (!SAforSecondSeq) {	// SA for first sequence
			nonSAsequence = seq2;
			from = params.seq2Start;
			to = params.seq2Stop - params.wordLength + 1;
			SAseqStart = params.seq1Start;
			SAseqStop  = params.seq1Stop;
			nonSAseqStart = params.seq2Start;
			nonSAseqStop  = params.seq2Stop;
		} else {				// SA for second sequence
			nonSAsequence = seq1;
			from = params.seq1Start;
			to = params.seq1Stop - params.wordLength + 1;
			SAseqStart = params.seq2Start;
			SAseqStop  = params.seq2Stop;
			nonSAseqStart = params.seq1Start;
			nonSAseqStop  = params.seq1Stop;
		}
		
		int[] hits = null;
		
		// set pseudodelimiter (for pseudocontigs)   ... don't dare to remove this anymore, old relic from remote functionality
		byte pseudodelimiter;
		boolean nuclmatrix = submat.isNucleotideMatrix();
		if (nuclmatrix)
			pseudodelimiter = submat.map( (byte)'Z');
		else
			pseudodelimiter=-99;
		
		// create identity and complementary maps
		byte[] idmap = new byte[128];
		for (byte i=0; i<128 && i >= 0; i++) idmap[i] = i;
		// use complementary map if no protein sequences are used
		byte[] complmap ;
		if (submat.isNucleotideMatrix())
			complmap = Sequence.getComplementaryMap();
		else 
			complmap = idmap;
		
		// iterate through non-SA sequence
		for (int i = from; i < to; i++) {
			// only do any searching if first letter != the pseudodelimiter Z
			// also ignore N's and X's in nucleotide matrices
			if ( (nonSAsequence[i] != pseudodelimiter )&&
					!(  nuclmatrix && (     nonSAsequence[i] ==  5         ) ) ) {

			//	if (submat.reverseMap(nonSAsequence[i]) == 'N') System.exit(1);

				// search current word of the non-SA sequence in the suffix array (in both directions)
				int[] forwardhits = sa.search(nonSAsequence, i, params.wordLength, false, idmap);

				int[] reversehits;
				if (i >= params.wordLength-1) {
					reversehits = sa.search(nonSAsequence, i, params.wordLength, true, complmap);
				} else
					reversehits = new int[0];
				
				// iterate through both hit arrays
				hits=forwardhits;
				boolean firsthits=true;
				int j=0;
				
				
				//for (int j = 0; j < hits.length; j++) {
				while (true) {
					// check indices
					
					if (j == hits.length)
						 if (firsthits) {
							 if (reversehits.length > 0) {
								 firsthits = false;
								 hits = reversehits;
								 j=0;
							 } else
								 break;
						 } else
							 break;
					
					// check if hit is in bounds
					if ((hits[j] >= SAseqStart)	&& (hits[j] <= SAseqStop)) {
						// transform coordinates
						float tx = (float) (hits[j] - SAseqStart) / params.zoom;
						float ty = (float) (i - nonSAseqStart) / params.zoom;
						
						float value = 0;
					//	// use windowsize?
					//	if (params.windowSize == 0) // no -> score 1
							value = 1;
					//	else
					//		value = getWindowScore(seq1, seq2, hits[j], i, params.windowSize);

						int x1=0,x2=0,y1=0,y2=0;
					 
						// set proper coordinates (switch if SA for second sequence)
						if (!SAforSecondSeq) {
							// determine the 4 involved dot matrix spots
							x1 = (int) tx;
							x2 = ((int) tx) + 1;
							y1 = (int) ty;
							y2 = ((int) ty) + 1;		
					
						} else {
							// determine the 4 involved dot matrix spots
							x1 = (int) ty;
							x2 = ((int) ty) + 1;
							y1 = (int) tx;
							y2 = ((int) tx) + 1;		
						}
						// calc fractions
						float fracx2 = tx - (float) ((int) tx);
						float fracx1 = 1 - fracx2;
						float fracy2 = ty - (float) ((int) ty);
						float fracy1 = 1 - fracy2;
						
						// distribute values to array
						if ((x1 < width) && (y1 < height)) 
							dotmatrix[x1][y1] += fracx1 * fracy1 * value;
						if ((x2 < width) && (y1 < height))
							dotmatrix[x2][y1] += fracx2 * fracy1 * value;
						if ((x1 < width) && (y2 < height)) 
							dotmatrix[x1][y2] += fracx1 * fracy2 * value;
						if ((x2 < width) && (y2 < height)) 
							dotmatrix[x2][y2] += fracx2 * fracy2 * value;
						
						
					}
					
					// increase index and shift hit array if necessary
					j++;
				
				}
				
				// check for callback
				if (i % callbackStep == 0) {
					callback.dotmatrixCalcStatus(
						// from:  params.seq2Start
						// to  : params.seq2Stop - params.wordLength + 1
						(float)(i-nonSAseqStart) / (float)(nonSAseqStop - params.wordLength + 1-nonSAseqStart) * 100
					);
					// abort?
					if (ac.dotplotAborted()) {
						callback.tellAborted();
						return;
					}
				}
			
			}
			
		}			
		// now determine highest and lowest value
		calcMaxMinAvg(false);
		
	}
	/*
	// matrix calculation (wordlength == 0)
	private void calcDotMatrixWordlenZeronew(SuffixArray sa, byte[] seq1,
			byte[] seq2, DotMatrixCallback callback, AbortionChecker ac) {
		
		// determine callback step
//		int callbackStep = (callback != null) ? callback.tellCallbackStep(params.wordLength, params.windowSize ) : 2147483647;
	
		// create dot matrix array
		dotmatrix = new float[width][height];
		
		
		// create identity and complementary maps
		byte[] idmap = new byte[128];
		for (byte i=0; i<128 && i >= 0; i++) idmap[i] = i;
		// use complementary map if no protein sequences are used
		byte[] complmap ;
		if (submat.isNucleotideMatrix())
			complmap = Sequence.getComplementaryMap();
		else 
			complmap = idmap;
		
		int winsize = params.windowSize;
		
		long progress=0;
		int width = params.seq1Stop-params.seq1Start+1;
		int height = params.seq2Stop-params.seq2Start+1;
		long progresstotal = ((width*height) - (width+height-1)) * 2;
		
		
		// *** RUN 1, forward matches
		
		// iterate over left and upper border
		int i=params.seq1Start,j=params.seq2Stop;
		
		while (i<=params.seq1Stop) {
			
			int x = i;
			int y = j;
			
			// calculate initial window bounds
			int backward = (winsize/2);
			if (x-backward < 0)
				backward -= (backward-x);
			if (y-backward < 0)
				backward -= (backward-y);
			int forward = (winsize/2);
			
			float score=0;
			// calculate initial window
			for (int k=-backward; (k<=forward) && (x + k<seq1.length) && (y + k<seq2.length) ; k++) {
				// calculate score
				score += submat.getScore(seq1[x + k], seq2[y + k]);
			}
			
			// write score to dotmatrix
			distributeScore(x, y, score);
			
			// now extend the diagonal as long as possible
			x+=1; 
			y+=1;
			while ((x<seq1.length) && (y<seq2.length)) {
				// check if the upper left end of the current diagonal was in range
				if ((x-backward-1 >= 0) && (y-backward-1 >= 0))  
					// subtract upper left end
					score -= submat.getScore(seq1[x-backward-1], seq2[y-backward-1]);
				// check if the lower right end of the current diagonal is in range
				if ((x+forward < seq1.length) && (y+forward<seq2.length)) 
					// add new lower right end
					score += submat.getScore(seq1[x], seq2[y]);
				// write score to dotmatrix
				distributeScore(x, y, score);
				// extend further
				x++; y++;
				
				progress++;
			}
			
			// check for callback
			callback.dotmatrixCalcStatus(
					(float)progress/(float)progresstotal*100f
			);
			// abort?
			if (ac.dotplotAborted()) {
				callback.tellAborted();
				return;
			}
			
			// increase variables
			if (j > params.seq2Start) j--;
			else i++;
		}
		
		// *** RUN 2, reverse matches
		
		// iterate over upper and right border
		i=params.seq1Stop;
		j=params.seq2Stop;
		
		while (i>=params.seq1Start) {
			
			int x = i;
			int y = j;
			
			
			// calculate initial window bounds
			int backward = (winsize/2);
			if (x+backward > seq1.length)
				backward = seq1.length - x -1;
			if (y-backward < 0)
				backward -= (backward-y);
			int forward = (winsize/2);
			
			
			float score=0;
			// calculate initial window
			for (int k=-backward; (k<=forward) && (x - k>=0) && (y + k<seq2.length) ; k++) {
				// calculate score
				score += submat.getScore(seq1[x - k], complmap[seq2[y + k]]);
			}
			
			// write score to dotmatrix
			distributeScore(x, y, score);
			
			// now extend the diagonal as long as possible
			x-=1; 
			y+=1;
			while ((x>=0) && (y<seq2.length)) {
				// check if the upper left end of the current diagonal was in range
				if ((x+backward+1 < seq1.length) && (y-backward-1 >= 0))  
					// subtract upper left end
					score -= submat.getScore(seq1[x+backward+1], complmap[seq2[y-backward-1]]);
				// check if the lower right end of the current diagonal is in range
				if ((x-forward >= 0) && (y+forward<seq2.length)) 
					// add new lower right end
					score += submat.getScore(seq1[x], complmap[seq2[y]]);
				// write score to dotmatrix
				distributeScore(x, y, score);
				// extend further
				x--; y++;
				
				progress++;
			}
			
			// check for callback
			callback.dotmatrixCalcStatus(
					(float)progress/(float)progresstotal*100f
			);
			// abort?
			if (ac.dotplotAborted()) {
				callback.tellAborted();
				return;
			}
		
			// increase variables
			if (j > params.seq2Start) j--;
			else i--;
			
		}
		

		// now determine highest and lowest value
		calcMaxAndMin();
		
	}
	*/
	/*
	private void distributeScore(int x, int y, float score) {
		// transform coordinates
		float tx = (float) (x - params.seq1Start) / params.zoom;
		float ty = (float) (y - params.seq2Start) / params.zoom;
		// determine the 4 involved dot matrix spots
		int x1 = (int) tx;
		int x2 = ((int) tx) + 1;
		int y1 = (int) ty;
		int y2 = ((int) ty) + 1;
		// calc fractions
		float fracx2 = tx - (float) ((int) tx);
		float fracx1 = 1 - fracx2;
		float fracy2 = ty - (float) ((int) ty);
		float fracy1 = 1 - fracy2;
		// distribute value to array
		if ((x1 < width) && (y1 < height))
			dotmatrix[x1][y1] += fracx1 * fracy1 * score;
		if ((x2 < width) && (y1 < height))
			dotmatrix[x2][y1] += fracx2 * fracy1 * score;
		if ((x1 < width) && (y2 < height))
			dotmatrix[x1][y2] += fracx1 * fracy2 * score;
		if ((x2 < width) && (y2 < height))
			dotmatrix[x2][y2] += fracx2 * fracy2 * score;
	}
*/

	private void calcMaxMinAvg(boolean windowmode) {
		
		// <CAP> calc cap
		float cap;
		if (windowmode)
			cap = Float.POSITIVE_INFINITY;
		else
			cap = params.zoom  / 4f;
		
		
		// find minimum and maximum dot score
		maxdotscore = Float.NEGATIVE_INFINITY;
		mindotscore = Float.POSITIVE_INFINITY;
		// iterate over whole matrix
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (dotmatrix[i][j] > cap)
					dotmatrix[i][j] = cap;
				if (dotmatrix[i][j] > maxdotscore)
					maxdotscore = dotmatrix[i][j];
				if (dotmatrix[i][j] < mindotscore)
					mindotscore = dotmatrix[i][j];
				avgdotscore += dotmatrix[i][j];
			}
		}
		avgdotscore /= (width * height);
	}

	private void calcGCRatios(byte[] s1, byte[] s2, ParameterSet params) {
		
		// iterate through first sequence
		int GCcount=0;
		for (int i=params.seq1Start ; i<=params.seq1Stop ; i++) {
			if (s1[i] == 3 || s1[i] == 4)
				GCcount++;
		}
		
		// store value
		GCratio1 = (float)GCcount / (float)(params.seq1Stop - params.seq1Start + 1);
		
		// iterate through second sequence
		GCcount=0;
		for (int i=params.seq2Start; i<=params.seq2Stop; i++) {
			if (s2[i] == 3 || s2[i] == 4)
				GCcount++;
		}
		
		// store value
		GCratio2 = (float)GCcount / (float)(params.seq2Stop - params.seq2Start + 1);
	}
	
	
	// constructor for existing dotplot data
	// previously used to re-construct remote dotplot, not used anymore
	public DotMatrix(float[][] matrix, float maxDotScore, float minDotScore,
			String seq1Name, int seq1Length, float seq1GCRatio,
			String seq2Name, int seq2Length, float seq2GCRatio,
			ParameterSet params) {
		
		// just copy values
		this.dotmatrix = matrix;
		this.maxdotscore = maxDotScore;
		this.mindotscore = minDotScore;
		this.seq1Name = seq1Name;
		this.seq1Length = seq1Length;
		this.GCratio1 = seq1GCRatio;
		this.seq2Name = seq2Name;
		this.seq2Length = seq2Length;
		this.GCratio2 = seq2GCRatio;
		this.params = params;
		this.nuclmatrix = true;
		
		// set additional values
		width = matrix.length;
		height = matrix[0].length;
	}
	

	public CompressedDotMatrix getCompressedMatrix() {
		byte[] intmatrix = new byte[width*height];
		float range = maxdotscore - mindotscore;
		int newmaxscore=Integer.MIN_VALUE, newminscore=Integer.MAX_VALUE;
		// iterate row-by-row
		for (int j=0; j<height; j++) {
			for (int i=0; i<width; i++) {
				int newval = Math.round ((dotmatrix[i][j] - mindotscore) / range * 255f );
				if (newval < newminscore) newminscore = newval;
				if (newval > newmaxscore) newmaxscore = newval;
				intmatrix[j*width+i] = (byte)newval;
			}
		}
		
		return new CompressedDotMatrix(intmatrix, newmaxscore, newminscore);
	}
	
	
	////////////////////////////////////////////////////////////////////
	
	// matrix calculation (wordlength == 0)
	private void calcDotMatrixWindowMode(SuffixArray sa, byte[] seq1,
			byte[] seq2, DotMatrixCallback callback, AbortionChecker ac) {
		
		// determine callback step
		int callbackStep = (callback != null) ? callback.tellCallbackStep(params.wordLength, params.windowSize ) : 2147483647;
	
		// create dot matrix array
		dotmatrix = new float[width][height];
		
		// create identity and complementary maps
		byte[] idmap = new byte[128];
		for (byte i=0; i<128 && i >= 0; i++) idmap[i] = i;
		// use complementary map if no protein sequences are used
		byte[] complmap ;
		if (submat.isNucleotideMatrix())
			complmap = Sequence.getComplementaryMap();
		else 
			complmap = idmap;
		// iterate through sequences
		//for (int i = params.seq2Start; i < params.seq2Stop - params.wordLength	+ 1; i++) {
		//	for (int j = params.seq1Start; j < params.seq1Stop- params.wordLength + 1; j++) {
		for (int i = params.seq2Start; i <= params.seq2Stop; i++) {
			for (int j = params.seq1Start; j <= params.seq1Stop; j++) {
				// transform coordinates
				float tx = (float) (j - params.seq1Start) / params.zoom;
				float ty = (float) (i - params.seq2Start) / params.zoom;

				float value = 0;
				// get window score
				value = getWindowScore(seq1, seq2, j, i, params.windowSize)
						+ getWindowScoreBackward(seq1, seq2, j, i, params.windowSize, complmap);
				
				// determine the 4 involved dot matrix spots
				int x1 = (int) tx;
				int x2 = ((int) tx) + 1;
				int y1 = (int) ty;
				int y2 = ((int) ty) + 1;

				// calc fractions
				float fracx2 = tx - (float) ((int) tx);
				float fracx1 = 1 - fracx2;
				float fracy2 = ty - (float) ((int) ty);
				float fracy1 = 1 - fracy2;

				// distribute value to array
				if ((x1 < width) && (y1 < height))
					dotmatrix[x1][y1] += fracx1 * fracy1 * value;
				if ((x2 < width) && (y1 < height))
					dotmatrix[x2][y1] += fracx2 * fracy1 * value;
				if ((x1 < width) && (y2 < height))
					dotmatrix[x1][y2] += fracx1 * fracy2 * value;
				if ((x2 < width) && (y2 < height))
					dotmatrix[x2][y2] += fracx2 * fracy2 * value;

			}
			
			// check for callback
			if (i % callbackStep == 0) {
				callback.dotmatrixCalcStatus(
					// from:  params.seq2Start
					// to  : params.seq2Stop - params.wordLength + 1
					(float)(i-params.seq2Start) / (float)(params.seq2Stop - params.wordLength + 1-params.seq2Start) * 100
				);
				// abort?
				if (ac.dotplotAborted()) {
					callback.tellAborted();
					return;
				}
			}			
		}

		// now determine highest and lowest value
		calcMaxMinAvg(true);
		
	}
	
	
	
//	 calculate the score of a given sequence window
	// (used for wordlength==0 calculations)
	private float getWindowScore(byte[] seq1, byte[] seq2, int x, int y,int windowsize) {
		
		float score = 0;
		int consecutives=0;
		
		int backforward = windowsize/2;
		
		if (nuclmatrix) {
			for (int i = -backforward; (i <= backforward) ; i++) {
				if ((x+i>0) && (y+i>0) && (x + i < seq1.length) && (y + i < seq2.length)) {
					if (seq1[x + i] == seq2[y + i]) {
						score += 1;
						score += consecutives;
						if (consecutives < 7) consecutives++;
					}
					else {
						//score--;
						consecutives=0;
					}
				}
			}
		} else {
			// protein dotplot
			for (int i = -backforward; (i <= backforward) ; i++) {
				if ((x+i>0) && (y+i>0) && (x + i < seq1.length) && (y + i < seq2.length)) {
					// add score determined from substitution matrix
					score += submat.getScore(seq1[x + i], seq2[y + i]);
					
//					System.out.println(submat.getScore(seq1[x + i], seq2[y + i]));
				}
				
			}
		}
		
		
		return score;
	}
	
	
	// calculate the backward score of a given  sequence window
	// (used for wordlength==0 calculations)
	private float getWindowScoreBackward(byte[] seq1, byte[] seq2, int x, int y,
			int windowsize, byte[] map) {
		
		float score = 0;
		int consecutives=0;
		int backforward = windowsize/2;
		
		if (nuclmatrix) {
			for (int i = -backforward; (i <= backforward) ; i++) {
				if ((x-i>0) && (y+i>0) && (x - i < seq1.length) && (y + i < seq2.length)) {
					if (seq1[x - i] == map[seq2[y + i]]) {
						score += 1;
						score += consecutives;
						if (consecutives < 7) consecutives++;
					}
					else {
						//score--;
						consecutives=0;
					}
				}
			}
		}
		
		return score;

	/*	for (int i = 0; (i < windowsize) && (x - i >= 0)
				&& (y + i < seq2.length); i++) {
			// add score determined from substitution matrix
			score += submat.getScore(seq1[x - i], map[seq2[y + i]]);
		}
		return score;*/
	}

	
}