package org.gepard.client.userinterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import org.gepard.client.Controller;
import org.gepard.common.Sequence;
import org.gepard.common.SubstitutionMatrix;

// information panel at the bottom (coordinates, alignments...)

public class InfoPanel extends JPanel implements ComponentListener {

	private static final long serialVersionUID = -499089675210568791L;
	
	
	private Graphics2D bufferGraphics;	// backbuffer stuff
	private BufferedImage offscreen;  	//     "        "
	private Graphics2D bufferFlashText;	// flashtext buffer stuff
	private BufferedImage offFlashText;  	//     "        "
	
	private static final int COORDINATES_X = 10;
	private static final int COORDINATES_Y = 0;
	
	private static final int ALIGNMENT_MARGIN_LEFT = 10;
	private static final int ALIGNMENT_MARGIN_HOR_ADD = 10;
	private static final int MAX_ALIGNMENT_LENGTH = 1000; 
	private int ALIGNMENT_Y=0;
	
	private static final int MARGIN_BOTTOM = 5;
	
	// pre-create char arrays & marker string for fast alignment displaying
	private char[] strseq1 = new char[MAX_ALIGNMENT_LENGTH];
	private char[] strseq2 = new char[MAX_ALIGNMENT_LENGTH];
	private char[] strsim  = new char[MAX_ALIGNMENT_LENGTH]; 
	private char[] strpos1 = new char[MAX_ALIGNMENT_LENGTH];	
	private char[] strpos2 = new char[MAX_ALIGNMENT_LENGTH];
	private String markers;
	
	private boolean showingFlashText = false;
	
	private Controller ctrl;
	
	private int alignlen;
	
	// internally stored alignment data
	private byte[] m_seq1, m_seq2;
	private int m_seq1pos, m_seq2pos;
	private int m_off1, m_off2;
	private int m_arrlen1, m_arrlen2;
	private SubstitutionMatrix m_submat;
	private boolean m_complementary;
	


	public InfoPanel(Controller ctrl) {
		// store controller reference
		this.ctrl = ctrl;
		// create backbuffer
		offscreen = new BufferedImage(1500, 200, BufferedImage.TYPE_INT_RGB);
		bufferGraphics = (Graphics2D)offscreen.getGraphics();
		bufferGraphics.fillRect(0,0,1500,200);
		bufferGraphics.setFont(new Font("Courier", Font.PLAIN, 12));
		// create second backbuffer
		offFlashText = new BufferedImage(1500, 200, BufferedImage.TYPE_INT_RGB);
		bufferFlashText = (Graphics2D)offFlashText.getGraphics();
		bufferFlashText.fillRect(0,0,1500,200);
		bufferFlashText.setFont(new Font("Courier", Font.PLAIN, 12));
		// add event handle
		addComponentListener(this);
		// calc alignment's y position
		ALIGNMENT_Y = bufferGraphics.getFontMetrics(bufferGraphics.getFont()).getHeight();
		
	}
	
	public void paintComponent(Graphics g) {
		// blit
		if (!showingFlashText)
			g.drawImage(offscreen, 0,0, this);
		else
			g.drawImage(offFlashText, 0,0, this);
	}
	
	public void showAlignments(boolean showalign) {
		// request correct panel size
		if (showalign)
			setPreferredSize(new Dimension(1,MARGIN_BOTTOM+8*bufferGraphics.getFontMetrics(bufferGraphics.getFont()).getHeight()));
		else
			setPreferredSize(new Dimension(1,MARGIN_BOTTOM+bufferGraphics.getFontMetrics(bufferGraphics.getFont()).getHeight()));
		// invalidate this panel and validate container
		invalidate();
		ctrl.validateContainer();
		
	}
	
