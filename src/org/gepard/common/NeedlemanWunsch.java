package org.gepard.common;

import java.util.ArrayList;
import java.util.List;

public class NeedlemanWunsch {
	public final int [][] align_matrix;
	public final int [][] matrix_pointer;
	public NeedlemanWunsch(int[][]align_matrix, int[][] matrix_pointer) {
		this.align_matrix = align_matrix;
		this.matrix_pointer = matrix_pointer;
	}
	
	public static Integer GetScoringLocation (String letter, SubstitutionMatrix submat) {
		int result;
		letter = letter.toUpperCase();
		if (!submat.isNucleotideMatrix()) {
			letter = letter.replace("A", "0");
			letter = letter.replace("R", "1");
			letter = letter.replace("N", "2");
			letter = letter.replace("D", "3");
			letter = letter.replace("C", "4");
			letter = letter.replace("Q", "5");
			letter = letter.replace("E", "6");
			letter = letter.replace("G", "7");
			letter = letter.replace("H", "8");
			letter = letter.replace("I", "9");
			letter = letter.replace("L", "10");
			letter = letter.replace("K", "11");
			letter = letter.replace("M", "12");
			letter = letter.replace("F", "13");
			letter = letter.replace("P", "14");
			letter = letter.replace("S", "15");
			letter = letter.replace("T", "16");
			letter = letter.replace("W", "17");
			letter = letter.replace("Y", "18");
			letter = letter.replace("V", "19");
			letter = letter.replace("B", "20");
			letter = letter.replace("Z", "21");
			letter = letter.replace("X", "22");
			letter = letter.replace("*", "23");
			result = Integer.parseInt(letter);
		}else {
		letter = letter.replace("A", "1");
		letter = letter.replace("G", "2");
		letter = letter.replace("C", "3");
		letter = letter.replace("T", "0");
		letter = letter.replaceAll("[^0-9]", "");
		result = Integer.parseInt(letter);
		}
		return result;
	}
	
	public static NeedlemanWunsch GetAlignmentMatrix (String seq1, String seq2, int [][] scoringmatrix, SubstitutionMatrix submat) {
		int align_matrix [][] = new int [seq1.length()+1][seq2.length()+1];
		int matrix_pointer [][] = new int [seq1.length()+1][seq2.length()+1];
		// filling in alignmatrix
		for (int g = 0;g<seq1.length()+1;g++) {
			align_matrix [0][g] = g*(-1);
		}
		for (int g = 0;g<seq2.length()+1;g++) {
			align_matrix [g][0] = g*(-1);
		}
		for (int f = 1;f < seq1.length()+1; f ++) {
			for (int g = 1; g < seq2.length()+1; g ++) {
				int score_m = 0;
				try {
					score_m = scoringmatrix[GetScoringLocation(seq1.substring(f,f+1), submat)][GetScoringLocation(seq2.substring(g,g+1), submat)];
				}
				catch (Exception e) {
				}
				int x = align_matrix[f-1][g-1] + score_m;
				int y = align_matrix[f-1][g] -5;
				int z = align_matrix[f][g-1] -5;
				if (Math.max(Math.max(Math.max(x, y), z),0) != x && Math.max(Math.max(x, y), z) != 0 && align_matrix[f-1][g-1] == 2) {
					//gap opening cost
				}
				align_matrix [f][g] += Math.max(Math.max(Math.max(x, y), z),0);
				if (Math.max(Math.max(Math.max(x, y), z),0) == x) {
					matrix_pointer[f][g] = 2;
				}
				else if (Math.max(Math.max(Math.max(x, y), z),0) == y){
					matrix_pointer[f][g] = 1;
				}
				else if (Math.max(Math.max(Math.max(x, y), z),0) == z){
					matrix_pointer[f][g] = 3;
				}
			}
		}
		return new NeedlemanWunsch(align_matrix, matrix_pointer);}

	public static List<String> GetAlignment (String seq1, String seq2, int [][] scoringmatrix, int [][] align_matrix, int [][] matrix_pointer) {
		//computes local alignment with Smith Waterman Algorythm
		//traceback
		List<Integer> tracebackx = new ArrayList<Integer>();
		List<Integer> tracebacky = new ArrayList<Integer>();
		//creating the position list in align_matrix
		int y = seq2.length();
		int x = seq1.length();
		int max_posx = x, max_posy = y;
		while (true){
			tracebackx.add(x);
			tracebacky.add(y);
			if (matrix_pointer [x][y] == 2) {
				if(x>0)
				x -= 1;
				if(y>0)
				y -= 1;
			}
			else if (matrix_pointer [x][y] == 1){
				if(x>0)
				x -= 1;
				
			}
			else if (matrix_pointer [x][y] == 3) {
				if (y>0)
				y -= 1;
			}
			else {
				x -= 1;
				y -= 1;
			}
			if (x==-1) {
				break;
			}
			if (y==-1) {
				break;
			}
		}
		//creating sequence from position list and sequences
		String aligned_sequence1 = "";
		String aligned_sequence2 = "";
		int min_posx = x;
		int min_posy = y;
		for (int a = tracebackx.size()-1; a >=0; a --) {
			//adding chars to aligned string
			//checking if insertion occurred
			if (a != tracebackx.size()-1 && tracebackx.get(a+1) == tracebackx.get(a) ) {
				aligned_sequence1 += "-";
			}
			else {
				if (tracebackx.get(a) != seq1.length()) {
					aligned_sequence1 += seq1.charAt(tracebackx.get(a));
				}
			}
		}
		for (int a = tracebacky.size()-1;a>=0; a--) {
			if (a != tracebacky.size()-1 && tracebacky.get(a+1) == tracebacky.get(a)) {
				aligned_sequence2 += "-";
			}
			else {
				if (tracebacky.get(a) != seq2.length()) {
					aligned_sequence2 += seq2.charAt(tracebacky.get(a));
				}
			}
		}
		//range is from max (x/y) t max - tracebackx/y.size for positions in sequence
		List<String> ans = new ArrayList<String>();
		ans.add(aligned_sequence1);
		ans.add(aligned_sequence2);
		ans.add(String.valueOf(min_posx+1));
		ans.add(String.valueOf(min_posy+1));
		ans.add(String.valueOf(max_posx+1));
		ans.add(String.valueOf(max_posy+1));
		return ans;
	}
}
