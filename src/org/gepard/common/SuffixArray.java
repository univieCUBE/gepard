package org.gepard.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

// a suffix array and its calculation methods

public class SuffixArray {
	
	private int[] suftab;
	private byte[] orgdata;
	private short[] lcp;
	
	private AbortionChecker ac;
	
	boolean isgepardsa=true;
	
	public SuffixArray(Sequence seq, int K, AbortionChecker ac) {
		
		this.ac = ac;
		
		// copy byte array to int array and add padding zeros
		byte[] s = seq.getSequenceData();
		int si[] = new int[s.length+3];
		
		for (int i=0; i<s.length; i++)
			si[i] = s[i];
		
		si[s.length]=si[s.length+1]=si[s.length+2]=0;
//		si[s.length]=si[s.length+1]=si[s.length+2]=K;
			
		// allocate space for suffix array
		suftab = new int[s.length];
		// generate suffix array
		genArray(si,suftab,s.length,K+1);
		
		// check for abortion
		if ((ac != null) && (ac.dotplotAborted())) {
			return;
		}
		
		// generate lcp table
		lcp = genLCP(suftab,s);
		// store original data
		orgdata = s;	
		
		
		
	}
	
	
	private SuffixArray(int[] st, short[] l,  byte[] od, boolean isgepardsa) {
		suftab = st;
		orgdata = od;
		lcp = l;
		this.isgepardsa = isgepardsa;
	}

	// search in 'query' from 'qstart' for 'qlen' bytes in the direction 'backwards?'
	// it uses the 'map' array for identity or complementary character mapping
	public int[] search(byte[] query, int qstart, int qlen, boolean backwards, byte[] map) {
	
		int[] ret = new int[0];
		int r,l,m;
		
		l=0; r=orgdata.length-1;
		m = (l+r)/2;
		
		while (l<=r) {
			m = (l+r)/2;	
			
			// compare current position
			int comp;
//			if (suftab[m] <= orgdata.length-qlen) {
				comp = compare(orgdata,suftab[m],query,qstart,qlen, backwards, map);
//			}
//			else
//				comp = -1; // probably wrong!!!
				
			if (comp == 1)       // orgdata > query at current pos -> search to the left 
				r = m-1;
			else if (comp == -1) // orgdata < query at current pos -> search to the right
				l=m+1;
			else {
				
				
				
				// find left boundary
				l=m;
				while ( (l>0) && (lcp[l-1] >= qlen)) l--;
				// find right boundary
				r=m;
				while ( r<(orgdata.length-1) && (lcp[r] >= qlen)) r++;

				// number of hits:  r-l+1
				// prepare result array
				ret = new int[r-l+1];
				
				// fill result array
				for (int i=0; i<r-l+1; i++) {
					ret[i] = suftab[l+i];
				}
				
				// set r<l to exit the loop
				r=0; l=1;
			} 
		}
			

		return ret;
		
	}
	
