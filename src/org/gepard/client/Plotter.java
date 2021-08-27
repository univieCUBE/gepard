package org.gepard.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.gepard.common.DotMatrix;

// plots DM objects including information

public class Plotter {

	// member variables
	// int dpwidth, dpheight; // dotplot dimensions
	DotMatrix dm;

	// selection coordinates
	int selX1 = -1, selY1 = -1, selX2 = -1, selY2 = -1;

	public static final int XOFF = 85;
	public static final int YOFF = 110;
	private static final int GC_RATIO_OFF = 160;
	private static final Font INFO_FONT = new Font("Monospaced", Font.PLAIN, 12);
	private static final Font GENE_FONT = new Font("Monospaced", Font.PLAIN, 11);

	private static final int NAME_MIN_HOR_MARGIN = 10;
	private static final int NAME_VERT_MARGIN = 30;

	// crosshair
	private int crossX = -1, crossY = -1;
	private int crossPosX, crossPosY;

	// gene tooltip
	private int genex = -1, geney = -1;

	private int minPlotWidth = 0;

	private BufferedImage offscreen; // " "

	private String infoPlotTitle;
	private String infoZoom;
	private String infoWordLen;
	private String infoWindowSize;
	private String infoSubmat;
	private String infoProgram;
	private String infoGCRatio1;
	private String infoGCRatio2;

	private String seq1Name, seq2Name;

	private int imgWidth, imgHeight;

	private DrawTarget dp;

	private String genehor;
	private String genevert;

	// private Color greyscale[]; // greyscale helper array

	public Plotter(DotMatrix dm, DrawTarget dp) {

		this.dm = dm;
		this.dp = dp;

		// create backbuffer
		offscreen = new BufferedImage(dm.getWidth(), dm.getHeight(), BufferedImage.TYPE_INT_RGB);

		// create information strings (except plot title)
		infoZoom = "Zoom: " + dm.getParameterSet().zoom + " : 1";
		infoWordLen = "Word length: " + dm.getParameterSet().wordLength;
		infoWindowSize = "Window size: " + dm.getParameterSet().windowSize;
		infoSubmat = "Matrix: " + dm.getParameterSet().submatName;
		infoProgram = "Program: " + ClientGlobals.APPNAME + " (" + ClientGlobals.VERSION + ")";

		NumberFormat fmt = new DecimalFormat("#####0.0000", new DecimalFormatSymbols(Locale.ENGLISH));
		infoGCRatio1 = "GC ratio seq1: " + fmt.format(dm.getGCratio1());
		infoGCRatio2 = "GC ratio seq2: " + fmt.format(dm.getGCratio2());

		// calculate the minimum width of the dp, need for scrollpane & picture
		// export
		// minimum width is determined using text width of seq vs. seq string,
		// the
		// GC ratio string and the program name string
		FontMetrics fontMetrics = offscreen.getGraphics().getFontMetrics(INFO_FONT);
		int infoGCRatioWidth = XOFF + GC_RATIO_OFF + fontMetrics.stringWidth(infoGCRatio1);
		int infoProgramWidth = XOFF + GC_RATIO_OFF + fontMetrics.stringWidth(infoProgram);

		// store sequence names
		int hormargin = Math.max(Math.max(NAME_MIN_HOR_MARGIN, fontMetrics.stringWidth(dm.getParameterSet().seq1Start + "") * 2),
				fontMetrics.stringWidth(dm.getParameterSet().seq1Stop + "") * 2);

		seq1Name = ClientGlobals.cutString(dm.getSeq1Name(), (int) ((dm.getWidth() - hormargin) / fontMetrics.charWidth(' ')));
		seq2Name = ClientGlobals.cutString(dm.getSeq2Name(), (int) ((dm.getHeight() - NAME_VERT_MARGIN) / fontMetrics.charWidth(' ')));

		// calc minimum (=> maximum of the two widths above
		minPlotWidth = Math.max(infoGCRatioWidth, infoProgramWidth);

		// calc image dimensions
		imgWidth = (minPlotWidth > XOFF + dm.getWidth()) ? minPlotWidth + 5 : XOFF + dm.getWidth() + 5;
		imgHeight = YOFF + dm.getHeight() + 5;

		// now we need to create the plot title with corretly cut strings
		String name1 = dm.getSeq1Name();
		String name2 = dm.getSeq2Name();
		infoPlotTitle = "";// seq1Name + " vs. " + seq2Name;
		// step 1: calc max amount of chars for plot title
		int charsfortitle = ((imgWidth - XOFF) / fontMetrics.charWidth(' ')) - 5;
		int halfchars = charsfortitle / 2;
		// step 2 calc max and min seq name length
		int maxnamelen = Math.max(name1.length(), name2.length());
		int minnamelen = Math.min(name1.length(), name2.length());
		// step 3: distribute characters
		// if both names are shorter than half of the available characters:
		// don't cut
		if (maxnamelen <= halfchars)
			infoPlotTitle = name1 + " vs. " + name2;
		// if both names are longer than the half... cut both!
		else if (minnamelen > halfchars)
			infoPlotTitle = ClientGlobals.cutString(name1, halfchars) + " vs. " + ClientGlobals.cutString(name2, halfchars);
		// if one seq is shorter and one is longer than the half: completely
		// print shorter sequence
		// and leave rest to the longer one
		else {
			if (name1.length() < name2.length())
				// name1 is the short one
				infoPlotTitle = name1 + " vs. " + ClientGlobals.cutString(name2, charsfortitle - name1.length());
			else
				// name2 is the short one
				infoPlotTitle = ClientGlobals.cutString(name1, charsfortitle - name2.length()) + " vs. " + name2;

		}

		dp.setPreferredSize(new Dimension(imgWidth, imgHeight));

	}

