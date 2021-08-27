package org.gepard.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gepard.client.ClientGlobals;

// read (our count the characters of) FASTA files

public class FASTAReader {
	
	private static final float NUCLEOTIDE_THRESHOLD = 0.95f;
	private static final int CHARACTERS_FOR_AUTOMATRIX = 250;

	public static Sequence readFile(String file, SubstitutionMatrix substmat)
			throws FileNotFoundException, IOException, InvalidFASTAFileException {
		String name = null;
		byte[] sequence = null;
		
		boolean invalidchars=false;
		
		int ACGTcount = 0;
		boolean[] isACGT = getACGTChecker();
		int charcount=0;
		
		// get lowercase->uppercase mapper
		byte[] LCToUC = getUppercaseMapper();
		
		// get file length
		File f = new File(file);
		int filelen = (int) f.length();
		if (filelen<0) 
			throw new InvalidFASTAFileException("Files larger than 2.1GB are not supported");
		if (filelen==0) 
			throw new InvalidFASTAFileException("Empty file");

		// get input stream
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		// read full contents into byte array
		byte[] contents = new byte[filelen];
		in.readFully(contents, 0, filelen);
		in.close();
		// check for status line
		if (contents.length == 0 || contents[0] != '>')
			// invalid format
			throw new InvalidFASTAFileException("No status line found");

		// search first line break, store name
		int i;
		for (i = 0; contents[i] != 10 && contents[i] != 13; i++)	;
		// one more?
		if (contents[i+1] == 10) i++;
		name = new String(contents, 1, i - 1).trim() ;
		// now parse rest of file

		// first run: count valid characters, overall characters and ACGT amount
		int validcount = 0;
		for (int j = i; j < contents.length; j++) {
			
			// new sequence?
			while (j<contents.length && contents[j] == '>') {
				// ignore status line
				while (j<contents.length && contents[j] != 10 && contents[j] != 13) j++;
				j++;
				// another LF here?
				if (j<contents.length && contents[j] == 10) j++;
				// 
			}
			
			if (j>=contents.length)
				break;
			
					
			// valid character?
			if (contents[j]<0)
				throw new InvalidFASTAFileException("Invalid character in FASTA file: " + (char)contents[j]);
	 
			// convert character to uppercase
			contents[j] = LCToUC[contents[j]]; 
			// valid character for current marix?
			if (substmat.map(contents[j]) > 0)
				validcount++;
			// any character except 13 and 10?
			if (substmat.map(contents[j]) >= 0)
				charcount++;
			// A, C, G or T?
			if (isACGT[contents[j]]) 
				ACGTcount++;

		}
		
		boolean isnucleotide = ((float)ACGTcount/(float)charcount) >= NUCLEOTIDE_THRESHOLD;
		
		// prepare multi-fasta
		List<String> allNames = new ArrayList<String>();
		List<Integer> allStarts = new ArrayList<Integer>();
		// add first one
		allNames.add(name);
		allStarts.add(0);
		
		// second run: create and fill array
		sequence = new byte[validcount];
		int k = 0;
		for (int j = i; j < contents.length; j++) {
			
			// new sequence?
			while (j<contents.length && contents[j] == '>') {
				int statusStart=j;
				// ignore status line
				while (j<contents.length && contents[j] != 10 && contents[j] != 13) j++;
				j++;
				allNames.add(new String(contents,statusStart+1,j-statusStart-2));
				// another LF here?
				if (j<contents.length && contents[j] == 10) j++;
				allStarts.add(k);
			}
			if (j>=contents.length)
				break;

			// add to sequence or output error
			byte mapval = substmat.map(contents[j]);
			// map U to T
			if (isnucleotide && mapval == 16)
				mapval = 2;
			// map unknown chars to N
			if (isnucleotide && mapval > 5)
				mapval = 5;

			if (mapval > 0)
				sequence[k++] = mapval;	
			else if (mapval == 0) 
				invalidchars = true;
		}
		
		// create and return LocalSequence object
		Sequence seq = null;
		if (allNames.size()>1) {
			name = "Multi FASTA (" + ClientGlobals.extractFilename(file) + ")";
			seq = new Sequence(sequence, name, isnucleotide, invalidchars);
			seq.setMulti(allNames, allStarts);
		} else
			seq = new Sequence(sequence, name, isnucleotide, invalidchars);
		
		return seq; 
	}
	
