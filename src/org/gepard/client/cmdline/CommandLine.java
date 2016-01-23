package org.gepard.client.cmdline;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.gepard.client.ClientGlobals;
import org.gepard.client.DrawTarget;
import org.gepard.client.Plotter;
import org.gepard.common.AbortionChecker;
import org.gepard.common.DotMatrix;
import org.gepard.common.DotMatrixCallback;
import org.gepard.common.FASTAReader;
import org.gepard.common.InvalidFASTAFileException;
import org.gepard.common.InvalidSubMatFileException;
import org.gepard.common.ParameterSet;
import org.gepard.common.Sequence;
import org.gepard.common.SubstitutionMatrix;
import org.gepard.common.SuffixArray;

public class CommandLine {

	private static final String[] ALLOWED_ARGS = new String[] { "seq1", "seq2", "maxwidth", "maxheight", "matrix", "silent", "word", "window", "from1", "to1",
			"from2", "to2", "lower", "upper", "greyscale", "outfile", "format", "safile", "sasecondseq", "secondcomp", "zoom" };

	private static final int DEF_WIDTH = 750;
	private static final int DEF_HEIGHT = 750;

	private static final int DEF_WORD = 10;
	private static final int DEF_WINDOW = 0;

	private static final String DEF_OUTFORMAT = "PNG";
	private static final String[] VALID_OUTFORMATS = new String[] { "png", "jpg", "bmp" };

	private static final int XMARGIN = (Plotter.XOFF + 15);
	private static final int YMARGIN = (Plotter.YOFF + 15);

	// private static final float DEF_WORDMODE_LOWER =

	// global variables accessible from catcher class
	private static int width, height;

	public static void main(String[] args) {

		// get arguments
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(ALLOWED_ARGS, args);
		} catch (InvalidArgumentsException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}

		// check if required arguments are there
		if (!arguments.isSet("seq1") || !arguments.isSet("seq2") || !arguments.isSet("matrix") || !arguments.isSet("outfile")) {
			printUsage();
			System.exit(1);
		}

		// silent mode?
		boolean silent = arguments.isSet("silent");

		// output format
		String format = null;
		if (arguments.isSet("format")) {
			format = arguments.getValue("format").toLowerCase();
			if (!contains(VALID_OUTFORMATS, format)) {
				System.err.println("Error: Invalid output format: " + format);
				System.exit(1);
			}
		} else
			format = DEF_OUTFORMAT;

		// is width/height AND zoom set?
		if ((arguments.isSet("maxwidth") || arguments.isSet("maxheight")) && arguments.isSet("zoom")) {
			System.out.println("Error: Parameters maxwidth/maxheight and zoom cannot be used simultanously.");
			System.out.println("maxwidth/maxheight automatically implies a zoom factor whereas a zoom factor implies the plot size");
			System.exit(1);
		}