	public void setCoordinates(int x, int y, String multiName1, String multiName2) {
		String toDraw = "Position: " + x+", "+ y;
		
		// assemble multi names
		String full="";
		// first one
		if (multiName1!=null)
			full = "H: " + multiName1;
		// space?
		if (multiName1!=null && multiName2!=null)
			full += "  ";
		// second one
		if (multiName2!=null)
			full += "V: " + multiName2;
		
		// blank area
		FontMetrics fm = bufferGraphics.getFontMetrics(bufferGraphics.getFont());
		bufferGraphics.setColor(Color.WHITE);
		bufferGraphics.fillRect(COORDINATES_X,
				COORDINATES_Y, 
				1000,
				fm.getHeight());
		// draw coordinates
		bufferGraphics.setColor(Color.BLACK);
		bufferGraphics.drawString(toDraw + "     " + full, COORDINATES_X,COORDINATES_Y+fm.getAscent());
		// cause repaint
		this.repaint();		
	}
	
	public void setSelection(int x1, int y1, int x2, int y2) {
		// determine correct corners
		int rx1=0,ry1=0, rx2=0, ry2=0;
		rx1 = (x1<x2) ? x1 : x2;
		rx2 = (x1>x2) ? x1 : x2;
		ry1 = (y1<y2) ? y1 : y2;		
		ry2 = (y1>y2) ? y1 : y2;
		// determine string to be drawn
		String toDraw = "Selection: " 
			+ rx1 + "," + ry1
			+ "  -  " 
			+ rx2 + "," + ry2 +
			" (" + Math.abs(rx1-rx2) + " x " + Math.abs(ry1-ry2) +")";
		// blank area
		FontMetrics fm = bufferGraphics.getFontMetrics(bufferGraphics.getFont());
		bufferGraphics.setColor(Color.WHITE);
		bufferGraphics.fillRect(COORDINATES_X,
				COORDINATES_Y, 
				1000,
				fm.getHeight());
		// draw selection
		bufferGraphics.setColor(Color.BLACK);
		bufferGraphics.drawString(toDraw, COORDINATES_X,COORDINATES_Y+fm.getAscent());
		// cause repaint
		this.repaint();		
	}
	
	
	
