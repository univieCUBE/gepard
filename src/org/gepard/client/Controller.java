// TODO [later] remove hardcoded N codes
// TODO [next] to suffixarrays created with one substitution matrix also work for others?
// next: test if dotplot of HCI is still fast on gepard, then generate data
// TODO "troubleshooting" or something in tutorial

package org.gepard.client;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.gepard.client.userinterface.AboutDialog;
import org.gepard.client.userinterface.ContainerWindow;
import org.gepard.client.userinterface.ControlPanel;
import org.gepard.client.userinterface.DrawPanel;
import org.gepard.client.userinterface.InfoPanel;
import org.gepard.client.userinterface.MainPanel;
import org.gepard.client.userinterface.StatusDialog;
import org.gepard.client.userinterface.UserMessageDialog;
import org.gepard.client.userinterface.WindowPos;
import org.gepard.common.AbortionChecker;
import org.gepard.common.DotMatrix;
import org.gepard.common.FASTAReader;
import org.gepard.common.FASTAWriter;
import org.gepard.common.InvalidFASTAFileException;
import org.gepard.common.ParameterSet;
import org.gepard.common.Sequence;
import org.gepard.common.SubstitutionMatrix;
import org.gepard.common.SuffixArray;
import org.gepard.common.VmatchConverter;

public class Controller implements AbortionChecker {

	// GUI objects
	private ContainerWindow container; // container object
	private MainPanel mp; // main GUI panel
	private DrawPanel dp; // GUI draw panel
	private ControlPanel cp; // GUI control panel
	private InfoPanel ip; // GUI information panel
	private StatusDialog stat; // calculation status dialog

	private DotplotInfo dpInfo; // data about the current plot
	private Plotter p; // current Plotter
	private GeneNames genenames;

	// crosshair & selection data
	private int crossX = -1, crossY = -1; // crosshair pixel position
	private int crossPosX, crossPosY; // crosshair real position
	private int selStartX = -1, selStartY = -1; // selection start data
	private boolean isDragging = false;
	private boolean ctrlpressed;
	private int mousex;
	private int mousey;

	// sticking
	private boolean sticking = false;
	private boolean reversestick = false;

	private boolean dotplotExists = false;
	private boolean dotplotAborted = false;

	// alignment display data
	private byte[] curAlignData1 = null, curAlignData2 = null;
	private int dataOffset1, dataOffset2;