	private static byte[] getUppercaseMapper() {
		byte[] ret = new byte[256];
		Arrays.fill(ret,(byte)10);	// convert all unsupported character to LF=10
									// because LF is ignored anyway
		// write upper case letter A-Z
		for (byte i=65; i<=90; i++)
			ret[i] = i;
		// write lower case letters a-z and convert to uppercase 
		for (byte i=97; i<=122; i++)
			ret[i] = (byte)(i-32);
		
		return ret;
		
	}
	
	private static boolean[] getACGTChecker() {
		boolean[] ret = new boolean[256];
		ret['A'] = true;
		ret['C'] = true;
		ret['G'] = true;
		ret['T'] = true;
		ret['N'] = true;
		ret['X'] = true;
		return ret;
	}
	
	public static boolean isNucleotideFile(String file) throws IOException, InvalidFASTAFileException {
		
		// get lowercase->uppercase mapper && ACGT checker
		byte[] LCToUC = getUppercaseMapper();
		boolean[] isACGT = getACGTChecker();
		
		// get input stream
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		// read first CHARACTERS_FOR_AUTOMATRIX bytes
		int readbytes = (int)Math.min(CHARACTERS_FOR_AUTOMATRIX, new File(file).length());
		byte[] contents = new byte[readbytes];
		in.readFully(contents, 0, readbytes);
		in.close();
		
		// check for status line
		if (contents.length == 0 || contents[0] != '>')
			// invalid format
			throw new InvalidFASTAFileException("No status line found");
		
		int start=0;
		
		// skip status line      
		while (start < contents.length && contents[start] != 10 && contents[start] != 13) start++;
		// did we find no line break?
		if (start == contents.length) throw new InvalidFASTAFileException("End of FASTA status line could not be identified");
		// one more?
		if (contents[start+1] == 10) start++;
		// and the last one to reach the new line
		start++;
		
		
		
		int totalCount=0, acgtCount=0;
		
		// count ACGT and total characters
		for (int i=start; i<contents.length; i++) {
		
			while (i<contents.length && contents[i] == '>') {
				// ignore status line
				while (i<contents.length && contents[i] != 10 && contents[i] != 13) i++;
				i++;
				// another LF here?
				if (i<contents.length && contents[i] == 10) i++;
			}
			if (i>=contents.length)
				break;
			
		
			if (contents[i] != 13 && contents[i] != 10) {
				if (contents[i]<0)
					throw new InvalidFASTAFileException("Invalid character in FASTA file: " + (char)contents[i]);
				if (isACGT[LCToUC[contents[i]]])
					acgtCount++;
				totalCount++;
			}
		}
		
		// check ACGT/total ratio
		if  ( ((double)acgtCount / (double)totalCount ) >= NUCLEOTIDE_THRESHOLD )
			return true;
		else
			return false;
		
	}
	
	public static int countNuclSeqLength(String file) throws IOException {
		
		int charcount=0;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		// ignore first line -> status
		//reader.readLine();
		
		String line;
		while ((line = reader.readLine()) != null) {
			// ignore status lines
			if (!line.startsWith(">"))
				charcount += line.length();
		}
		
		reader.close();
		
		return charcount;
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InvalidFASTAFileException, InvalidSubMatFileException {
				
		Sequence s = readFile("/home/jan/work/workspace/Gepard/stuff/bad.fa", ClientGlobals.getEDNA());
		System.out.println(s.isMulti());
//		
//		s = readFile("/home/jan/work/workspace/Gepard/stuff/ecoli_part_1.fa", ClientGlobals.getEDNA());
//		System.out.println(s.isMulti());
		
		System.out.println(s.hasInvalidChars());
		System.out.println(s.getLength());
		System.out.println(s.likelyNucleotides());
//		System.out.println(isNucleotideFile("/home/jan/work/workspace/Gepard/stuff/bad.fa"));
	}
	
}