	public void showAlignment(byte[] seq1, byte[] seq2, int offset1, int offset2,
			int arrlength1, int arrlength2, int seq1pos, int seq2pos, 
			SubstitutionMatrix submat, boolean complementary) {
		
//		boolean reverse=true;
		
		// store alignment data
		m_seq1 = seq1;
		m_seq2 = seq2;
		m_off1 = offset1;
		m_off2 = offset2;
		m_arrlen1 = arrlength1;
		m_arrlen2 = arrlength2;
		m_seq1pos = seq1pos;
		m_seq2pos = seq2pos;
		m_submat = submat;
		m_complementary = complementary;
		
		// blank paint area
		bufferGraphics.setColor(Color.WHITE);
		bufferGraphics.fillRect(0,ALIGNMENT_Y,  this.getSize().width,  this.getSize().height-ALIGNMENT_Y);
	
		bufferGraphics.setColor(Color.BLACK);
	
		// calculate range
		int backward = alignlen / 2;
	//	if ((seq1pos - backward - offset1) < 0) seq1pos = (alignlen / 2) + offset1;
	//	if ((seq2pos - backward - offset2) < 0) seq2pos = (alignlen / 2) + offset2;
		int forward = alignlen / 2;
	//	if ((seq1pos + forward - offset1) >= arrlength1) seq1pos = 
	//		arrlength1 - (alignlen / 2) - 1 + offset1;
	//	if ((seq2pos + forward - offset2) >= arrlength2) 
	//		seq2pos = arrlength2 - (alignlen / 2) - 1 + offset2; 
				
		// empty position byte arrays
		Arrays.fill(strpos1, ' '); 
		Arrays.fill(strpos2, ' ');
		
		byte[] complmap = Sequence.getComplementaryMap();
		
		// iterate through range
		int j=0;
		for (int i=-backward; i<=forward; i++) {
			byte a=-1, b=-1;
			// get characters from both sequences
			if (seq1pos+i-offset1 >= 0 && seq1pos+i-offset1 < seq1.length) 
				a = seq1[seq1pos+i-offset1];
			if (!complementary) {
				if (seq2pos+i-offset2 >= 0 && seq2pos+i-offset2 < seq2.length)
					b = seq2[seq2pos+i-offset2];
			} else {
				if (seq2pos-i-offset2 >= 0 && seq2pos-i-offset2 < seq2.length)
					b = complmap[seq2[seq2pos-i-offset2]];
			}

			// insert characters into strings
			if (a != -1) 	strseq1[j] = submat.reverseMap(a);
			else 			strseq1[j] = ' ';
			
			if (b != -1) 	strseq2[j] = submat.reverseMap(b);
			else 			strseq2[j] = ' ';
			
			// are there two characters to be compared?
			if (a != -1 && b != -1) 
				strsim[j] = getNuclSimCode(a, b, submat);
			else
				strsim[j] = ' ';
			
			j++;
			
		}
		
		int oddadd = (alignlen%2!=0) ? 1 : 0;
		
		// generate first position string
		if (seq1pos-backward-offset1 >= 0)
			insertNumIntoCharArray(strpos1, 0, seq1pos-backward);
		insertNumIntoCharArray(strpos1,  forward, seq1pos);
		if (seq1pos+forward-offset1 <= seq1.length)
			insertNumIntoCharArray(strpos1, oddadd+forward+backward-numLength(seq1pos+forward-1), seq1pos+forward-1);
		
		// generate second position string
		if (!complementary) {
			if (seq2pos-backward-offset2 >= 0)
				insertNumIntoCharArray(strpos2, 0, seq2pos-backward);
			insertNumIntoCharArray(strpos2, forward, seq2pos);
			if (seq2pos+forward-offset2 <= seq2.length)
				insertNumIntoCharArray(strpos2, oddadd+forward+backward-numLength(seq2pos+forward-1), seq2pos+forward-1);
		} else {
			if (seq2pos+backward-offset2 <  seq2.length)
				insertNumIntoCharArray(strpos2, 0, seq2pos+backward);
			insertNumIntoCharArray(strpos2, forward, seq2pos);
			
			if (seq2pos-forward-offset2+1 >= 0)
				insertNumIntoCharArray(strpos2, oddadd+forward+backward-numLength(seq2pos-forward+1), seq2pos-forward+1);
		}
		
		// now generate actual string
		String pos1 = new String(strpos1);
		String pos2 = new String(strpos2);
		
		// draw the strings
		int ascent = bufferGraphics.getFontMetrics(bufferGraphics.getFont()).getAscent();
		int charheight =  bufferGraphics.getFontMetrics(bufferGraphics.getFont()).getHeight();
		bufferGraphics.drawString(pos1, 10, ALIGNMENT_Y+ascent);
		bufferGraphics.drawString(markers, 10, ALIGNMENT_Y+charheight+ascent);
		bufferGraphics.drawString(String.valueOf(strseq1,0,alignlen), 10, ALIGNMENT_Y+(charheight*2)+ascent);
		bufferGraphics.drawString(String.valueOf(strsim,0,alignlen),  10, ALIGNMENT_Y+(charheight*3)+ascent);
		bufferGraphics.drawString(String.valueOf(strseq2,0,alignlen), 10, ALIGNMENT_Y+(charheight*4)+ascent);
		bufferGraphics.drawString(markers, 10, ALIGNMENT_Y+(charheight*5)+ascent);
		bufferGraphics.drawString(pos2, 10, ALIGNMENT_Y+(charheight*6)+ascent);
		
		// cause repaint
		repaint();
	}
	