	public Controller() {

		// catch everything
		try {

			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

			// check if vmatch's mkvtree is available
			try {
				Runtime.getRuntime().exec("mkvtree");
				ClientGlobals.useVmatch = true;
				ClientGlobals.vmatchCommand = "mkvtree";
			} catch (Exception e) {
				// now try mkvtree.exe
				try {
					Runtime.getRuntime().exec("mkvtree.exe");
					ClientGlobals.useVmatch = true;
					ClientGlobals.vmatchCommand = "mkvtree.exe";
				} catch (Exception e2) {

					ClientGlobals.useVmatch = false;

				}
			}

			// ensure the settings directory exists
			new File(ClientGlobals.SETTINGS_DIR).mkdirs();

			// set config file
			try {
				Config.setConfigFile(ClientGlobals.SETTINGS_DIR + System.getProperty("file.separator") + ClientGlobals.CONFIG_FILE);
			} catch (Exception e) {
				// default config will be used

				// JOptionPane.showMessageDialog(null, "Could not load
				// configuration file. Standard values will be used.\n\nError
				// message:\n" + e,"Error", JOptionPane.WARNING_MESSAGE);
			}

			// load proxy settings
			updateProxySettings(true);

			// create GUI objects
			container = new ContainerWindow();
			dp = new DrawPanel(this);
			ip = new InfoPanel(this);
			cp = new ControlPanel(this);
			mp = new MainPanel(cp, dp, ip);
			// set program icon
			container.setIconImage(Toolkit.getDefaultToolkit().getImage(container.getClass().getResource(ClientGlobals.RES_APPICON)));

			container.setup(this, mp);
			// set title
			container.setTitle(ClientGlobals.APPNAME);

			// show user message (if there is one)
			if (Config.getInstance().getIntVal("userhelloshown", 0) == 0) {
				UserMessageDialog.showUserMessageIfExisting(ClientGlobals.FILE_USERMSG);
				Config.getInstance().setIntVal("userhelloshown", 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			ClientGlobals.unexpectedError(e, this);
		}

	}

	public void zoomFullPlot() {

		// set coordinates in current parameter set
		dpInfo.params.seq1Start = 0;
		dpInfo.params.seq1Stop = dpInfo.seq1len - 1;
		dpInfo.params.seq2Start = 0;
		dpInfo.params.seq2Stop = dpInfo.seq2len - 1;
		// create dotplot
		initiateLocalDotplot(dpInfo.submat, dpInfo.seqFile1, dpInfo.seqFile2, dpInfo.params, dpInfo.compseq);

	}

	public void zoom(float factor) {

		int add;

		// get new parameter set from GUI
		try {
			dpInfo.params = cp.getParameterSet(false);
		} catch (InvalidParamSetException e) {
		}

		// calculate new x coordinates
		int oldRange1 = (dpInfo.params.seq1Stop - dpInfo.params.seq1Start);
		int newRange1 = (int) ((dpInfo.params.seq1Stop - dpInfo.params.seq1Start + 1) * factor);
		int newStart1 = dpInfo.params.seq1Start - ((newRange1 - oldRange1) / 2);

		add = 0;
		if (newStart1 < 0) {
			add = -newStart1;
			newStart1 = 0;
		}

		int newStop1 = dpInfo.params.seq1Stop + ((newRange1 - oldRange1) / 2) + add;
		if (newStop1 >= dpInfo.seq1len)
			newStop1 = dpInfo.seq1len - 1;

		// calculate new y coordinates
		int oldRange2 = (dpInfo.params.seq2Stop - dpInfo.params.seq2Start);
		int newRange2 = (int) ((dpInfo.params.seq2Stop - dpInfo.params.seq2Start + 1) * factor);
		int newStart2 = dpInfo.params.seq2Start - ((newRange2 - oldRange2) / 2);
		add = 0;
		if (newStart2 < 0) {
			add = -newStart2;
			newStart2 = 0;
		}
		int newStop2 = dpInfo.params.seq2Stop + ((newRange2 - oldRange2) / 2) + add;
		if (newStop2 >= dpInfo.seq2len)
			newStop2 = dpInfo.seq2len - 1;
		// set coordinates
		dpInfo.params.seq1Start = newStart1;
		dpInfo.params.seq1Stop = newStop1;
		dpInfo.params.seq2Start = newStart2;
		dpInfo.params.seq2Stop = newStop2;

		// create dotplot
		initiateLocalDotplot(dpInfo.submat, dpInfo.seqFile1, dpInfo.seqFile2, dpInfo.params, dpInfo.compseq);

	}

	public boolean dotplotExists() {
		return dotplotExists;
	}

	public void abortDotplot() {
		// set flag
		dotplotAborted = true;
	}

	public boolean dotplotAborted() {
		// return flag
		return dotplotAborted;
	}

	public boolean checkDotplotAborted() {
		if (dotplotAborted) {
			// display message
			JOptionPane.showMessageDialog(null, "Dotplot computation aborted.", "Aborted", JOptionPane.INFORMATION_MESSAGE);
			// erase everything
			erasePlot();
			// set flag
			dotplotExists = false;
			dotplotAborted = false;
			// clean up
			plotFailedCleanup();
			// reset draw panel
			dp.setPreferredSize(new Dimension(1, 1));
			dp.repaint();
			dp.invalidate();
			container.validate();
			// set files changed flag to cause reloading in the next dp
			cp.setNeedReload(true);

			return true;
		} else
			return false;

	}

	public void initiateLocalDotplot(final SubstitutionMatrix submat, final String seqfile1, final String seqfile2, final ParameterSet params,
			final int compseq) {

		// create status window
		stat = new StatusDialog(this);
		// get containers position and center dialog
		WindowPos cpos = container.getPosition();
		stat.setLocation(cpos.x + (cpos.width / 2) - (stat.getWidth() / 2), cpos.y + (cpos.height / 2) - (stat.getHeight() / 2));

		final Controller ctrl = this;

		// create worker thread and start computing the dotplot
		Thread worker = new Thread() {
			public void run() {
				// catch all errors which are not caught yet
				try {
					doLocalDotplot(submat, seqfile1, seqfile2, params, compseq);
				} catch (Exception e) {
					e.printStackTrace();
					ClientGlobals.unexpectedError(e, ctrl);
				}
			}
		};
		worker.start();

	}

	public void doLocalDotplot(SubstitutionMatrix submat, String seqfile1, String seqfile2, ParameterSet params, int compseq) {

		// setup GUI for working conditions
		cp.dotplotStartCalc();
		((Component) container).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		SuffixArray sa = null;
		boolean SAforSecondSeq = false;
		Sequence sequence1 = null, sequence2 = null;

		// check if input sequences have changed
		if (!cp.needReload() && dpInfo != null) {
			// erase old alignment
			ip.removeAlignment();
			// we can keep the suffix array
			sa = dpInfo.suffixArray;
			SAforSecondSeq = dpInfo.SAforSecondSeq;
			// sequence data are also kept
			sequence1 = dpInfo.sequence1;
			sequence2 = dpInfo.sequence2;
		} else {
			// delete old plot (if existing)
			erasePlot();
			// load sequence files
			stat.setStatusText("Reading first sequence file...");
			sequence1 = readSequenceFile(seqfile1, 1, submat);
			if (sequence1 == null) { // abort
				// close status dialog
				stat.close();
				stat = null;
				// reset GUI
				plotFailedCleanup();
				return;
			}

			// show warning message if there were invalid characters
			if (sequence1.hasInvalidChars())
				JOptionPane.showMessageDialog(null,
						"Sequence 1 contains characters which to do not belong to the alphabet defined by the substitution matrix.\n\nA suffix array cannot be saved to or loaded from a file.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			// show warning message if this is an AA sequence and
			// 'complementary' was checked
			if (!sequence1.likelyNucleotides() && compseq == 1) {
				JOptionPane.showMessageDialog(null, "Sequence 1 seems to be an amino acid sequence.\nNo complementary sequence can be calculated.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				compseq = 0;
				cp.uncheckFirstComp();
			}

			if (sequence1.likelyNucleotides() && compseq == 1) {
				// complementarize sequence
				stat.setStatusText("Calculating complementary sequence...");
				Sequence.complementarizeSequence(sequence1);
			}

			// now if the second filename is equal to the first one AND none of
			// them is complementarized
			// -> use same object
			if (seqfile1.equals(seqfile2) && compseq == 0)
				sequence2 = sequence1;
			else {
				// load second sequence
				stat.setStatusText("Reading first sequence file...");
				sequence2 = readSequenceFile(seqfile2, 2, submat);
				if (sequence2 == null) { // abort
					// close status dialog
					stat.close();
					stat = null;
					// reset GUI
					plotFailedCleanup();
					return;
				}
				// show warning message if there were invalid characters
				if (sequence2.hasInvalidChars())
					JOptionPane.showMessageDialog(null,
							"Sequence 2 contains characters which to do not belong to the alphabet defined by the substitution matrix.\n\nA suffix array cannot be saved to or loaded from a file.",
							"Warning", JOptionPane.WARNING_MESSAGE);
				// show warning message if this is an AA sequence and
				// 'complementary' was checked
				if (!sequence1.likelyNucleotides() && compseq == 2) {
					JOptionPane.showMessageDialog(null, "Sequence 2 seems to be an amino acid sequence.\nNo complementary sequence can be calculated.",
							"Warning", JOptionPane.WARNING_MESSAGE);
					compseq = 0;
					cp.uncheckSecondComp();
				}

				if (sequence2.likelyNucleotides() && compseq == 2) {
					// complementarize sequence
					stat.setStatusText("Calculating complementary sequence...");
					Sequence.complementarizeSequence(sequence2);
				}
			}

		}

		// sequences are loaded, we need to check if the stop parameters
		// are out of range
		if (sequence1.getLength() <= params.seq1Stop) {
			JOptionPane.showMessageDialog(null, "Stop parameter for sequence 1 is higher than sequence length.", "Warning", JOptionPane.WARNING_MESSAGE);
			// reset GUI
			plotFailedCleanup();
			return;
		}
		if (sequence2.getLength() <= params.seq2Stop) {
			JOptionPane.showMessageDialog(null, "Stop parameter for sequence 2 is higher than sequence length.", "Warning", JOptionPane.WARNING_MESSAGE);
			// reset GUI
			plotFailedCleanup();
			return;
		}

		// check if user aborted!
		if (checkDotplotAborted())
			return;

		// set real end coordiates if 0
		if (params.seq1Stop == 0)
			params.seq1Stop = sequence1.getLength() - 1;
		if (params.seq2Stop == 0)
			params.seq2Stop = sequence2.getLength() - 1;

		// if auto zoom -> set zoom now
		if (cp.useAutoZoom() || (params.zoom == 0)) {
			params.zoom = getAutoZoom(params.seq1Start, params.seq1Stop, params.seq2Start, params.seq2Stop);

			if (params.zoom == -1) {
				errPlotTooNarrow();
				// reset GUI
				plotFailedCleanup();
				return;
			}
		}

		// check if current zoom would cause on side of the plot = 0 length
		if ((params.seq1Stop - params.seq1Start + 1) / params.zoom == 0) {
			JOptionPane.showMessageDialog(null,
					"Plot is too narrow. Please alter start/stop parameters for sequence 1 to " + "enlarge the range on this sequence.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			// reset GUI
			plotFailedCleanup();
			return;
		}
		if ((params.seq2Stop - params.seq2Start + 1) / params.zoom == 0) {
			JOptionPane.showMessageDialog(null,
					"Plot is too narrow. Please alter start/stop parameters for sequence 2 to " + "enlarge the range on this sequence.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			// reset GUI
			plotFailedCleanup();
			return;
		}

		// if auto params -> set params now

		if (cp.useAutoParameters()) {
			AutoParameters.setAutoParameters(params);
		}

		// check for critical parameters
		if (AutoParameters.areCriticalParameters(params)) {
			if (JOptionPane.showConfirmDialog(null,
					"The plot parameters you set might result in a long computation time."
							+ "You should increase the wordlength or disable window mode.\nProceed?",
					"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
				// reset GUI
				plotFailedCleanup();
				return;
			}
		}

		// update paramters in GUI
		cp.setParameterSet(params);

		boolean noSAfileOverwrite = false;

		// create suffix array (if necessary)
		if (sa == null) {
			boolean calcSA = true;

			// set sequence array and filename for correct sequence
			// determine by complementary setting or sequence length otherwise
			Sequence SAseq;
			String seqFileForSA;
			if (compseq > 0) {
				if (compseq == 1)
					SAforSecondSeq = true;
				else
					SAforSecondSeq = false;
			} else {
				if (sequence1.getLength() >= sequence2.getLength())
					SAforSecondSeq = false;
				else
					SAforSecondSeq = true;
			}
			if (!SAforSecondSeq) {
				SAseq = sequence1;
				seqFileForSA = seqfile1;
			} else {
				SAseq = sequence2;
				seqFileForSA = seqfile2;
			}

			// construct possible suffix array file names
			String seqdirsafile = seqFileForSA + ".sa";
			String settingssafile = ClientGlobals.SETTINGS_DIR + SuffixArray.getSAFilename(seqFileForSA);

			// try loading SA from file?
			if ((!SAseq.hasInvalidChars())) {
				calcSA = false;

				// check if any of the possible SA files exists (in settings
				// folder or in seq directory)
				String safile;
				if (new File(seqdirsafile).exists())
					safile = seqdirsafile;
				else if (new File(settingssafile).exists())
					safile = settingssafile;
				else
					safile = null;

				// check if file exists
				if (safile != null) {
					// try loading suffix arrays
					try {
						stat.setStatusText("Loading suffix array from file...");
						sa = SuffixArray.loadFromFile(new File(safile), SAseq.getSequenceData());
					} catch (Exception ex) {
						// display warning message
						if (JOptionPane.showConfirmDialog(null,
								"The file '" + safile + "' could not be recognized as a valid suffix array file for this sequence.\n\nDelete file now?",
								"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
							new File(safile).delete();
						else
							// if the user does not want the SA file to be
							// deleted
							// it may also not be overwritten
							noSAfileOverwrite = true;
						// we need to calculate the SA !
						calcSA = true;

					}
				} else
					// we need to calculate the SA !
					calcSA = true;
			}

			if (calcSA) {

				// can we try it with vmatch?
				if (ClientGlobals.useVmatch && SAseq.getLength() >= ClientGlobals.MINVMATCHLENGTH && SAseq.likelyNucleotides()) {

					try {
						if (cp.saveSuffixArrays()) {
							// saving is enabled -> directly save the SA to the
							// user directory
							stat.setStatusText("Vmatch calculates suffix array...");
							VmatchConverter.genSAFileFromVmatch(seqFileForSA, SAseq.getLength(), new File(settingssafile), ClientGlobals.vmatchCommand);
							// then load it
							stat.setStatusText("Loading array from file...");
							sa = SuffixArray.loadFromFile(new File(settingssafile), SAseq.getSequenceData());
						} else {
							// saving is disabled -> save to temporary file
							File tempfile = File.createTempFile("gepard", "tmpfile");
							stat.setStatusText("Vmatch calculates suffix array...");
							VmatchConverter.genSAFileFromVmatch(seqFileForSA, SAseq.getLength(), tempfile, ClientGlobals.vmatchCommand);
							// load it
							stat.setStatusText("Loading array from file...");
							sa = SuffixArray.loadFromFile(tempfile, SAseq.getSequenceData());
							// delete the temp file
							tempfile.delete();

						}
					} catch (Exception e) {

						e.printStackTrace();
						// if any exception occured -> vmatch call failed, calc
						// suffix array manually
						ClientGlobals.useVmatch = false;

					}
				}

				if (sa == null) {

					stat.setStatusText("Calculating suffix array...");
					try {
						sa = new SuffixArray(SAseq, submat.getAlphabetSize(), this);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
						ClientGlobals.errMessage("Out of memory!\n" + "Try launching Gepard with a higher amount of available memory.");
						plotFailedCleanup();
						return;
					}
					// save suffix array to file if set by user
					if (!dotplotAborted && cp.saveSuffixArrays() && (!SAseq.hasInvalidChars()) && !noSAfileOverwrite) {
						try {
							stat.setStatusText("Writing suffix array to file...");
							sa.saveToFile(settingssafile);
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, "Failed saving suffix array to file.\n\nError message:\n" + ioe, "Error",
									JOptionPane.WARNING_MESSAGE);
						}
					}
				}

			}
		}

		// suppose garbage collection
		System.gc();

		// check if user aborted!
		if (checkDotplotAborted())
			return;

		dotplotExists = false;

		stat.setStatusText("Calculating dot matrix...");

		// create dot matrix
		DotMatrix dm = new DotMatrix(sequence1.getSequenceData(), sequence2.getSequenceData(), sequence1.getName(), sequence2.getName(), sa, params, submat,
				stat, this, SAforSecondSeq);

		// check if user aborted!
		if (checkDotplotAborted())
			return;
		stat.setStatusText("Preparing dotplot...");

		// create the current info object
		dpInfo = new DotplotInfo(sequence1.getLength(), sequence2.getLength(), dm.getWidth(), dm.getHeight(), sa, SAforSecondSeq, params, submat, true, compseq,
				seqfile1, seqfile2);

		// keep sequence data in mem
		dpInfo.sequence1 = sequence1;
		dpInfo.sequence2 = sequence2;

		showDotplot(dm);
		// reset GUI
		((Component) container).setCursor(Cursor.getDefaultCursor());
		// close status dialog
		stat.close();
		stat = null;

		// tell GUI that dotplot is ready
		cp.dotplotReady();
		cp.setNucleotidePlot(dm.isNucleotideMatrix());

		// update SA file size value in GUI
		cp.updateSADiskSpace();

		// suppose garbage collection
		System.gc();

	}

	private void showDotplot(DotMatrix dm) {

		// create plotter object
		p = new Plotter(dm, dp);
		// set plotter in draw panel
		dp.setPlotter(p);
		// setup scrollbars in control panel
		cp.setupScrollbars(dm.getMinDotScore(), dm.getMaxDotScore());

		// set initial scrollbar values
		if (dpInfo.params.wordLength > 0)
			cp.setScrollbars(0, 20, 0, 20);
		else {
			// calculate good display value
			float gooddisp = (dm.getAvgDotScore() + dm.getMaxDotScore()) * 0.40f;
			// convert to 0 to 100 value
			int percent = (int) ((gooddisp - dm.getMinDotScore()) / (dm.getMaxDotScore() - dm.getMinDotScore()) * 100f);
			cp.setScrollbars(percent, 100, 50, 20);
		}

		// show display tab
		cp.showDisplayTab();
		// set files changed flag
		cp.setNeedReload(false);
		// set title of container window (if possible)
		container.setTitle(ClientGlobals.cutString(dm.getSeq1Name(), ClientGlobals.MAXNAMELEN_TITLE) + "  vs.  "
				+ ClientGlobals.cutString(dm.getSeq2Name(), ClientGlobals.MAXNAMELEN_TITLE) + "  -  " + ClientGlobals.APPNAME);

		// set flag
		dotplotExists = true;

		// set draw panel dimensions
		/// // dp.setPreferredSize(p.getPlotDimensions());
		// invalidate draw panel and validate container (needed if drawpanel
		/// changed size)
		dp.invalidate();
		container.validate();
		// call resize event of info panel
		ip.componentResized(null);

		// set button caption in controlpanel
		cp.setGoButtonCaption(true);

	}

	public void plotFailedCleanup() {
		cp.dotplotFailed();
		((Component) container).setCursor(Cursor.getDefaultCursor());
		if (stat != null)
			stat.close();
	}

	public void showAlignments(boolean show) {
		// forward information to info panel
		ip.showAlignments(show);
	}

	private Sequence readSequenceFile(String file, int num, SubstitutionMatrix submat) {
		// load first sequence
		Sequence seq = null;

		try {
			seq = FASTAReader.readFile(file, submat);
		} catch (IOException e) {
			JOptionPane.showMessageDialog((Component) container, "Failed loading sequence file " + num + "\nIO error: " + e.getMessage(),
					"Failed loading sequence file " + num, JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (InvalidFASTAFileException e) {
			JOptionPane.showMessageDialog((Component) container, "Failed loading sequence file " + num + "\nInvalid FASTA file: " + e.getMessage(),
					"Failed loading sequence file " + num, JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// check if predicted sequence type (AA or nucl)
		// matches matrix type
		if (seq.likelyNucleotides() && !submat.isNucleotideMatrix()) {
			int ret = JOptionPane.showConfirmDialog((Component) container,
					"You are using an amino acid (protein) substitution matrix but your sequence appears to be a nucleotide sequence.\nProceed?",
					"Warning: Sequence " + num, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (ret == JOptionPane.NO_OPTION)
				return null;
		} else if (!seq.likelyNucleotides() && submat.isNucleotideMatrix()) {
			int ret = JOptionPane.showConfirmDialog((Component) container,
					"You are using a nucleotide substitution matrix but your sequence appears to be an amino acid (protein) sequence.\nProceed?",
					"Warning: Sequence " + num, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (ret == JOptionPane.NO_OPTION)
				return null;
		}

		return seq;

	}

	// erase plot, try to remove objects from memory
	private void erasePlot() {
		dp.setPlotter(null);
		p = null;
		dpInfo = null;
		// tell info panel to clean up
		ip.removeAlignment();
		// suggest garbage collection
		System.gc();
	}

	// called by ControlPanel when scrollbars were changed and a color
	// recalculation and redrawing of the plot is needed
	public void eventReplot(float lower, float upper, float greyscale) {
		p.reCalc(lower, upper, greyscale);
		dp.repaint();
	}

	public void eventExportImage(String file, String format) {
		try {
			// export image
			ImageIO.write(p.getFullImage(), format, new File(file));
			// show success message
			JOptionPane.showMessageDialog((Component) container, "Dotplot successfully exported", "Export", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Could not export image file.\n\nError message:\n" + e, "Error", JOptionPane.WARNING_MESSAGE);

		}
	}

	// called by DrawPanel while mouse is moving over the dotplot
	// shows current coordinates in InfoPanel
	public void eventMouseMove(int x, int y) {

		if (!dotplotExists)
			return;

		if (!dp.hasFocus()) {
			dp.requestFocus();
		}

		// store coordinates
		this.mousex = x;
		this.mousey = y;

		// transform coordinates
		int tx = transX(x);
		int ty = transY(y);

		// ctrl pressed?
		if (ctrlpressed) {
			// get in-plot coordinates
			int inplotx = x - Plotter.XOFF;
			if (inplotx < 0)
				inplotx = 0;
			if (inplotx >= dpInfo.dpWidth)
				inplotx = dpInfo.dpWidth - 1;
			// get in-plot coordinates
			int inploty = y - Plotter.YOFF;
			if (inploty < 0)
				inploty = 0;
			if (inploty >= dpInfo.dpHeight)
				inploty = dpInfo.dpHeight - 1;

			// get associated genes

			String gene1 = null, gene2 = null;

			if (genenames == null) {
				gene1 = "-";
				gene2 = "-";
			} else {
				genenames.getGeneName(true, inplotx);
				genenames.getGeneName(false, inploty);
			}

			// tell plotter to display them
			p.setGeneToolTip(x, y, gene1, gene2);
			dp.repaint();
		}

		// show coordinates in InfoPanel
		if (dpInfo.sequence1 != null && dpInfo.sequence2 != null)
			ip.setCoordinates(tx, ty, getMultiName(dpInfo.sequence1, tx), getMultiName(dpInfo.sequence2, ty));

	}

	/**
	 * Determine name of sequence in multi-fastas file based on the current
	 * sequence coordinate.
	 */
	private String getMultiName(Sequence seq, int pos) {
		if (!seq.isMulti())
			return null;
		else {
			int[] starts = seq.getMultiStarts();
			int i = 0;
			for (i = 0; i < starts.length; i++)
				if (pos <= starts[i])
					break;
			i--;
			if (i < 0)
				i = 0;

			String[] names = seq.getMultiNames();
			return names[i];
		}
	}

	public void validateContainer() {
		if (container != null)
			container.validate();
	}

	// called by DrawPanel when user clicked into the plot
	public void eventMouseClick(int x, int y, int button) {

		if (!dotplotExists)
			return;

		int tX = transX(x), tY = transY(y);

		// determine action to be performed
		int localClickAction = cp.getLocalClickAction();
		// local plot
		if (localClickAction == ClientGlobals.LOCALCLICK_SHOWALIGN) {

			sticking = false;
			reversestick = false;

			// set data
			curAlignData1 = dpInfo.sequence1.getSequenceData();
			curAlignData2 = dpInfo.sequence2.getSequenceData();
			dataOffset1 = 0;
			dataOffset2 = 0;

			crossX = x;
			crossY = y;
			// sticky click?
			if (button == MouseEvent.BUTTON1) {
				// normal click
				crossPosX = tX;
				crossPosY = tY;
			} else {

				if (dpInfo.submat.isNucleotideMatrix()) {
					// sticky!
					Pos diagonal = searchDiagonal(tX, tY);
					// found something?
					if (diagonal.x == -1) {
						ClientGlobals.infoMessage("No diagonal found in this area, showing normal aligment");
						crossPosX = tX;
						crossPosY = tY;
					} else {
						// stick to diagonal!
						stickToDiagonal(diagonal);
					}
				} else {
					ClientGlobals.warnMessage("No sticky click for protein sequence dotplots!");
					crossPosX = tX;
					crossPosY = tY;
				}

			}

			// show alignment
			ip.showAlignment(curAlignData1, curAlignData2, dataOffset1, dataOffset2, curAlignData1.length, curAlignData2.length, crossPosX, crossPosY,
					dpInfo.submat, cp.reverseComplementaryAlignments());

			// set pos in plotter
			p.setCrossHair(crossX, crossY, crossPosX, crossPosY);
			dp.repaint();

		} else if (localClickAction == ClientGlobals.LOCALCLICK_EXPORT) {
			// write window to fasta files

			// user message
			ClientGlobals.infoMessage("Exporting current sequence window to FASTA files:\n" + "Sequence 1: " + dpInfo.params.seq1Start + "-"
					+ dpInfo.params.seq1Stop + "\n" + "Sequence 2: " + dpInfo.params.seq2Start + "-" + dpInfo.params.seq2Stop);

			String lastdir = Config.getInstance().getStringVal("lastopendir_seqexport", "");
			// first file
			JFileChooser f = new JFileChooser(lastdir);
			String defFile = ClientGlobals.insertBeforeExtension(ClientGlobals.extractFilename(dpInfo.seqFile1),
					"_" + dpInfo.params.seq1Start + "-" + dpInfo.params.seq1Stop);
			f.setSelectedFile(new File(defFile));
			int retval = f.showSaveDialog(mp);
			if (retval == JFileChooser.APPROVE_OPTION) {
				try {
					FASTAWriter.writeFASTAFile(f.getSelectedFile().getAbsolutePath(), dpInfo.sequence1, dpInfo.params.seq1Start,
							dpInfo.params.seq1Stop - dpInfo.params.seq1Start + 1, dpInfo.submat);
					// second file
					defFile = ClientGlobals.insertBeforeExtension(ClientGlobals.extractFilename(dpInfo.seqFile2),
							"_" + dpInfo.params.seq2Start + "-" + dpInfo.params.seq2Stop);
					f.setSelectedFile(new File(defFile));
					retval = f.showSaveDialog(mp);
					if (retval == JFileChooser.APPROVE_OPTION) {
						FASTAWriter.writeFASTAFile(f.getSelectedFile().getAbsolutePath(), dpInfo.sequence2, dpInfo.params.seq2Start,
								dpInfo.params.seq2Stop - dpInfo.params.seq2Start + 1, dpInfo.submat);
						Config.getInstance().setVal("lastopendir_seqexport", f.getSelectedFile().getParent());
					}
				} catch (IOException e) {
					ClientGlobals.errMessage("Error while writing file:\n\n" + e.getMessage());
				}

			}
			;

			// System.out.println("export");

		} else {
			// do nothing

		}

	}

	private void stickToDiagonal(Pos diagonal) {
		crossPosX = diagonal.x;
		crossPosY = diagonal.y;
		crossX = ((crossPosX - dpInfo.params.seq1Start) / dpInfo.params.zoom) + Plotter.XOFF;
		crossY = ((crossPosY - dpInfo.params.seq2Start) / dpInfo.params.zoom) + Plotter.YOFF;
		cp.setRevComplAlign(diagonal.reverse);
		sticking = true;
		reversestick = diagonal.reverse;

	}

	public void updateAlignment() {
		ip.updateAlignment(cp.reverseComplementaryAlignments());
	}

	// called by DrawPanel when user moves mouse while button down
	public void eventMouseDrag(int x, int y) {

		if (!dotplotExists)
			return;

		// just started dragging?
		if (!isDragging) {
			selStartX = x;
			selStartY = y;
			isDragging = true;
			// remove crosshair
			p.removeCrossHair();
		}
		// transform & display in info panel
		ip.setSelection(transX(selStartX), transY(selStartY), transX(x), transY(y));
		// correct values of outside of dotplot and set current selection
		p.setSelection(selStartX, selStartY, x, y);
		dp.repaint();
	}

	// called by DrawPanel when user releases the mouse button
	public void eventMouseRelease(int x, int y) {

		if (!dotplotExists)
			return;

		// only use this code if the user was dragging
		if (isDragging) {
			// transform coordinates
			int tselStartX = transX(selStartX);
			int tselStartY = transY(selStartY);
			int tselStopX = transX(x);
			int tselStopY = transY(y);
			// only carry on if not start=stop for x or y
			if (tselStartX != tselStopX && tselStartY != tselStopY) {
				// determine new plot coordinates
				int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
				x1 = (tselStartX < tselStopX) ? tselStartX : tselStopX;
				x2 = (tselStartX > tselStopX) ? tselStartX : tselStopX;
				y1 = (tselStartY < tselStopY) ? tselStartY : tselStopY;
				y2 = (tselStartY > tselStopY) ? tselStartY : tselStopY;

				isDragging = false;
				// get copy of parameter set and change settings
				ParameterSet cpyparams = null;
				try {
					cpyparams = cp.getParameterSet(true).getClone();
				} catch (Exception e) {
				}
				// set zoom automatically?
				if (cp.useAutoZoom())
					cpyparams.zoom = getAutoZoom(x1, x2, y1, y2);
				if (cpyparams.zoom == 0)
					cpyparams.zoom = 1;
				cpyparams.seq1Start = x1;
				cpyparams.seq1Stop = x2;
				cpyparams.seq2Start = y1;
				cpyparams.seq2Stop = y2;
				// set parameters in GUI
				cp.setParameterSet(cpyparams);
				// show options tab
				cp.showOptionsTab();
			}
		}

	}

	public void eventExit() {
		// save configuration file

		try {
			Config.getInstance().storeConfig();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Could not save configuration file '" + Config.getConfigFileName() + "'.\n\nError:\n" + e, "Error",
					JOptionPane.WARNING_MESSAGE);
		}

		// show about dialog
		@SuppressWarnings("unused")
		AboutDialog about = new AboutDialog();

	}

	public void exit() {
		// cause the program to exit
		container.close();
	}

	private void errPlotTooNarrow() {
		ClientGlobals.warnMessage(
				"Using the current zoom level the dotplot would become too narrow (as one of the sequences is too short).\nPlease decrease the zoom level or use a smaller section of the longer sequence.");
	}

	// calculates the maximum possible zoom for a given set of coordinates
	private int getAutoZoom(int x1, int x2, int y1, int y2) {
		// get drawing area dimension

		Dimension dimdraw = mp.getRealDrawPanelDim();
		dimdraw.width -= (Plotter.XOFF + 15);
		dimdraw.height -= (Plotter.YOFF + 15);

		// if we are in "small plots mode": halve the values
		if (cp.smallPlots()) {
			dimdraw.width /= 2;
			dimdraw.height /= 2;
		}

		// calculate maximum possible x&y zoom
		int xzoom = (int) Math.ceil((float) (x2 - x1) / (float) dimdraw.width);
		int yzoom = (int) Math.ceil((float) (y2 - y1) / (float) dimdraw.height);

		// avoid zoom = 0
		if ((xzoom == 0) && (yzoom == 0))
			xzoom = 1;

		// use higher value
		int realzoom = (xzoom > yzoom) ? xzoom : yzoom;

		// avoid zooming so high that one of the sequences is zoomed to 0
		if (realzoom > x2 - x1 || realzoom > y2 - y1)
			return -1;
		else
			return realzoom;

	}

	// transform screen x coordinate to real dotplot coordinate
	private int transX(int x) {
		int val = (int) ((x - Plotter.XOFF) * dpInfo.params.zoom) + dpInfo.params.seq1Start;
		if (val < dpInfo.params.seq1Start)
			val = dpInfo.params.seq1Start;
		else if (val > dpInfo.params.seq1Stop)
			val = dpInfo.params.seq1Stop;
		return val;
	}

	// transform screen x coordinate to real dotplot coordinate
	private int transY(int y) {
		int val = (int) ((y - Plotter.YOFF) * dpInfo.params.zoom) + dpInfo.params.seq2Start;
		if (val < dpInfo.params.seq2Start)
			val = dpInfo.params.seq2Start;
		else if (val > dpInfo.params.seq2Stop)
			val = dpInfo.params.seq2Stop;

		return val;
	}

	// global keyboard event
	public boolean eventKeyPressed(KeyEvent evt) {

		// if this was CTRL -> store information
		if (evt.getKeyCode() == 17) {
			ctrlpressed = true;
			// repeat last mouse event
			eventMouseMove(mousex, mousey);
		}

		if (curAlignData1 == null)
			return false;

		boolean updateAlignment = true;
		switch (evt.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			crossPosX--;
			sticking = false;
			break;
		case KeyEvent.VK_RIGHT:
			crossPosX++;
			sticking = false;
			break;
		case KeyEvent.VK_UP:
			crossPosY--;
			sticking = false;
			break;
		case KeyEvent.VK_DOWN:
			crossPosY++;
			sticking = false;
			break;
		case KeyEvent.VK_A:
			crossPosX -= ClientGlobals.BIGKEYBOARDSTEP;
			sticking = false;
			break;
		case KeyEvent.VK_D:
			crossPosX += ClientGlobals.BIGKEYBOARDSTEP;
			sticking = false;
			break;
		case KeyEvent.VK_W:
			crossPosY -= ClientGlobals.BIGKEYBOARDSTEP;
			sticking = false;
			break;
		case KeyEvent.VK_S:
			crossPosY += ClientGlobals.BIGKEYBOARDSTEP;
			sticking = false;
			break;

		// STICKY MOVEMENT
		case KeyEvent.VK_G:
			crossPosX--;
			if (reversestick)
				crossPosY++;
			else
				crossPosY--;
			break;
		case KeyEvent.VK_H:
			crossPosX++;
			if (reversestick)
				crossPosY--;
			else
				crossPosY++;
			break;
		case KeyEvent.VK_J:
			crossPosX -= ClientGlobals.BIGKEYBOARDSTEP;
			if (reversestick)
				crossPosY += ClientGlobals.BIGKEYBOARDSTEP;
			else
				crossPosY -= ClientGlobals.BIGKEYBOARDSTEP;
			break;
		case KeyEvent.VK_K:
			crossPosX += ClientGlobals.BIGKEYBOARDSTEP;
			if (reversestick)
				crossPosY -= ClientGlobals.BIGKEYBOARDSTEP;
			else
				crossPosY += ClientGlobals.BIGKEYBOARDSTEP;
			break;

		default:
			updateAlignment = false;
		}
		if (updateAlignment) {
			// correct coordinates if necessary
			if (crossPosX < dpInfo.params.seq1Start)
				crossPosX = dpInfo.params.seq1Start;
			if (crossPosX > dpInfo.params.seq1Stop)
				crossPosX = dpInfo.params.seq1Stop;
			if (crossPosY < dpInfo.params.seq2Start)
				crossPosY = dpInfo.params.seq2Start;
			if (crossPosY > dpInfo.params.seq2Stop)
				crossPosY = dpInfo.params.seq2Stop;
			// calculate new pixel coordinates
			crossX = ((crossPosX - dpInfo.params.seq1Start) / dpInfo.params.zoom) + Plotter.XOFF;
			crossY = ((crossPosY - dpInfo.params.seq2Start) / dpInfo.params.zoom) + Plotter.YOFF;
			// set alignment position and crosshair, then repaint
			if (sticking)
				cp.setRevComplAlign(reversestick);
			ip.showAlignment(curAlignData1, curAlignData2, dataOffset1, dataOffset2, curAlignData1.length, curAlignData2.length, crossPosX, crossPosY,
					dpInfo.submat, cp.reverseComplementaryAlignments());
			p.setCrossHair(crossX, crossY, crossPosX, crossPosY);
			p.removeSelection();
			dp.repaint();
		}

		return false;

	}

	public boolean eventKeyReleased(KeyEvent evt) {
		// if this was CTRL -> store information
		if (evt.getKeyCode() == 17) {
			ctrlpressed = false;
			// tell plotter to stop displaying gene name tooltips
			p.noToolTips();
			dp.repaint();
		}

		return false;
	}

	public void responseDotplot(DotMatrix dm, GeneNames genenames) {

		// update the seq lengths in the dotplot info object
		// (the uploaded sequence length is 0 up to now)
		dpInfo.seq1len = dm.getSeq1Length();
		dpInfo.seq2len = dm.getSeq2Length();

		// show plot
		showDotplot(dm);

		// store genenames list
		this.genenames = genenames;

		// reset GUI
		((Component) container).setCursor(Cursor.getDefaultCursor());
		// close status dialog
		if (stat != null) {
			stat.close();
			stat = null;
		}
		// tell GUI that dotplot is ready
		cp.dotplotReady();
		cp.setNucleotidePlot(true);

	}

	public void updateProxySettings(boolean startup) {
		// read proxy settings from current config and set system-wide values
		String host = Config.getInstance().getStringVal("proxy_host", "");
		String port = Config.getInstance().getStringVal("proxy_port", "");

		Properties sysProperties = System.getProperties();

		if (host.trim().equals("")) {
			// don't use a proxy
			sysProperties.put("http.proxyHost", "");
			sysProperties.put("http.proxyPort", "");
			sysProperties.put("http.proxySet", "false");
		} else {
			// use a HTTP proxy!
			sysProperties.put("http.proxyHost", host);
			sysProperties.put("http.proxyPort", port);
			sysProperties.put("http.proxySet", "true");
		}

		// tell the user he needs to restart the program
		if (!startup) {
			if (JOptionPane.showConfirmDialog(container, "You have to restart Gepard in order to apply the proxy settings.\n\n" + "Quit program now?",
					"Proxy settings", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				eventExit();
			}
		}
	}

	public DotplotInfo getDPInfo() {
		return dpInfo;
	}

	public ContainerWindow getContainer() {
		return container;
	}

	public String getGUIDump() {
		return cp.getGUIDump();
	}

	private Pos searchDiagonal(int x, int y) {

		final int SCANOFZOOM = 10;
		final int STICKY_RANGE = dpInfo.params.zoom * SCANOFZOOM;
		final int STICKY_WINDOW = 40 / 2;
		final int STICKY_THRESH = (int) (((float) STICKY_WINDOW * 2f) * 0.7f);

		byte[] complmap = Sequence.getComplementaryMap();

		int maxmatches = 0, maxx = 0, maxy = 0;
		boolean reverse = false;

		final int forward = STICKY_RANGE / 2;

		ParameterSet params = dpInfo.params;

		// initial coordinates
		boolean dox = true;
		int checkx = x - forward;
		int checky = y + forward;

		// search for forward diagonals
		while (checkx <= x + forward) {

			if (checkx >= params.seq1Start + STICKY_WINDOW && checky >= params.seq2Start + STICKY_WINDOW && checkx <= params.seq1Stop - STICKY_WINDOW
					&& checky <= params.seq2Stop - STICKY_WINDOW) {

				// slide over window
				int matches = 0;
				for (int j = -STICKY_WINDOW; j <= STICKY_WINDOW; j++) {
					if (curAlignData1[checkx + j - dataOffset1] == curAlignData2[checky + j - dataOffset2])
						matches++;
				}
				// new max?
				if (matches > maxmatches) {
					maxmatches = matches;
					maxx = checkx;
					maxy = checky;
					reverse = false;
				}
			}

			// step
			if (dox)
				checkx++;
			else
				checky--;
			dox = !dox;
		}

		// initial coordinates
		dox = true;
		checkx = x + forward;
		checky = y + forward;

		// search for reverse diagonals
		while (checkx >= x - forward) {

			if (checkx >= params.seq1Start + STICKY_WINDOW && checky >= params.seq2Start + STICKY_WINDOW && checkx <= params.seq1Stop - STICKY_WINDOW
					&& checky <= params.seq2Stop - STICKY_WINDOW) {

				// slide over window
				int matches = 0;
				for (int j = -STICKY_WINDOW; j <= STICKY_WINDOW; j++) {
					if (curAlignData1[checkx - j - dataOffset1] == complmap[curAlignData2[checky + j - dataOffset2]])
						matches++;
				}
				// new max?
				if (matches > maxmatches) {
					maxmatches = matches;
					maxx = checkx;
					maxy = checky;
					reverse = true;
				}

			}

			// step
			if (dox)
				checkx--;
			else
				checky--;
			dox = !dox;
		}

		if (maxmatches >= STICKY_THRESH)
			return new Pos(maxx, maxy, reverse);
		else
			return new Pos(-1, -1, true);

	}

	private class Pos {
		public int x;
		public int y;
		boolean reverse;

		public Pos(int x, int y, boolean reverse) {
			this.x = x;
			this.y = y;
			this.reverse = reverse;
		}
	}

}