	// sets the current crosshair position
	// recalculates all dotplot data
	public void reCalc(float colorlower, float colorupper, float greyscalestart) {

		// store dot matrix data
		float[][] matrix = dm.getDotMatrix();
		

		int colorDot = 0;
		int r = 0, g = 0, b = 0; // final color values

		for (int i = 0; i < dm.getWidth(); i++) {
			for (int j = 0; j < dm.getHeight(); j++) {

				if (matrix[i][j] >= colorlower) {
					float dotscore = matrix[i][j];
					if (dotscore > colorupper)
						dotscore = colorupper;

					float ratio = greyscalestart + ((float) (dotscore - colorlower) / (float) (colorupper - colorlower)) * (1f - greyscalestart);
					// calculate grey value
					colorDot = (int) ((1f - ratio) * 255f);
				} else
					colorDot = 255;
				r = colorDot;
				g = colorDot;
				b = colorDot;

				offscreen.setRGB(i, j, r * 0x10000 + g * 0x100 + b); // is this
																		// still
																		// needed?
			}
		}
	}

	public void setSelection(int x1, int y1, int x2, int y2) {
		// correct values if outside of dotplot
		if (x1 < XOFF)
			x1 = XOFF;
		if (x2 < XOFF)
			x2 = XOFF;
		if (y1 < YOFF)
			y1 = YOFF;
		if (y2 < YOFF)
			y2 = YOFF;

		if (x1 >= XOFF + dm.getWidth())
			x1 = XOFF + dm.getWidth() - 1;
		if (x2 >= XOFF + dm.getWidth())
			x2 = XOFF + dm.getWidth() - 1;
		if (y1 >= YOFF + dm.getHeight())
			y1 = YOFF + dm.getHeight() - 1;
		if (y2 >= YOFF + dm.getHeight())
			y2 = YOFF + dm.getHeight() - 1;

		selX1 = x1;
		selY1 = y1;
		selX2 = x2;
		selY2 = y2;
	}

	public void setCrossHair(int x, int y, int posx, int posy) {
		// correct values if outside plot
		if (x < XOFF)
			x = XOFF;
		if (y < YOFF)
			y = YOFF;
		if (x >= XOFF + dm.getWidth())
			x = XOFF + dm.getWidth() - 1;
		if (y >= YOFF + dm.getHeight())
			y = YOFF + dm.getHeight() - 1;
		// store values
		crossX = x;
		crossY = y;
		crossPosX = posx;
		crossPosY = posy;
	}

	public void moveCrossHair(int xmove, int ymove) {
		crossPosX += xmove;
		crossPosY += ymove;
		// correct values if necessary
		/*
		 * if (crossPosX<0) crossPosX = 0; if (crossPosX>=dm.getSeq1Length())
		 * crossPosX = dm.getSeq1Length(); if (crossPosY<0) crossPosY = 0; if
		 * (crossPosY>=dm.getSeq2Length()) crossPosY = dm.getSeq2Length();
		 */

	}

	public void removeCrossHair() {
		crossX = -1;
		crossY = -1;
	}

	public void removeSelection() {
		selX1 = -1;
		selY1 = -1;
	}