	public void removeAlignment() {
		// delete all references
		m_seq1 = null;
		m_seq2 = null;
		m_submat = null;		
		// blank paint area
		bufferGraphics.setColor(Color.WHITE);
		bufferGraphics.fillRect(0,0,  this.getSize().width,  this.getSize().height);
		// cause repaint
		repaint();
	}

	// resize event handler
	public void componentResized(ComponentEvent arg0) {
		// get size of this panel
		Dimension dim = this.getSize();
		// calculate alignment length depending on panel width
		int charwidth = bufferGraphics.getFontMetrics(bufferGraphics.getFont()).stringWidth("a");
		alignlen = ((dim.width-(ALIGNMENT_MARGIN_LEFT+ALIGNMENT_MARGIN_HOR_ADD)) / charwidth);
		// alignlen may not be larger than any of the sequence lengths
		if (ctrl.dotplotExists() ) {
			int s1len = ctrl.getDPInfo().seq1len;
			int s2len = ctrl.getDPInfo().seq2len;
			if (alignlen > ((s1len<s2len)?s1len:s2len))
				alignlen = ((s1len<s2len)?s1len:s2len);
		}
		
		// construct marker, alignlen might be zero or less if window is just being created
		if (alignlen > 5) {
			constructMarker(alignlen);
			// if there is an alignment currently shown -> update
			if (m_seq1 != null)
				showAlignment(m_seq1, m_seq2, m_off1, m_off2, m_arrlen1, m_arrlen2,
						m_seq1pos, m_seq2pos, m_submat, m_complementary);
		}
	}
	
	public void updateAlignment(boolean complemenatary) {
		// if there is an alignment currently shown -> update
		if (m_seq1 != null)
			showAlignment(m_seq1, m_seq2, m_off1, m_off2, m_arrlen1, m_arrlen2,
					m_seq1pos, m_seq2pos, m_submat, complemenatary);
	}
	
	// pre-creates the marker string for quick access later
	private void constructMarker(int alignlength) {
		int i;
		char[] cmarkers = new char[alignlength];
		cmarkers[0] = '|';
		for (i=1; i<alignlength/2; i++) cmarkers[i] = ' ';
		cmarkers[i] = '|';
		for (i=alignlength/2+1; i<alignlength-1; i++) cmarkers[i] = ' ';
		cmarkers[i] = '|';
		markers = String.valueOf(cmarkers);
	}
	
	
	private void insertNumIntoCharArray(char[] arr, int pos, int num) {
		String snum = (""+num);
		int mod = 0; if (pos+snum.length() > arr.length) mod = (pos+snum.length()) - arr.length;
		for(int i=0; i<snum.length(); i++) 
			arr[pos+i-mod] = snum.charAt(i);
	}
	
	private int numLength(int num) {
		return new String(""+num).length();
	}
	
	private char getNuclSimCode(byte a, byte b, SubstitutionMatrix submat) {
		if (a==b)
			return ':';
		else {
			int score = submat.getScore(a,b);
			if (score > 0)
				return '.';
			else
				return ' ';
		}
	}
	
	
	// unneeded event handlers
	public void componentMoved(ComponentEvent arg0) {
	}

	public void componentShown(ComponentEvent arg0) {
	}

	public void componentHidden(ComponentEvent arg0) {
	}

	public void flashMessage(String message, int seconds) {
		showingFlashText = true;
		
		bufferFlashText.setColor(Color.WHITE);
		bufferFlashText.fillRect(0,0,1500,500);
		bufferFlashText.setColor(Color.RED);
		
		FontMetrics fm = bufferGraphics.getFontMetrics(bufferFlashText.getFont());
		
		bufferFlashText.drawString(message, COORDINATES_X, COORDINATES_Y+fm.getAscent());
		repaint();
		
		// set timer to remove flashing message
		Timer x = new Timer();
		x.schedule(new EndFlashTimer(), seconds*1000);
		

	}
	
	private class EndFlashTimer extends TimerTask {
		@Override
		public void run() {
			showingFlashText = false;		
			
		}
	}

}