		// load substitution matrix
		if (!silent)
			System.out.println("Loading substitution matrix...");
		SubstitutionMatrix submat = null;
		try {
			submat = SubstitutionMatrix.loadFromFile(arguments.getValue("matrix"));
			// need to guess if it is a protein or DNA matrix
			submat.guessIfSubstitutionMatrix();
		} catch (IOException e) {
			System.err.println("Error: Could not load substitution matrix file:");
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (InvalidSubMatFileException e) {
			System.err.println("Error: Invalid substitution matrix file:");
			System.err.println(arguments.getValue("matrix"));
			System.exit(1);
		}

		// load first sequence
		if (!silent)
			System.out.println("Loading sequence from " + arguments.getValue("seq1"));
		Sequence seq1 = null;
		try {
			seq1 = FASTAReader.readFile(arguments.getValue("seq1"), submat);
		} catch (IOException e) {
			System.err.println("Error: Could not load first sequence file:");
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (InvalidFASTAFileException e) {
			System.err.println("Error: File could not be recognized as FASTA format file:");
			System.err.println(arguments.getValue("seq1"));
			System.err.println("Problem: " + e.getMessage());
			System.exit(1);
		}

		// load second sequence
		if (!silent)
			System.out.println("Loading sequence from " + arguments.getValue("seq2"));
		Sequence seq2 = null;
		try {
			seq2 = FASTAReader.readFile(arguments.getValue("seq2"), submat);
		} catch (IOException e) {
			System.err.println("Error: Could not load second sequence file:");
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (InvalidFASTAFileException e) {
			System.err.println("Error: File could not be recognized as FASTA format file:");
			System.err.println(arguments.getValue("seq2"));
			System.err.println("Problem: " + e.getMessage());
			System.exit(1);
		}
		// complementary
		if (arguments.isSet("secondcomp"))
			Sequence.complementarizeSequence(seq2);

		// dimension and dotplot parameters
		int word = -1, window = -1;
		try {

			// load dotplot calculation parameters
			if (arguments.isSet("word"))
				word = Integer.parseInt(arguments.getValue("word"));
			else if (arguments.isSet("window"))
				word = 0;
			else
				word = DEF_WORD;
			window = (arguments.isSet("window")) ? Integer.parseInt(arguments.getValue("window")) : DEF_WINDOW;
		} catch (NumberFormatException e) {
			System.err.println("Error: Invalid integer number:");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// display parameters
		// lower
		float lower = -1;
		if (arguments.isSet("lower"))
			lower = Float.parseFloat(arguments.getValue("lower")) / 100f;
		else {
			if (word > 0)
				lower = 0f;
			else
				lower = Float.NaN; // will be set later when we have dotplot
									// scores
		}
		// upper
		float upper = -1;
		if (arguments.isSet("upper")) {
			upper = Float.parseFloat(arguments.getValue("upper")) / 100f;
		} else {
			if (word > 0)
				upper = 0.2f;
			else
				upper = 1.0f;
		}
		// greyscale
		float greyscale = -1;
		if (arguments.isSet("greyscale")) {
			greyscale = Float.parseFloat(arguments.getValue("greyscale")) / 100f;
		} else {
			if (word > 0)
				greyscale = 0f;
			else
				greyscale = 0.5f;
		}

		// get parameter set
		ParameterSet params = new ParameterSet(-1, word, window, -1, -1, -1, -1, ClientGlobals.extractFilename(arguments.getValue("matrix").toLowerCase()));
		// set correct coordinates
		try {
			setCoordinates(params, seq1, seq2, arguments);

			// plot size
			// zoom specified from user?
			if (arguments.isSet("zoom")) {
				// yes
				int zoom = Integer.parseInt(arguments.getValue("zoom"));
				width = XMARGIN + (int) Math.ceil((float) seq1.getLength() / (float) zoom);
				height = YMARGIN + (int) Math.ceil((float) seq2.getLength() / (float) zoom);

				/*
				 * // plot too small? if (width-XMARGIN <= 1 || height-YMARGIN
				 * <= 1) {
				 * 
				 * }
				 */

				params.zoom = zoom;

			} else {
				// get size from user of def values
				width = (arguments.isSet("maxwidth")) ? Integer.parseInt(arguments.getValue("maxwidth")) : DEF_WIDTH;
				height = (arguments.isSet("maxheight")) ? Integer.parseInt(arguments.getValue("maxheight")) : DEF_HEIGHT;

				// calc zoom factor
				params.zoom = calcZoom(width, height, params.seq1Start, params.seq1Stop, params.seq2Start, params.seq2Stop);
			}

			// plot too small?
			if (width - XMARGIN <= 1 || height - YMARGIN <= 1) {
				System.err.println("Error: This plot would become too small.");
				System.err.println("Please use bigger plot size or smaller zoom factor.");
				System.exit(1);
			}

			// invalid values?
			if (params.seq1Start >= params.seq1Stop || params.seq2Start >= params.seq2Stop) {
				System.err.println("Error: To coordinates may not be greater than from coordinates!");
				System.exit(1);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error: Invalid integer number:");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// get suffix array
		boolean saSecondSeq = false;
		SuffixArray sa = null;
		if (!arguments.isSet("safile")) {
			// calculate suffix array for the longer sequence
			if (!silent)
				System.out.println("Calculating suffix array... ");
			if (seq1.getLength() >= seq2.getLength())
				sa = new SuffixArray(seq1, submat.getAlphabetSize(), null);
			else {
				sa = new SuffixArray(seq2, submat.getAlphabetSize(), null);
				saSecondSeq = true;
			}
		} else {

			// read it from file
			String safile = arguments.getValue("safile");
			saSecondSeq = arguments.isSet("sasecondseq");

			if (!silent)
				System.out.println("Reading suffix array from " + arguments.getValue("safile"));

			try {

				if (!saSecondSeq) {
					// read for first file
					sa = SuffixArray.loadFromFile(new File(safile), seq1.getSequenceData());
				} else
					// read for second file
					sa = SuffixArray.loadFromFile(new File(safile), seq2.getSequenceData());
				// correct size?
				Sequence saSeq = (saSecondSeq) ? seq2 : seq1;
				if (sa.getLength() != saSeq.getLength()) {
					System.err.println("Error: Suffix array does not appear to belong to that sequence.");
					System.exit(1);
				}
			} catch (IOException e) {
				System.err.println("Error: Could not load suffix array from file:");
				// dirty hack: if java says it's not a gzip file it is probably
				// just an invalid suffix array file
				if (e.getMessage().toLowerCase().contains("gzip"))
					System.err.println("Invalid suffix array file.");
				else
					System.err.println(e.getMessage());

				System.exit(1);
			} catch (OutOfMemoryError e) {
				// must be an invalid suffix array file
				System.err.println("Error: Invalid suffix array file.");
				System.exit(1);
			}
		}

		// Sequence.complementarizeSequence(seq2);

		// everything ready, calculate dotplot
		if (!silent)
			System.out.println("Calculating dotplot... ");
		Callback catcher = new Callback();
		DotMatrix dm = new DotMatrix(seq1.getSequenceData(), seq2.getSequenceData(), seq1.getName(), seq2.getName(), sa, params, submat, catcher, catcher,
				saSecondSeq);

		if (!silent)
			System.out.println("Creating image and writing to file... ");
		// create plotter, calculate good display parameters
		Plotter plotter = new Plotter(dm, catcher);
		// calc
		float minScore = dm.getMinDotScore();
		float maxScore = dm.getMaxDotScore();

		// calculate good lower value now?
		if (Float.isNaN(lower)) {
			// calculate good display value
			float gooddisp = (dm.getAvgDotScore() + maxScore) * 0.40f;
			lower = (gooddisp - minScore) / (maxScore - minScore);
		}

		plotter.reCalc((float) ((maxScore - minScore) * lower + minScore), (float) ((maxScore - minScore) * upper + minScore), greyscale);
		// TODO next: parametrize
		// TODO next: display parameters for window mode

		// save image to file
		try {
			File outfile = new File(arguments.getValue("outfile"));
			ImageIO.write(plotter.getFullImage(), format, outfile);
		} catch (Exception e) {
			System.err.println("Error: Could not write image file:");
			System.err.println(e.getMessage());
			System.exit(1);
		}

	}

	private static void setCoordinates(ParameterSet params, Sequence seq1, Sequence seq2, CommandLineArguments arguments) {
		// set coordinates of the given argument is set
		if (arguments.isSet("from1"))
			params.seq1Start = getCoordinate(arguments.getValue("from1"), seq1);
		else
			params.seq1Start = 0;

		if (arguments.isSet("to1"))
			params.seq1Stop = getCoordinate(arguments.getValue("to1"), seq1);
		else
			params.seq1Stop = seq1.getLength() - 1;

		if (arguments.isSet("from2"))
			params.seq2Start = getCoordinate(arguments.getValue("from2"), seq2);
		else
			params.seq2Start = 0;

		if (arguments.isSet("to2"))
			params.seq2Stop = getCoordinate(arguments.getValue("to2"), seq2);
		else
			params.seq2Stop = seq2.getLength() - 1;

		// correct coordinates if needed
		if (params.seq1Start < 0)
			params.seq1Start = 0;
		if (params.seq1Stop >= seq1.getLength())
			params.seq1Stop = seq1.getLength() - 1;
		if (params.seq2Start < 0)
			params.seq2Start = 0;
		if (params.seq2Stop >= seq2.getLength())
			params.seq2Stop = seq2.getLength() - 1;

		// System.out.println(params.zoom);

	}

	// convert coordinate specification to number
	// if it contains a % => calculate fraction
	private static final int getCoordinate(String argument, Sequence seq) {
		if (!argument.contains("%"))
			return Integer.parseInt(argument);
		else {
			// contains a %
			float fraction = Float.parseFloat(argument.substring(0, argument.indexOf("%"))) / 100;
			return (int) (seq.getLength() * fraction);
		}

	}

	private static int calcZoom(int imgwidth, int imgheight, int x1, int x2, int y1, int y2) {

		// actual drawing area
		int drawWidth = imgwidth - XMARGIN;
		int drawHeight = imgheight - YMARGIN;

		// calculate maximum possible x&y zoom
		int xzoom = (int) Math.ceil((float) (x2 - x1) / (float) drawWidth);
		int yzoom = (int) Math.ceil((float) (y2 - y1) / (float) drawHeight);

		// avoid zoom = 0
		if ((xzoom == 0) && (yzoom == 0))
			xzoom = 1;

		// use higher value
		return (xzoom > yzoom) ? xzoom : yzoom;

	}

	private static void printUsage() {
		// header
		System.err.println();
		System.err.println("Gepard " + ClientGlobals.VERSION + " - command line mode");
		System.err.println();
		System.err.println("Reference:");
		System.err.println("Krumsiek J, Arnold R, Rattei T");
		System.err.println("Gepard: A rapid and sensitive tool for creating dotplots on genome scale.");
		System.err.println("Bioinformatics 2007; 23(8): 1026-8. PMID: 17309896");
		System.err.println();

		// usage
		System.err.println("Parameters are supplied as -name value");
		System.err.println();
		System.err.println("Required parameters:");
		System.err.println("  -seq1:        first sequence file");
		System.err.println("  -seq2:        second sequence file");
		System.err.println("  -matrix:      substitution matrix file");
		System.err.println("  -outfile:     output file name");
		System.err.println();
		System.err.println("Dotplot image parameters:");
		System.err.println("  -maxwidth:    maximum width of the generated image (default: " + DEF_WIDTH + ")");
		System.err.println("  -maxheight:   maximum height of the generated image (default: " + DEF_HEIGHT + ")");
		System.err.println("  -zoom:        specify a zoom factor for the dotplot");
		System.err.println("  note: you can only use maxwidth/maxheight OR zoom");
		System.err.println("        when using maxwidth/maxheight the program tries to generate the largest");
		System.err.println("        possible dotplot within the given bounds");
		System.err.println("  -format:      output format, one of:  'png', 'jpg', 'bmp' (default:" + DEF_OUTFORMAT + ")");
		System.err.println();
		System.err.println("Dotplot computation parameters:");
		System.err.println("  -secondcomp   use complementary of second sequence");
		System.err.println("  -word:        word length for suffix array mode (default: " + DEF_WORD + ")");
		System.err.println("  -window:      window size for ordinary dotplot mode (default: " + DEF_WINDOW + ")");
		System.err.println("  if a window value and no word value is specified, word=0 is assumed");
		System.err.println();
		System.err.println("Suffix array parameters:");
		System.err.println("  -safile       load suffix array from file instead of calculating it");
		System.err.println("  -sasecondseq  the suffix array is for the second sequence");
		System.err.println("  if -sasecondseq is NOT specified, the suffix array will be used for first sequence");
		System.err.println();
		System.err.println("Coordinate parameters (absolute values of % values)");
		System.err.println("  -from1,-to1   coordinates of first sequence");
		System.err.println("  -from2,-to2   coordinates of second sequence");
		System.err.println("  if these parameters are not specified the full sequence will be used");
		System.err.println();
		System.err.println("Display parameters:");
		System.err.println("  -lower        lower limit for dot intensity (in %)");
		System.err.println("  -upper        upper limit for dot intensity (in %)");
		System.err.println("  -greyscale    greyscale start value (in %)");
		System.err.println();
		System.err.println("Miscellaneous:");
		System.err.println("  -silent       generate no output (except error messages)");
		System.err.println();

	}

	private static boolean contains(Object[] array, Object item) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(item))
				return true;
		}
		return false;
	}

	private static class Callback implements DotMatrixCallback, AbortionChecker, DrawTarget {

		public void dotmatrixCalcStatus(float percent) {
			// TODO Auto-generated method stub

		}

		public void tellAborted() {
			// TODO Auto-generated method stub

		}

		public int tellCallbackStep(int wordlen, int windowsize) {
			// TODO Auto-generated method stub
			return 1000;
		}

		// AbortionChecker adapter from here
		public boolean dotplotAborted() {
			// do not do anything, simply notice caller that nothing was aborted
			return false;
		}

		// DrawTarget adapter from here
		public int getHeight() {
			// TODO Auto-generated method stub
			return height;
		}

		public int getWidth() {
			// TODO Auto-generated method stub
			return width;
		}

		public void setPreferredSize(Dimension dimension) {
			// ignore!
		}
	}

}