	public void redraw(Graphics2D g) {

		// whiten (image dimensions but at least draw panel dimensions
		int whitenWidth = Math.max(imgWidth, dp.getWidth());
		int whitenHeight = Math.max(imgHeight, dp.getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, whitenWidth, whitenHeight);
		// draw frame
		g.setColor(Color.BLACK);
		g.drawRect(XOFF - 1, YOFF - 1, dm.getWidth() + 1, dm.getHeight() + 1);

		// draw plot information
		g.setFont(INFO_FONT);

		FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

		// draw first sequence name
		g.drawString(seq1Name, XOFF + (dm.getWidth() / 2) - (fontMetrics.stringWidth(seq1Name) / 2), YOFF - 7);
		// draw second sequence name, rotated
		Font oldFont = g.getFont();
		Font rotated = getRotatedFont(oldFont);
		g.setFont(rotated);
		g.drawString(seq2Name, XOFF - 7, YOFF + (dm.getHeight() / 2) + (fontMetrics.stringWidth(seq2Name) / 2));
		g.setFont(oldFont);

		g.drawString(infoPlotTitle, XOFF, 13);
		g.drawString(infoZoom, XOFF, 28);
		g.drawString(infoWordLen, XOFF, 43);
		g.drawString(infoWindowSize, XOFF, 58);
		g.drawString(infoSubmat, XOFF, 73);
		// draw GC ratio (if nucleotide dotplot)
		if (this.dm.isNucleotideMatrix()) {
			g.drawString(infoGCRatio1, XOFF + GC_RATIO_OFF, 43);
			g.drawString(infoGCRatio2, XOFF + GC_RATIO_OFF, 58);
		}
		g.drawString(infoProgram, XOFF + GC_RATIO_OFF, 73);

		// draw start/end coordinates + lines

		g.drawLine(XOFF, YOFF - 10, XOFF, YOFF);
		g.drawString(dm.getParameterSet().seq1Start + "", XOFF - 3, YOFF - 17);
		g.drawLine(XOFF + dm.getWidth() - 1, YOFF - 10, XOFF + dm.getWidth() - 1, YOFF);
		g.drawString(dm.getParameterSet().seq1Stop + "", XOFF + dm.getWidth() - fontMetrics.stringWidth(dm.getParameterSet().seq1Stop + "") + 3, YOFF - 17);
		g.drawLine(XOFF - 10, YOFF, XOFF, YOFF);
		g.drawString(dm.getParameterSet().seq2Start + "", XOFF - 17 - fontMetrics.stringWidth(dm.getParameterSet().seq2Start + ""), YOFF + 4);
		g.drawLine(XOFF - 10, YOFF + dm.getHeight() - 1, XOFF, YOFF + dm.getHeight() - 1);
		g.drawString(dm.getParameterSet().seq2Stop + "", XOFF - 17 - fontMetrics.stringWidth(dm.getParameterSet().seq2Stop + ""), YOFF + dm.getHeight() + 2);
		// draw plot
		g.drawImage(offscreen, XOFF, YOFF, null);
		// draw selection
		if (selX1 != -1) {
			int x = 0, y = 0, width = 0, height = 0;
			if (selX1 < selX2) {
				x = selX1;
				width = selX2 - selX1;
			} else {
				x = selX2;
				width = selX1 - selX2;
			}
			if (selY1 < selY2) {
				y = selY1;
				height = selY2 - selY1;
			} else {
				y = selY2;
				height = selY1 - selY2;
			}

			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}

		if (crossX > -1) {
			// draw crosshair
			g.setColor(Color.BLUE);
			g.drawLine(crossX, YOFF, crossX, crossY - 1);
			g.drawLine(crossX, crossY + 1, crossX, dm.getHeight() + YOFF);
			g.drawLine(XOFF, crossY, crossX - 1, crossY);
			g.drawLine(crossX + 1, crossY, dm.getWidth() + XOFF, crossY);
			// draw position
			g.drawString(crossPosX + ", " + crossPosY, crossX + 5, crossY - 5);
		}

		// draw gene tool tip
		if (genex > -1) {

			// get gene name widths
			FontMetrics genemetrics = offscreen.getGraphics().getFontMetrics(GENE_FONT);
			int texthorwidth = genemetrics.stringWidth(genehor);
			int textvertwidth = genemetrics.stringWidth(genevert);
			int textwidth = (texthorwidth > textvertwidth) ? texthorwidth : textvertwidth;
			// and height
			int textheight = genemetrics.getHeight();
			// and the strange vertical shifting
			int yshift = genemetrics.getAscent();

			final int GENE_YOFF = 20;
			final int GENE_XOFF = 0;

			// draw gene name box
			g.setFont(GENE_FONT);
			g.setColor(Color.BLACK);
			g.drawRect(genex + GENE_XOFF, geney + GENE_YOFF, textwidth + 2, textheight * 2 + 2);
			g.setColor(Color.WHITE);
			g.fillRect(genex + GENE_XOFF + 1, geney + GENE_YOFF + 1, textwidth + 2 - 1, textheight * 2 + 2 - 1);

			// draw gene names
			g.setColor(Color.BLACK);
			g.drawString(genehor, genex + GENE_XOFF + 1, geney + GENE_YOFF + yshift + 1);
			g.drawString(genevert, genex + GENE_XOFF + 1, geney + GENE_YOFF + textheight + yshift + 1);

		}

	}

	private Font getRotatedFont(Font original) {
		AffineTransform fontAT = new AffineTransform();
		fontAT.rotate(Math.PI / 2 * 3);
		return original.deriveFont(fontAT);
	}

	public BufferedImage getFullImage() {

		// create new image which is large enough to hold the total plot
		BufferedImage fullImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		// create graphics context
		Graphics2D g = (Graphics2D) fullImage.getGraphics();
		// draw plot into this image & return
		redraw(g);

		return fullImage;
	}

	public void setGeneToolTip(int x, int y, String genehor, String genevert) {
		genex = x;
		geney = y;
		this.genehor = ClientGlobals.GENE_NAME_HOR_PREFIX + genehor;
		this.genevert = ClientGlobals.GENE_NAME_VERT_PREFIX + genevert;
	}

	public String getGeneNameInformation() {
		return this.genehor + "\n" + this.genevert;
	}

	public void noToolTips() {
		genex = -1;
	}

}