	// results: -1 -> s1<s2      0 ->  s1==s2    1 -> s1>s2
	private int compare(byte[] s1, int off1, byte[] s2, int off2, int p, boolean backwards, byte[] map) {
		
		// characters of the second sequence are mapped (to complementary when searching backwards!)
		
		if (!backwards) {
			int i;
			for (i=0; i<p && (i+off1<s1.length) && (i+off2<s2.length) ;i++) {
				if (s1[i+off1] < map[s2[i+off2]])
					return -1;
				else if (s1[i+off1] > map[s2[i+off2]])
					return 1;
			}
			if (i<p) {
				if (isgepardsa) {
					if (i+off1 == s1.length)
						return -1;
					else 
						return 1;
				} else {
					if (i+off1 == s1.length)
						return 1;
					else 
						return -1;
				}
			}
			else
				return 0;
		} else {
			int i;
			for (i=0; i<p && (i+off1<s1.length) && (i+off2<s2.length);i++) {
				if (s1[i+off1] < map[s2[-i+off2]])
					return -1;
				else if (s1[i+off1] > map[s2[-i+off2]])
					return 1;
			}
			if (i<p) {
				if (isgepardsa) {
					if (i+off1 == s1.length)
						return -1;
					else 
						return 1;
				} else {
					if (i+off1 == s1.length)
						return 1;
					else 
						return -1;
				}
			}
			else 
				return 0;
		
		}
		
	/*	if (!backwards) {
			for (int i=0; i<p;i++) {
				if (s1[i+off1] < s2[i+off2])
					return -1;
				else if (s1[i+off1] > s2[i+off2])
					return 1;
			}
			return 0;
		} else {
			for (int i=0; i<p;i++) {
				if (s1[i+off1] < s2[-i+off2])
					return -1;
				else if (s1[i+off1] > s2[-i+off2])
					return 1;
			}
			return 0;
		
		}*/
	}
	

	
	private void radixPass(int[] a, int[] b, int[] s, int offset, int n, int K) {
		
		int[] c = new int[K+1];					// create counter array
		for (int i=0; i<n; i++)	 c[s[a[i]+offset]]++;	// count occurences
		for (int i=0, sum=0; i<=K; i++) {		// calculate first indices in target array
			int t = c[i]; c[i] = sum; sum+=t;	//    for each character from 0..K
		}
		
		for (int i=0; i<n; i++)		// assign new positions in target array (sort)
			b[c[s[a[i]+offset]]++] = a[i];
			
	}	
	
	
	public int[] getRawArray() {
		return suftab;
	}
	
	
	// generate suffix array from input array s[] with key 0..K
	private void genArray(int[] s, int[] SA, int n, int K) {
				
		// check for abortion
		if ((ac != null) && (ac.dotplotAborted())) {
			suftab = null;
			orgdata = null;
			lcp = null;
			return;
		}
		
		// calculate amount of suffixes index mod 3 = 0, = 1, =2
		int n0 = (n+2)/3,	n1 = (n+1)/3,	n2=n/3;
		int n02 = n0 + n2;
		
		
		// generate helper arrays
		int s12a[] = new int[n02+3];
		int s12b[] = new int[n02+3];
	
		// generate indices with mod 3 != 0
		for (int i=0, j=0; i<n+(n0-n1); i++) if (i%3 != 0) s12a[j++] = i;
				
		// radix sort mod 3 != 0 suffixes stored in s12a
		// 3 passes for each triple position
		radixPass(s12a, s12b, s ,2,n02, K);
		radixPass(s12b, s12a, s ,1,n02, K);
		radixPass(s12a, s12b, s ,0,n02, K);
		// (potential) suffix array is now stored in s12b
		
		// find lexographic names
		int name=0;
		int c0=-1,c1=-1,c2=-1;
		for (int i=0; i<n02; i++) {
			// check if current triple is different from predecessor
			if ( (s[s12b[i]] != c0) || (s[s12b[i]+1] != c1) || (s[s12b[i]+2] != c2) ) {
				name++;		// new lexographic name
				c0=s[s12b[i]];	c1=s[s12b[i]+1];	c2=s[s12b[i]+2];	// store last values
			}
			// store lex. names in s12a (mod3=1 -> left half, mod3=2 -> right half)
			if (s12b[i] % 3 == 1)	s12a[s12b[i]/3] = name;			// left half
			else					s12a[s12b[i]/3 + n0] = name;	// right half
		}
		
		// names already unique?
		if (name < n02) { // no -> recurse
			genArray(s12a, s12b, n02, name);
			
			// check for abortion
			if ((ac != null) && (ac.dotplotAborted())) {
				suftab = null;
				orgdata = null;
				lcp = null;
				return;
			}
			
			// create and store unique names
			for (int i=0; i<n02; i++) s12a[s12b[i]] = i+1;
		} else { // yes -> generate suffix array from names
			for (int i=0; i<n02; i++) s12b[s12a[i]-1]=i;
		}
	
		int s0a[] = new int[n0];
		int s0b[] = new int[n0];
		// <DONTKNOW>
		for (int i=0,j=0; i<n02; i++)
			if (s12b[i] < n0) s0a[j++] = 3*s12b[i];
		// </DONTKNOW>
		radixPass(s0a, s0b, s,0, n0, K);
		s0a=null;
		
		// merge arrays now
		
		// t = current pos in SA12 (s12b) array
		// p = current pos in SA0 (s0b) array
		// i = pos of current 12 suffix
		// j = pos of current 0 suffix
		// k = current pos in final suffix array cSA
		
		for (int p=0, t=n0-n1, k=0; k<n; k++) {
			// get real pos of current mod3 =1,=2 suffix
			int i =    (s12b[t]<n0) ? s12b[t] * 3 + 1 : (s12b[t]-n0)*3+2;
			int j = s0b[p];
			
			boolean q=false;
			if (s12b[t] < n0) {
				int a1=s[i],a2=s12a[s12b[t] + n0],b1=s[j],b2=s12a[j/3];
				q=		(a1 < b1 || (a1 == b1 && a2 <= b2));		
			} else {
				int a1=s[i],a2=s[i+1],a3=s12a[s12b[t]-n0+1] ,      b1=s[j],b2=s[j+1],b3=s12a[j/3+n0];
				q = (a1 < b1 ||(a1 == b1 && (a2 < b2 || (a2 == b2 && a3 <= b3))));
			}
			if (q) {
				SA[k] = i; t++;
				if (t == n02) { // only SA0 suffixes left
					for (k++; p<n0; p++,k++) SA[k] = s0b[p];
				}
					
			} else {
				SA[k] = j; p++;
				if (p == n0) { // only SA12 suffixes left
					for (k++; t<n02; t++,k++) SA[k] =    (s12b[t]<n0) ? s12b[t] * 3 + 1 : (s12b[t]-n0)*3+2;
				}
			}
		}
	}

	
	public void saveToFile(String filename) throws IOException {
		// create output stream
		LEDataOutputStream safile =	new LEDataOutputStream(
			new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))));
		
		// vector to recall big lcps
		Vector<BigLCP> biglcps = new Vector<BigLCP>();
		
		// write type 0 (gepard suffix array)
		safile.write(0);
		
		// write number of array entries
		safile.writeInt(suftab.length);
		// write suffix array
		for (int i=0; i<suftab.length; i++) 
			safile.writeInt(suftab[i]);
		// write lcp table
		for (int i=0; i<lcp.length; i++) {
			// remember big lcp?
			if (lcp[i] < 255) {
				safile.write(unsignedshort2byte(lcp[i]));
			} else {
				biglcps.add(new BigLCP(i, lcp[i]));
				safile.write(unsignedshort2byte((byte)255));
			}
		}
		
		// now write big lcps to file
		for (BigLCP lcp : biglcps) {
			safile.writeInt(lcp.suffix+1);
			safile.writeInt(lcp.lcp);
		}
			
		
		safile.close();
	}
	
	public static SuffixArray loadFromFile(File file, byte[] orgdata) throws FileNotFoundException, IOException {
		
		LEDataInputStream in = new LEDataInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		
		// read type
		byte type = in.readByte();
		boolean isgepardsa = (type == 0);
		
		// read sequence length
		int seqlength = in.readInt();
		// create arrays
		int[] suftab = new int[seqlength];
		short[] lcp = new short[seqlength-1];
		// read suffix array
		for (int i=0; i<seqlength; i++)
	        suftab[i] = in.readInt();
		// read lcp table
		for (int i=0; i<seqlength-1; i++) 
        	lcp[i] = byte2unsignedshort(in.readByte());
		
		// now load remaining big lcp information
		boolean eof=false;
		while (!eof) {
			try {
				int cursuf = in.readInt();
				int curlcp = in.readInt();
				
				// restrict to maximum positive short value
				if (curlcp > Short.MAX_VALUE) curlcp = Short.MAX_VALUE;

				lcp[cursuf-1] = (short)curlcp;
				
			} catch (EOFException e) {
				eof = true;
			}
		}
        		
		in.close();
		
		
		return new SuffixArray(suftab,lcp,orgdata, isgepardsa);
	       
		   
	}
	
	private static short byte2unsignedshort(byte i) {
		if (i < 0) {
			return (short)(256 + i);
		} else {
			return i;
		}
	}
	
	private static byte unsignedshort2byte(short i) {
		if (i > 127)
			return (byte)(i-256);
		else
			return (byte)i;
	}
	
	private short[] genLCP(int[] suftab, byte[] orgdata) {
		// shortcut
		int len = suftab.length;
		// create lcp array
		short[] ret = new short[len-1];
		// iterate through suffix table
		for (int i=0; i<len-1; i++) {
			// determine LCP of suffix i and i+1
			int  j=0;
			while (j<32768 && (suftab[i]+j < len) && (suftab[i+1]+j <len)
				 && (orgdata[suftab[i]+j] == orgdata[suftab[i+1]+j]) ) {
				j++;
			//	System.out.println(j);
			}
			// store LCP
			ret[i] = (short)j;
			
			
		}
		
		return ret;
	}

	public static String getSAFilename(String sequenceFile) {
		// extract filename
		String filename = extractFilename(sequenceFile);
		// construct suffix array file name using original file size and extension .sa
		return filename + "_" + new File(sequenceFile).length() + ".sa";
	}
	
	private static String extractFilename(final String source) {
		int sep = source.lastIndexOf(System.getProperty("file.separator"));
		if (sep > -1) {
			return source.substring(sep+1);
		} else
			return source;
	}
	
	public void compareSAs(SuffixArray sa) {
		
		int j=0;
		for (int i=0; i<sa.suftab.length; i++) {
			if (this.suftab[i] != sa.suftab[i]) {
				System.out.println("differing at suffix " + i  + ", this: " + this.suftab[i] + ", other: " + sa.suftab[i]);
				j++;
				/*
				for (int k=0; k<=8 && suftab[i]+k < suftab.length; k++)
					System.out.print(mapit[orgdata[suftab[i]+k]]);
				System.out.println();*/
				System.out.println();
				dump("vmatch  ", orgdata, this.suftab[i]);
				dump("gepard  ", orgdata, sa.suftab[i]);
				
			}
			if (j >= 10)
			break;
		}
		/*
		j=0;
		for (int i=0; i<sa.lcp.length; i++) {
			if (this.lcp[i]!= sa.lcp[i]) {
				System.out.println("differing at lcp " + i  + ", this: " + this.lcp[i] + ", other: " + sa.lcp[i]);
			}
		if (j > 100)
				break;
		}*/
	}
	
	private static void dump(String name ,  byte[] orgdata, int pos) {
		char mapit[] = {' ', 'A', 'T', 'G' , 'C', 'N'};
		
		System.out.print(name);
		for (int k=0; k<50 && pos+k < orgdata.length; k++)
			System.out.print(mapit[orgdata[pos+k]]);
		System.out.println();
	}
	
	public void printYourself() {
		char mapit[] = {' ', 'A', 'T', 'G' , 'C'};
		
		for (int m=0; m<suftab.length; m++) {
			System.out.print(m + ": ");
			for (int i=0; i<=8 && suftab[m]+i < suftab.length; i++)
				System.out.print(mapit[orgdata[suftab[m]+i]]);
			System.out.println();
		}
	}
	
	private static class BigLCP {
		
		public int suffix;
		public int lcp;
		
		public BigLCP(int suffix, int lcp) {
			this.suffix = suffix;
			this.lcp = lcp;
		}
		
	}
	
	public void verifyArray() {
		
		byte[] idmap = new byte[128];
		for (byte i=0; i<128 && i >= 0; i++) idmap[i] = i;
		
		for (int i=0; i<suftab.length-1; i++) {
			
			if (compare(orgdata, suftab[i], orgdata, suftab[i+1], 1000, false, idmap) > 0) {
				System.out.println("WAAAAA, bad one: " + suftab[i+1]);
				dump(i+ "\t", orgdata, suftab[i]);
				dump((i+1)+"\t", orgdata, suftab[i+1]);
//				System.exit(1);
			}
				
		//	System.out.println();
			
		}
	}
	
	public void showFirst(int n) {
		for (int i=0; i<n; i++)
			dump("", orgdata, suftab[i]);
	}
	
	public int getLength() {
		return suftab.length;
	}
	
	public void doubles() {
		byte[] check = new byte[suftab.length];
		
		System.out.println("seq: " + suftab.length);
		
		for (int i=0; i<check.length; i++) {
			
			if (suftab[i] >= check.length)
				System.out.println("Was: " + i);
			
			if (check[suftab[i]] == 1) 
				System.err.println("NOOOOOOOOOOOOOO");
			else
				check[suftab[i]] = 1;
		}
	}
	
}