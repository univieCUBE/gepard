
package org.gepard.client.userinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gepard.client.ClientGlobals;
import org.gepard.client.Config;
import org.gepard.client.Controller;
import org.gepard.client.InvalidParamSetException;
import org.gepard.client.SubstMatrixFile;
import org.gepard.client.SubstMatrixList;
import org.gepard.common.FASTAReader;
import org.gepard.common.InvalidFASTAFileException;
import org.gepard.common.InvalidSubMatFileException;
import org.gepard.common.ParameterSet;
import org.gepard.common.SubstitutionMatrix;

public class ControlPanel extends JPanel implements AdjustmentListener, ActionListener, ChangeListener {

	private static final long serialVersionUID = 2211444361988984615L;

	private static NumberFormat INTFMT = new DecimalFormat("###,###,###,###,##0", new DecimalFormatSymbols(Locale.ENGLISH));

	// main constants
	private static final int HOR_MARGIN = 5;
	private static final int CMB_HEIGHT = 18;

	private static final int SEQ_HEIGHT = 218;
	private static final int FUNCT_HEIGHT = 132;
	private static final int OPT_HEIGHT = 285;

	// sequence tab constants
	private static final int SEQ_TAB_SPACE_TOP = 3;
	private static final int SEQ_TAB_SPACE_BOTTOM = 3;
	private static final int SEQ_TAB_BETWEEN_SEQS = 10;
	private static final int SEQ_TAB_INITIAL_TOP = 5;
	private static final int SEQ_TAB_BUTTON_HEIGHT = 20;
	// function panel
	private static final int FUNCT_BETWEEN_ZOOM = 5;
	private static final int FUNCT_MARGIN_ZOOM = 10;
	private static final int FUNCT_GOADV_HOR_MARGIN = 40;
	private static final int FUNCT_ABOVE_GO = 8;
	private static final int FUNCT_BELOW_GO = 12;
	private static final int FUNCT_ABOVE_ADV = 20;

	// plotopt panel
	private static final int PLOTOPT_VERTSPACE = 2;
	private static final int PLOTOPT_EXTRAVERT = 5;
	private static final int PLOTOPT_BEGIN_VERTSPACE = 5;
	private static final int PLOTOPT_ITEMS_INDENT = 6;
	// misc tab
	private static final int MISC_VERTSPACE = 2;
	private static final int MISC_EXTRAVERT = 6;
	private static final int MISC_INITVERT = 3;
	// disp tab
	private static final int DISP_VERT_SMALL = 4;
	private static final int DISP_VERT_BIG = 8;
	private static final int DISP_ABOVE_EXPORT = 5;
	// quit button
	private static final int QUIT_BTN_HEIGHT = 27;
	private static final int BELOW_QUIT_BTN = 5;
	private static final int QUIT_BTN_HOR_MARGIN = 40;

	// font(s)
	private static final Font MAIN_FONT = new Font("Dialog", Font.BOLD, 11);
	private static final Font CAPT_FONT = new Font("Dialog", Font.BOLD, 12);

	private static final Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 11);

	// CONTROLS
	// tabs
	private JTabbedPane seqTabs;
	private JTabbedPane optTabs;
	// "Local" tab
	private JButton btnLocalSeq1, btnLocalSeq2;
	private JTextField txtLocalSeq1, txtLocalSeq2;
	private JCheckBox chkFirstComp, chkSecondComp;
	// plot options tab
	private JCheckBox chkAutoZoom, chkSmallPlots, chkAutoParams, chkAutoMatrix;
	private JTextField txtZoom, txtWordlen, txtWinsize;
	private CustomComboBox cmbMatrices;
	// ... coordinates
	private JTextField txtStart1, txtStop1, txtStart2, txtStop2;
	// misc tab
	private JCheckBox chkSaveSA;
	private JLabel lblSASpace;
	private JButton btnDeleteSAs;
	private JRadioButton optLocalShowAlign, optLocalExport, optLocalDoNothing;
	private JButton btnSetupProxy;
	// display tab
	private JPanel dispTab;
	private JScrollBar scrLower, scrUpper, scrGreyscale;// , scrNuclDistr;
	private JCheckBox chkRevCompAlign;
	// global stuff
	private JButton btnGo;
	private JButton btnAdvanced;
	private JButton btnFullPlot;
	private JButton btnZoomOut;
	private JButton btnExportFile;
	private JButton btnZoomIn;
	private JButton btnQuit;

	private String curSubmatName;

	// current mode (simple or advanced)
	private boolean isAdvancedMode = false;

	// substitution matrix list
	private SubstMatrixFile[] substMatrices;
	// custom subst matrix
	private SubstitutionMatrix customMatrix = null;
	// sequence files changed?
	private boolean needreload;

	private boolean noScrollbarEvents = false;

	// controller object reference
	Controller ctrl;

	public ControlPanel(Controller ictrl) {

		// store controller
		ctrl = ictrl;

		// load substitution matrices from XML file
		try {
			substMatrices = SubstMatrixList.getInstance().getMatrixFiles();
		} catch (Exception e) {
			ClientGlobals.errMessage("Could not open substitution matrix list. " + "The 'matrices/' subfolder seems to be corrupted");
			System.exit(1);
		}

		// sequences panel
		JPanel localTab = generateLocalTab();

		seqTabs = new JTabbedPane();
		// add sequence panes
		seqTabs.addTab("Sequences", localTab);
		seqTabs.addChangeListener(this);

		// function panel
		JPanel functPanel = generateFunctionPanel();

		// options panel
		JPanel plotOptTab = generatePlotOptTab();
		JPanel miscTab = generateMiscTab();
		dispTab = generateDispTab();

		optTabs = new JTabbedPane();
		optTabs.setFont(MAIN_FONT);
		// optTabs.setBorder(BorderFactory.createEmptyBorder());
		optTabs.addTab("Plot", plotOptTab);
		optTabs.addTab("Misc", miscTab);
		optTabs.addTab("Display", dispTab);
		optTabs.setSelectedIndex(0);

		// create layout
		seqTabs.setAlignmentX(Component.LEFT_ALIGNMENT);
		optTabs.setAlignmentX(Component.LEFT_ALIGNMENT);
		btnGo.setAlignmentX(Component.LEFT_ALIGNMENT);
		functPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel fixedBox = new JPanel();
		fixedBox.setLayout(new BoxLayout(fixedBox, BoxLayout.Y_AXIS));
		// fixedBox.setLayout(new GridBagLayout());
		fixedBox.add(Box.createRigidArea(new Dimension(0, 3)));
		fixedBox.add(seqTabs);
		fixedBox.add(Box.createRigidArea(new Dimension(0, 10)));
		fixedBox.add(functPanel);
		// fixedBox.add(Box.createRigidArea(new Dimension(0,10)));

		fixedBox.add(Box.createRigidArea(new Dimension(0, 10)));
		fixedBox.add(optTabs);

		seqTabs.setPreferredSize(new Dimension(1, SEQ_HEIGHT));
		functPanel.setPreferredSize(new Dimension(1, FUNCT_HEIGHT));
		optTabs.setPreferredSize(new Dimension(1, OPT_HEIGHT));

		// dummy panel which pushes the go button to the bottom of the window
		JPanel panelQuit = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1000;
		panelQuit.add(new JLabel(""), c);
		c.gridy++;
		c.weighty = 1;
		c.insets = new Insets(0, HOR_MARGIN + QUIT_BTN_HOR_MARGIN, BELOW_QUIT_BTN, HOR_MARGIN + QUIT_BTN_HOR_MARGIN);
		panelQuit.add(btnQuit = new JButton("Quit"), c);

		btnQuit.setPreferredSize(new Dimension(1, QUIT_BTN_HEIGHT));

		setLayout(new BorderLayout());
		add(fixedBox, BorderLayout.NORTH);
		add(panelQuit, BorderLayout.CENTER);

		btnQuit.addActionListener(this);

		// simple or advanced mode
		if (Config.getInstance().getIntVal("advanced", 0) == 1)
			switchMode(true);
		else
			switchMode(false);

		// help tooltips
		btnQuit.setToolTipText(HelpTexts.getInstance().getHelpText("quit"));

		// set initial parameters
		needreload = true;

		setDispTabEnabled(false);
		setNavigationEnabled(false);

	}

	public void setGoButtonCaption(boolean update) {
		if (update)
			btnGo.setText("Update dotplot");
		else
			btnGo.setText("Create dotplot");
	}

	private JPanel generateFunctionPanel() {

		HelpTexts hlp = HelpTexts.getInstance();

		JPanel functPane = new JPanel();
		functPane.setLayout(new GridBagLayout());

		// load icons
		ImageIcon icoZoomIn = new ImageIcon(this.getClass().getResource("/resources/images/zoomin.gif"));
		ImageIcon icoZoomOut = new ImageIcon(this.getClass().getResource("/resources/images/zoomout.gif"));
		ImageIcon icoShowFull = new ImageIcon(this.getClass().getResource("/resources/images/zoomfull.gif"));

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridwidth = 4;
		c.insets = new Insets(FUNCT_ABOVE_GO, FUNCT_GOADV_HOR_MARGIN, FUNCT_BELOW_GO, FUNCT_GOADV_HOR_MARGIN);
		functPane.add(btnGo = getCustomButton(""), c);

		c.gridwidth = 1;
		c.gridy++;
		c.insets = new Insets(0, FUNCT_BETWEEN_ZOOM + FUNCT_MARGIN_ZOOM, 0, FUNCT_BETWEEN_ZOOM);
		functPane.add(btnZoomIn = new JButton(icoZoomIn), c);
		c.gridx++;
		c.insets = new Insets(0, FUNCT_BETWEEN_ZOOM, 0, FUNCT_BETWEEN_ZOOM);
		functPane.add(btnZoomOut = new JButton(icoZoomOut), c);
		c.gridx++;
		functPane.add(btnFullPlot = new JButton(icoShowFull), c);

		// advanced mode button
		c.insets = new Insets(FUNCT_ABOVE_ADV, FUNCT_GOADV_HOR_MARGIN, 0, FUNCT_GOADV_HOR_MARGIN);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		functPane.add(btnAdvanced = getCustomButton(""), c);

		setGoButtonCaption(false);

		// insert stretcher
		c.gridy++;
		c.weighty = 10000;
		c.gridwidth = 1;
		functPane.add(new JLabel(), c);

		// event handlers
		btnGo.addActionListener(this);
		btnAdvanced.addActionListener(this);
		btnFullPlot.addActionListener(this);
		btnZoomIn.addActionListener(this);
		btnZoomOut.addActionListener(this);

		// set help texts
		btnGo.setToolTipText(hlp.getHelpText("createplot"));
		btnZoomIn.setToolTipText(hlp.getHelpText("zoomin"));
		btnZoomOut.setToolTipText(hlp.getHelpText("zoomout"));
		btnFullPlot.setToolTipText(hlp.getHelpText("fullplot"));
		btnAdvanced.setToolTipText(hlp.getHelpText("advmode"));

		return functPane;
	}

	private JPanel generateLocalTab() {
		HelpTexts hlp = HelpTexts.getInstance();

		JPanel localPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(SEQ_TAB_SPACE_TOP + SEQ_TAB_INITIAL_TOP, HOR_MARGIN, SEQ_TAB_SPACE_BOTTOM, HOR_MARGIN);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		localPanel.add(getLargeLabel("Sequence 1 (horizontal):"), c);

		c.insets = new Insets(SEQ_TAB_SPACE_TOP, HOR_MARGIN, SEQ_TAB_SPACE_BOTTOM, HOR_MARGIN);
		c.gridy++;
		localPanel.add(txtLocalSeq1 = getCustomTextField(""), c);
		c.gridy++;

		c.gridwidth = 1;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		localPanel.add(chkFirstComp = getCustomCheckbox("Complementary"), c);

		c.gridx++;

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		localPanel.add(btnLocalSeq1 = getCustomButton("Select file"), c);
		btnLocalSeq1.setPreferredSize(new Dimension(100, SEQ_TAB_BUTTON_HEIGHT));
		btnLocalSeq1.setMinimumSize(new Dimension(100, SEQ_TAB_BUTTON_HEIGHT));

		c.gridwidth = 2;
		c.gridx = 0;
		// a little extra space
		c.insets = new Insets(SEQ_TAB_SPACE_TOP + SEQ_TAB_BETWEEN_SEQS, HOR_MARGIN, SEQ_TAB_SPACE_BOTTOM, HOR_MARGIN);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy++;
		localPanel.add(getLargeLabel("Sequence 2 (vertical):"), c);
		c.insets = new Insets(SEQ_TAB_SPACE_TOP, HOR_MARGIN, SEQ_TAB_SPACE_BOTTOM, HOR_MARGIN);
		c.gridy++;
		localPanel.add(txtLocalSeq2 = getCustomTextField(""), c);
		c.gridy++;

		c.gridwidth = 1;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		localPanel.add(chkSecondComp = getCustomCheckbox("Complementary"), c);

		c.gridx++;

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		localPanel.add(btnLocalSeq2 = getCustomButton("Select file"), c);
		btnLocalSeq2.setPreferredSize(new Dimension(100, SEQ_TAB_BUTTON_HEIGHT));
		btnLocalSeq2.setMinimumSize(new Dimension(100, SEQ_TAB_BUTTON_HEIGHT));

		// create empty jlabel which eats up the remaining space
		c.gridy++;
		c.weighty = 200;
		localPanel.add(new JLabel(""), c);

		// help tooltips
		txtLocalSeq1.setToolTipText(hlp.getHelpText("sequence1"));
		btnLocalSeq1.setToolTipText(hlp.getHelpText("sequence1select"));
		txtLocalSeq2.setToolTipText(hlp.getHelpText("sequence2"));
		btnLocalSeq2.setToolTipText(hlp.getHelpText("sequence2select"));

		// event handler
		btnLocalSeq1.addActionListener(this);
		btnLocalSeq2.addActionListener(this);
		chkFirstComp.addActionListener(this);
		chkSecondComp.addActionListener(this);

		return localPanel;

	}

	private JPanel generatePlotOptTab() {

		HelpTexts hlp = HelpTexts.getInstance();

		JPanel plotOptTab = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel horStretcher;
		JLabel start1;

		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		Insets defInsets = new Insets(0, HOR_MARGIN, PLOTOPT_VERTSPACE, HOR_MARGIN);
		Insets leftspaceInsets = new Insets(0, HOR_MARGIN + PLOTOPT_ITEMS_INDENT, PLOTOPT_VERTSPACE, HOR_MARGIN);
		Insets extraVertInsets = new Insets(0, HOR_MARGIN + PLOTOPT_ITEMS_INDENT, PLOTOPT_VERTSPACE + PLOTOPT_EXTRAVERT, HOR_MARGIN);

		c.gridwidth = 2;
		c.insets = new Insets(PLOTOPT_BEGIN_VERTSPACE, HOR_MARGIN, PLOTOPT_VERTSPACE, HOR_MARGIN);
		plotOptTab.add(getCustomLabel("Coordinates"), c);

		c.gridy++;
		c.gridwidth = 1;
		c.insets = leftspaceInsets;
		plotOptTab.add(start1 = getCustomLabel("Start 1:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtStart1 = getCustomTextField("0", 20), c);
		c.gridx++;
		c.insets = leftspaceInsets;
		plotOptTab.add(horStretcher = new JLabel());

		c.gridx = 0;
		c.gridy++;
		c.insets = leftspaceInsets;
		plotOptTab.add(getCustomLabel("Stop 1:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtStop1 = getCustomTextField("0", 20), c);

		c.gridx = 0;
		c.gridy++;
		c.insets = leftspaceInsets;
		plotOptTab.add(getCustomLabel("Start 2:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtStart2 = getCustomTextField("0", 20), c);

		c.gridx = 0;
		c.gridy++;
		c.insets = extraVertInsets;
		plotOptTab.add(getCustomLabel("Stop 2:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtStop2 = getCustomTextField("0", 20), c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		c.insets = defInsets;
		plotOptTab.add(chkAutoZoom = getCustomCheckbox("Auto zoom"), c);
		c.gridx++;
		c.gridwidth = 2;
		plotOptTab.add(chkSmallPlots = getCustomCheckbox("Small plots"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		c.insets = extraVertInsets;
		plotOptTab.add(getCustomLabel("Zoom:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtZoom = getCustomTextField("0", 5), c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		plotOptTab.add(chkAutoParams = getCustomCheckbox("Auto params"), c);
		c.gridy++;
		c.gridwidth = 1;
		c.insets = leftspaceInsets;
		plotOptTab.add(getCustomLabel("Word length:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtWordlen = getCustomTextField("10", 5), c);

		c.gridx = 0;
		c.gridy++;
		c.insets = extraVertInsets;
		plotOptTab.add(getCustomLabel("Window size:"), c);
		c.gridx++;
		c.insets = defInsets;
		plotOptTab.add(txtWinsize = getCustomTextField("0", 5), c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		plotOptTab.add(chkAutoMatrix = getCustomCheckbox("Auto matrix"), c);
		c.gridy++;
		c.gridwidth = 1;
		c.insets = leftspaceInsets;
		plotOptTab.add(getCustomLabel("Use matrix:"), c);
		c.gridx++;
		c.gridwidth = 2;
		c.insets = defInsets;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 1;
		plotOptTab.add(cmbMatrices = getCustomComboBox(), c);
		cmbMatrices.setMaximumSize(new Dimension(170, CMB_HEIGHT));
		cmbMatrices.setMinimumSize(new Dimension(170, CMB_HEIGHT));
		cmbMatrices.setPreferredSize(new Dimension(170, CMB_HEIGHT));

		// insert stretcher
		c.gridy++;
		c.gridx = 0;
		c.weighty = 10000;
		c.gridwidth = 1;
		plotOptTab.add(new JLabel(""), c);

		// set sizes
		horStretcher.setMinimumSize(new Dimension(50, 1));
		start1.setMinimumSize(new Dimension(140, 15));

		// help tooltips
		chkAutoZoom.setToolTipText(hlp.getHelpText("autozoom"));
		txtZoom.setToolTipText(hlp.getHelpText("zoom"));
		chkAutoParams.setToolTipText(hlp.getHelpText("autoparams"));
		txtWordlen.setToolTipText(hlp.getHelpText("wordlen"));
		txtWinsize.setToolTipText(hlp.getHelpText("winsize"));
		txtStart1.setToolTipText(hlp.getHelpText("coordinates"));
		txtStop1.setToolTipText(hlp.getHelpText("coordinates"));
		txtStart2.setToolTipText(hlp.getHelpText("coordinates"));
		txtStop2.setToolTipText(hlp.getHelpText("coordinates"));
		chkAutoMatrix.setToolTipText(hlp.getHelpText("automatrix"));
		cmbMatrices.setToolTipText(hlp.getHelpText("substmatrix"));
		chkSmallPlots.setToolTipText(hlp.getHelpText("smallplots"));
		// set input verifiers
		txtZoom.setInputVerifier(new IntVerifier("Ratio", ClientGlobals.MINZOOM, ClientGlobals.MAXZOOM));
		txtWordlen.setInputVerifier(new IntVerifier("Word length", ClientGlobals.MINWORDLEN - 1, ClientGlobals.MAXWORDLEN));
		txtWinsize.setInputVerifier(new IntVerifier("Window size", ClientGlobals.MINWINDOWSIZE, ClientGlobals.MAXWINDOWSIZE));
		txtStart1.setInputVerifier(new IntVerifier("Coordinates", 0, Integer.MAX_VALUE));
		txtStop1.setInputVerifier(new IntVerifier("Coordinates", 0, Integer.MAX_VALUE));
		txtStart2.setInputVerifier(new IntVerifier("Coordinates", 0, Integer.MAX_VALUE));
		txtStop2.setInputVerifier(new IntVerifier("Coordinates", 0, Integer.MAX_VALUE));

		// event handler
		chkAutoZoom.addActionListener(this);
		chkAutoParams.addActionListener(this);
		chkAutoMatrix.addActionListener(this);
		cmbMatrices.addActionListener(this);
		chkSmallPlots.addActionListener(this);

		// read values for auto zoom and auto parameters from config
		chkAutoZoom.setSelected(Config.getInstance().getIntVal("autozoom", 1) == 1 ? true : false);
		chkAutoParams.setSelected(Config.getInstance().getIntVal("autoparams", 1) == 1 ? true : false);
		chkAutoMatrix.setSelected(Config.getInstance().getIntVal("automatrix", 1) == 1 ? true : false);
		chkSmallPlots.setSelected(Config.getInstance().getIntVal("smallplots", 0) == 1 ? true : false);

		// enable/disable text boxes accordingly
		txtZoom.setEnabled(!chkAutoZoom.isSelected());
		txtWordlen.setEnabled(!chkAutoParams.isSelected());
		txtWinsize.setEnabled(!chkAutoParams.isSelected());
		cmbMatrices.setEnabled(!chkAutoMatrix.isSelected());
		chkSmallPlots.setEnabled(chkAutoZoom.isSelected());

		fillSubstMatrixCombo();

		return plotOptTab;
	}

	private void switchMode(boolean advanced) {
		if (advanced) {
			btnAdvanced.setText("<< Simple mode");
			optTabs.setVisible(true);
			isAdvancedMode = true;
		} else {
			btnAdvanced.setText(">> Advanced mode");
			optTabs.setVisible(false);
			isAdvancedMode = false;
		}
		// store setting
		Config.getInstance().setIntVal("advanced", isAdvancedMode ? 1 : 0);
	}

	private void fillSubstMatrixCombo() {
		cmbMatrices.removeAllItems();
		for (int i = 0; i < substMatrices.length; i++)
			// cmbMatrices.addItem("ya");
			cmbMatrices.addItem(substMatrices[i].getName());

		cmbMatrices.adaptPopupWidth();
	}

	private JPanel generateMiscTab() {

		JPanel miscTab = new JPanel();
		HelpTexts hlp = HelpTexts.getInstance();

		miscTab.setLayout(new GridBagLayout());

		Insets defInsets = new Insets(MISC_VERTSPACE, HOR_MARGIN, 0, HOR_MARGIN);
		Insets extraVertInsets = new Insets(MISC_VERTSPACE, HOR_MARGIN, MISC_EXTRAVERT, HOR_MARGIN);
		Insets initInsets = new Insets(MISC_VERTSPACE + MISC_INITVERT, HOR_MARGIN, 0, HOR_MARGIN);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;

		c.gridy++;
		c.insets = initInsets;
		miscTab.add(chkSaveSA = getCustomCheckbox("Save suffix arrays "), c);

		c.gridy++;
		c.insets = defInsets;
		miscTab.add(lblSASpace = getCustomLabel(""), c);
		c.gridy++;
		c.insets = extraVertInsets;
		miscTab.add(btnDeleteSAs = getCustomButton("Delete suffix array files"), c);

		c.gridy++;
		c.insets = defInsets;
		miscTab.add(getCustomLabel("Local dotplot click action..."), c);
		c.gridy++;
		miscTab.add(optLocalShowAlign = getCustomRadioButton("... show alignment"), c);
		c.gridy++;
		miscTab.add(optLocalExport = getCustomRadioButton("... export window to FASTA"), c);
		c.gridy++;
		c.insets = extraVertInsets;
		miscTab.add(optLocalDoNothing = getCustomRadioButton("... do nothing"), c);

		c.gridy++;
		miscTab.add(btnSetupProxy = getCustomButton("HTTP proxy settings"), c);

		// set sizes
		btnDeleteSAs.setPreferredSize(new Dimension(175, 24));
		btnDeleteSAs.setMinimumSize(new Dimension(175, 24));
		btnSetupProxy.setPreferredSize(new Dimension(175, 24));
		btnSetupProxy.setMinimumSize(new Dimension(175, 24));

		// insert stretcher
		c.gridy++;
		c.weighty = 100000;
		c.gridwidth = 1;
		miscTab.add(new JLabel(""), c);

		updateSADiskSpace();

		// pre-select
		optLocalShowAlign.setSelected(true);

		// create button groups
		ButtonGroup localGrp = new ButtonGroup();
		localGrp.add(optLocalShowAlign);
		localGrp.add(optLocalExport);
		localGrp.add(optLocalDoNothing);

		// load values from config
		chkSaveSA.setSelected(Config.getInstance().getIntVal("savesa", 0) == 1 ? true : false);
		optLocalShowAlign.setSelected(Config.getInstance().getIntVal("local_click", 0) == 0 ? true : false);
		optLocalExport.setSelected(Config.getInstance().getIntVal("local_click", 0) == 1 ? true : false);
		optLocalDoNothing.setSelected(Config.getInstance().getIntVal("local_click", 0) == 2 ? true : false);

		// tell GUI if alignments shall be shown (for local plots)
		ctrl.showAlignments(optLocalShowAlign.isSelected());

		// add action listeners
		chkSaveSA.addActionListener(this);
		optLocalShowAlign.addActionListener(this);
		optLocalExport.addActionListener(this);
		optLocalDoNothing.addActionListener(this);
		btnDeleteSAs.addActionListener(this);
		btnSetupProxy.addActionListener(this);

		// help tooltips
		chkSaveSA.setToolTipText(hlp.getHelpText("savesa"));
		optLocalShowAlign.setToolTipText(hlp.getHelpText("localclick"));
		optLocalDoNothing.setToolTipText(hlp.getHelpText("localclick"));
		btnDeleteSAs.setToolTipText(hlp.getHelpText("deletesas"));
		btnSetupProxy.setToolTipText(hlp.getHelpText("httpproxy"));

		return miscTab;
	}

	private JRadioButton getCustomRadioButton(String caption) {
		JRadioButton ret = new JRadioButton(caption);
		ret.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		ret.setFont(MAIN_FONT);
		return ret;
	}

	private JCheckBox getCustomCheckbox(String caption) {
		JCheckBox ret = new JCheckBox(caption);
		ret.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		ret.setFont(MAIN_FONT);
		return ret;
	}

	private JButton getCustomButton(String caption) {
		JButton ret = new JButton(caption);
		ret.setFont(MAIN_FONT);
		return ret;
	}

	private JLabel getCustomLabel(String caption) {
		JLabel ret = new JLabel(caption);
		ret.setFont(MAIN_FONT);

		return ret;
	}

	private JLabel getLargeLabel(String caption) {
		JLabel ret = new JLabel(caption);
		ret.setFont(CAPT_FONT);

		return ret;
	}

	private JTextField getCustomTextField(String text, int size) {
		JTextField ret = new JTextField(text, size);
		ret.setFont(TEXT_FONT);
		return ret;
	}

	private JTextField getCustomTextField(String text) {
		JTextField ret = new JTextField(text);
		ret.setFont(TEXT_FONT);
		return ret;
	}

	private CustomComboBox getCustomComboBox() {
		CustomComboBox ret = new CustomComboBox();
		ret.setFont(TEXT_FONT);
		ret.setPreferredSize(new Dimension(1, CMB_HEIGHT));
		return ret;
	}

	private JPanel generateDispTab() {
		JPanel dispTab = new JPanel();
		HelpTexts hlp = HelpTexts.getInstance();

		ImageIcon icoExport = new ImageIcon(this.getClass().getResource("/resources/images/export.gif"));

		Insets smallInsets = new Insets(DISP_VERT_SMALL, HOR_MARGIN, 0, HOR_MARGIN);
		Insets bigInsets = new Insets(DISP_VERT_BIG, HOR_MARGIN, 0, HOR_MARGIN);
		Insets extraInsets = new Insets(DISP_VERT_BIG + DISP_ABOVE_EXPORT, HOR_MARGIN, 0, HOR_MARGIN);

		GridBagConstraints c = new GridBagConstraints();
		dispTab.setLayout(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.insets = smallInsets;
		dispTab.add(getCustomLabel("Lower color limit:"), c);
		c.gridy++;
		dispTab.add(scrLower = new JScrollBar(SwingConstants.HORIZONTAL, 0, 0, 0, 1), c);

		c.gridy++;
		c.insets = bigInsets;
		dispTab.add(getCustomLabel("Upper color limit:"), c);
		c.gridy++;
		c.insets = smallInsets;
		dispTab.add(scrUpper = new JScrollBar(SwingConstants.HORIZONTAL, 0, 0, 0, 1), c);

		c.gridy++;
		c.insets = bigInsets;
		dispTab.add(getCustomLabel("Greyscale start:"), c);
		c.gridy++;
		c.insets = smallInsets;
		dispTab.add(scrGreyscale = new JScrollBar(SwingConstants.HORIZONTAL, 0, 0, 0, 100), c);

		c.gridy++;
		c.insets = bigInsets;
		dispTab.add(chkRevCompAlign = getCustomCheckbox("Reverse complementary alignment"), c);
		c.gridy++;
		c.insets = smallInsets;

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = extraInsets;
		c.gridwidth = 1;
		dispTab.add(getCustomLabel("Export image:"), c);
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		dispTab.add(btnExportFile = new JButton(icoExport), c);
		// btnExportFile = new JButton(icoExport);

		// insert stretcher
		c.gridy++;
		c.weighty = 10000;
		c.gridwidth = 1;
		dispTab.add(new JLabel(), c);

		// help tooltips
		scrLower.setToolTipText(hlp.getHelpText("lowerlimit"));
		scrUpper.setToolTipText(hlp.getHelpText("upperlimit"));
		scrGreyscale.setToolTipText(hlp.getHelpText("greyscale"));
		btnExportFile.setToolTipText(hlp.getHelpText("export"));
		chkRevCompAlign.setToolTipText(hlp.getHelpText("reversecomplementary"));

		// add event handler
		scrLower.addAdjustmentListener(this);
		scrUpper.addAdjustmentListener(this);
		scrGreyscale.addAdjustmentListener(this);
		btnExportFile.addActionListener(this);
		chkRevCompAlign.addActionListener(this);

		// set size of export button

		btnExportFile.setMaximumSize(new Dimension(20, 30));

		return dispTab;
	}

	private void setDispTabEnabled(boolean e) {
		Component[] components = dispTab.getComponents();
		for (int i = 0; i < components.length; i++)
			components[i].setEnabled(e);
	}

	private void setNavigationEnabled(boolean e) {
		btnZoomOut.setEnabled(e);
		btnZoomIn.setEnabled(e);
		btnFullPlot.setEnabled(e);
		btnExportFile.setEnabled(e);
	}

	// called by controller when dotplot starts being calculated
	public void dotplotStartCalc() {
		btnGo.setEnabled(false);
		setDispTabEnabled(false);
		setNavigationEnabled(false);
	}

	// called by controller when dotplot is ready
	public void dotplotReady() {
		btnGo.setEnabled(true);
		setDispTabEnabled(true);
		setNavigationEnabled(true);
	}

	// called by controller when dotplot calculation failed
	public void dotplotFailed() {
		btnGo.setEnabled(true);
		if (ctrl.dotplotExists()) {
			setDispTabEnabled(true);
			setNavigationEnabled(true);
		}
		// reset coordinates
		txtStart1.setText("0");
		txtStop1.setText("0");
		txtStart2.setText("0");
		txtStop2.setText("0");
	}

	public boolean saveSuffixArrays() {
		return chkSaveSA.isSelected();
	}

	public boolean needReload() {
		return needreload;
	}

	public void setNeedReload(boolean c) {
		// set changed flag
		needreload = c;
	}

	// setup scrollbar parameters according to current dotmatrix
	public void setupScrollbars(float lowest, float highest) {
		noScrollbarEvents = true;

		// round limit value
		scrLower.setMaximum((int) (highest * 100));
		scrUpper.setMaximum((int) (highest * 100));
		scrLower.setMinimum((int) (lowest * 100));
		scrUpper.setMinimum((int) (lowest * 100));
		// set scrollbar increment values
		scrLower.setUnitIncrement((int) (highest / 10));
		scrLower.setBlockIncrement((int) (highest));
		scrUpper.setUnitIncrement((int) (highest / 10));
		scrUpper.setBlockIncrement((int) (highest));

		noScrollbarEvents = false;

	}

	public void setScrollbars(float percentLower, float percentUpper, float percentGreyscale, float percentFunCats) {
		noScrollbarEvents = true;
		// set values
		scrUpper.setValue((int) ((float) (scrUpper.getMaximum() - scrUpper.getMinimum()) / 100f * (float) percentUpper) + scrUpper.getMinimum());
		scrLower.setValue((int) ((float) (scrLower.getMaximum() - scrLower.getMinimum()) / 100f * (float) percentLower) + scrLower.getMinimum());
		scrGreyscale.setValue(
				(int) ((float) (scrGreyscale.getMaximum() - scrGreyscale.getMinimum()) / 100f * (float) percentGreyscale) + scrGreyscale.getMinimum());
		// manually call adjustment value change handler to invoke
		// Plotter.reCalc()
		noScrollbarEvents = false;
		adjustmentValueChanged(null);

	}

	public void adjustmentValueChanged(AdjustmentEvent evt) {
		try {
			if (!noScrollbarEvents) {
				// get values
				int lv = scrLower.getValue(), uv = scrUpper.getValue(), gv = scrGreyscale.getValue();

				// assure lower value is not higher than upper value
				if (lv >= uv) {
					scrLower.setValue(uv - 1);
					lv = uv - 1;
				}
				// replot
				ctrl.eventReplot(lv / 100f, uv / 100f, gv / 100f);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ClientGlobals.unexpectedError(e, ctrl);
		}

	}

	public void actionPerformed(ActionEvent evt) {

		// catch everything (which is not caught by inner try blocks)
		try {

			// check which control generated the event
			if (evt.getSource() == cmbMatrices) {
				// *** MATRIX CHANGED, RELOAD REQUIRED
				needreload = true;

				// check if the user wants to select a custom matrix file
				if (substMatrices[cmbMatrices.getSelectedIndex()].getFile().equals("-")) {
					FileChooseResult f = showFileDialog("Select matrix file", true, false);
					// set value in GUI
					if (f.file != null) {
						// try to load this file
						try {
							customMatrix = SubstitutionMatrix.loadFromFile(f.file);
							customMatrix.setNucleotideMatrix(false);
						} catch (InvalidSubMatFileException e) {
							ClientGlobals.warnMessage("The substitution matrix seems to be invalid.\nPlease specify a matrix file in Blast compatible format.");
							customMatrix = null;
						}

					}
				} else
					customMatrix = null;

			} else if ((evt.getSource() == btnLocalSeq1) || (evt.getSource() == btnLocalSeq2)) {
				// *** SELECT SEQUENCE FILE
				// show dialog
				FileChooseResult f = showFileDialog("Select sequence file", true, false);
				// set value in GUI
				if (f.file != null) {
					if (evt.getSource() == btnLocalSeq1) // set first sequence
						txtLocalSeq1.setText(f.file);
					else // set second sequence
						txtLocalSeq2.setText(f.file);
					newDotplot();
				}

			} else if (evt.getSource() == btnGo) {
				initiateLocalDotplot();

			} else if (evt.getSource() == chkAutoZoom) {
				// *** auto ratio checkbox
				// enable/disable ratio text box
				txtZoom.setEnabled(!chkAutoZoom.isSelected());
				chkSmallPlots.setEnabled(chkAutoZoom.isSelected());
				chkSmallPlots.setSelected(false);
				// save config setting
				Config.getInstance().setVal("autozoom", chkAutoZoom.isSelected() ? "1" : "0");

			} else if (evt.getSource() == chkAutoParams) {
				// *** auto params checkbox
				// enable/disable wordlen & winsize text box
				txtWordlen.setEnabled(!chkAutoParams.isSelected());
				txtWinsize.setEnabled(!chkAutoParams.isSelected());
				// save config setting
				Config.getInstance().setVal("autoparams", chkAutoParams.isSelected() ? "1" : "0");

			} else if (evt.getSource() == btnFullPlot) {
				// *** show full dotplot
				ctrl.zoomFullPlot();
			} else if (evt.getSource() == btnZoomOut) {
				// *** zoom out
				ctrl.zoom(ClientGlobals.ZOOMOUTFACTOR);
			} else if (evt.getSource() == btnZoomIn) {
				// *** zoom in
				ctrl.zoom(ClientGlobals.ZOOMINFACTOR);
			} else if (evt.getSource() == btnExportFile) {
				// *** export image
				FileChooseResult f = showFileDialog("Store plot to image file", false, true);

				// report to controller
				if (f.file != null)
					ctrl.eventExportImage(f.file, f.type);

			} else if (evt.getSource() == chkSaveSA) {
				// save config setting
				Config.getInstance().setVal("savesa", chkSaveSA.isSelected() ? "1" : "0");
			} else if (evt.getSource() == optLocalShowAlign) {
				// tell controller if alignments will be shown
				setupForAlignments();
				// save setting
				Config.getInstance().setVal("local_click", "0");
			} else if (evt.getSource() == optLocalExport) {
				// tell controller if alignments will be shown
				setupForAlignments();
				// save setting
				Config.getInstance().setVal("local_click", "1");
			} else if (evt.getSource() == optLocalDoNothing) {
				// tell controller if alignments will be shown
				setupForAlignments();
				// save setting
				Config.getInstance().setVal("local_click", "2");
			} else if (evt.getSource() == btnAdvanced)
				switchMode(!isAdvancedMode);
			else if (evt.getSource() == chkAutoMatrix) {
				// set GUI
				if (chkAutoMatrix.isSelected()) {
					cmbMatrices.setEnabled(false);
					cmbMatrices.setSelectedIndex(0);
					// store setting
					Config.getInstance().setVal("automatrix", "1");
				} else {
					cmbMatrices.setEnabled(true);
					// store setting
					Config.getInstance().setVal("automatrix", "0");
				}

			} else if (evt.getSource() == btnDeleteSAs) {
				// delete suffix array files
				deleteSAFiles();
				updateSADiskSpace();
			} else if (evt.getSource() == btnQuit) {
				ctrl.exit();
			} else if (evt.getSource() == btnSetupProxy) {
				new ProxyDialog(ctrl);
			} else if (evt.getSource() == chkSmallPlots) {
				// store setting
				Config.getInstance().setVal("smallplots", (chkSmallPlots.isSelected()) ? "1" : "0");
			} else if (evt.getSource() == chkFirstComp) {
				if (chkFirstComp.isSelected())
					chkSecondComp.setSelected(false);
				newDotplot();
			} else if (evt.getSource() == chkSecondComp) {
				if (chkSecondComp.isSelected())
					chkFirstComp.setSelected(false);
				newDotplot();
			} else if (evt.getSource() == chkRevCompAlign) {
				ctrl.updateAlignment();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ClientGlobals.unexpectedError(e, ctrl);
		}

	}

	private void setupForAlignments() {

		// local mode
		if (optLocalShowAlign.isSelected())
			ctrl.showAlignments(true);
		else
			ctrl.showAlignments(false);
	}

	private void newDotplot() {
		// set changed flag
		needreload = true;
		// set coordinates to zero
		txtStart1.setText("0");
		txtStop1.setText("0");
		txtStart2.setText("0");
		txtStop2.setText("0");
		// set go-button caption
		setGoButtonCaption(false);

	}

	private void initiateLocalDotplot() {

		// check if the user actually selected two sequences
		if (txtLocalSeq1.getText().trim().equals("") || txtLocalSeq2.getText().trim().equals("")) {
			ClientGlobals.warnMessage("Please select two sequence files.");
			return;
		}

		// determine type of both sequence files
		boolean firstSecIsNucl;
		try {
			firstSecIsNucl = FASTAReader.isNucleotideFile(txtLocalSeq1.getText());
		} catch (IOException e) {
			ClientGlobals.errMessage("Failed loading sequence file 1\nIO error: " + e.getMessage());
			return;
		} catch (InvalidFASTAFileException e) {
			ClientGlobals.errMessage("Failed loading sequence file 1\nInvalid FASTA file: " + e.getMessage());
			return;
		}

		boolean secondSecIsNucl;
		try {
			secondSecIsNucl = FASTAReader.isNucleotideFile(txtLocalSeq2.getText());
		} catch (IOException e) {
			ClientGlobals.errMessage("Failed loading sequence file 2\nIO error: " + e.getMessage());
			return;
		} catch (InvalidFASTAFileException e) {
			ClientGlobals.errMessage("Failed loading sequence file 2\nInvalid FASTA file: " + e.getMessage());
			return;
		}

		// load matrix
		String basePath = ClientGlobals.PATH_MATRICES;
		SubstitutionMatrix mat;
		if (!chkAutoMatrix.isSelected()) {
			// no automatrix-mode

			if (customMatrix == null) {
				// use specified matrix
				mat = loadSubstMatrix(basePath + substMatrices[cmbMatrices.getSelectedIndex()].getFile());
				mat.setNucleotideMatrix(substMatrices[cmbMatrices.getSelectedIndex()].isNucleotideMatrix());
				curSubmatName = substMatrices[cmbMatrices.getSelectedIndex()].getName();
			} else {
				// use custom matrix
				mat = customMatrix;
				curSubmatName = "Custom";
			}
			// one cannot use custom DNA substitution matrices
			if (customMatrix != null && (firstSecIsNucl || secondSecIsNucl)) {
				ClientGlobals.warnMessage("Custom matrices are only allowed for protein dotplots.");
				return;
			}

		} else {
			// auto-matrix mode

			// check if both types are equal
			if (firstSecIsNucl != secondSecIsNucl) {
				ClientGlobals.warnMessage("The two sequences do not seem to have the same type (DNA/Protein).\n"
						+ "Please choose different sequences or do not use auto-matrix mode.");
				return;
			}

			// now set automatic matrix
			if (firstSecIsNucl) {
				mat = loadSubstMatrix(basePath + ClientGlobals.AUTO_NUCL_MATRIX);
				curSubmatName = ClientGlobals.AUTO_NUCL_MATRIX_NAME;
				mat.setNucleotideMatrix(true);
			} else {
				mat = loadSubstMatrix(basePath + ClientGlobals.AUTO_PROT_MATRIX);
				curSubmatName = ClientGlobals.AUTO_PROT_MATRIX_NAME;
				mat.setNucleotideMatrix(false);
			}

		}

		// get parameters
		ParameterSet params;
		try {
			params = getParameterSet(false);
		} catch (InvalidParamSetException e) {
			// do nothing, getParameterSet() has already informed the user
			return;
		}

		if (params == null) {
			dotplotFailed();
			return;
		}

		// do last GUI check
		if ((params.wordLength == 0) && (params.windowSize == 0)) {
			ClientGlobals.warnMessage("Whether window size or word length must have a non-zero value");
			return;
		}

		if (mat != null) {

			// determine if theres a sequence we need to use the complementary
			// of
			int compseq = 0;
			if (chkFirstComp.isSelected())
				compseq = 1;
			if (chkSecondComp.isSelected())
				compseq = 2;

			// create local dotplot
			ctrl.initiateLocalDotplot(mat, txtLocalSeq1.getText(), txtLocalSeq2.getText(), params, compseq);
		}

	}

	public ParameterSet getParameterSet(boolean novalidation) throws InvalidParamSetException {
		// read values
		try {
			int ratio = Integer.parseInt(txtZoom.getText());
			int wordlen = Integer.parseInt(txtWordlen.getText());
			int windowsize = Integer.parseInt(txtWinsize.getText());
			int seq1start = Integer.parseInt(txtStart1.getText());
			int seq1stop = Integer.parseInt(txtStop1.getText());
			int seq2start = Integer.parseInt(txtStart2.getText());
			int seq2stop = Integer.parseInt(txtStop2.getText());

			// parameter validation
			if (!novalidation
					&& (((seq1stop <= seq1start) && (seq1start != 0 || seq1stop != 0)) || ((seq2stop <= seq2start) && (seq2start != 0 || seq2stop != 0)))) {
				ClientGlobals.warnMessage("Sequence start parameter must be smaller than stop parameter");
				throw new InvalidParamSetException();
			}

			// return parameter set
			return new ParameterSet(ratio, wordlen, windowsize, seq1start, seq1stop, seq2start, seq2stop, curSubmatName);

		} catch (Exception e) {
			// we need to catch an exception here as GUI values became corrupted
			// sometimes
			ClientGlobals.warnMessage("Something went wrong in the GUI, please try again what you were just intending to do.");

			e.printStackTrace();

			return null;

		}

	}

	public void setParameterSet(ParameterSet params) {
		// set values
		txtZoom.setText(params.zoom + "");
		txtWordlen.setText(params.wordLength + "");
		txtWinsize.setText(params.windowSize + "");
		txtStart1.setText(params.seq1Start + "");
		txtStop1.setText(params.seq1Stop + "");
		txtStart2.setText(params.seq2Start + "");
		txtStop2.setText(params.seq2Stop + "");
	}

	public boolean useAutoZoom() {
		return chkAutoZoom.isSelected();
	}

	public boolean useAutoParameters() {
		return chkAutoParams.isSelected();
	}

	public void showOptionsTab() {
		optTabs.setSelectedIndex(0);
	}

	public void showDisplayTab() {
		optTabs.setSelectedIndex(2);
	}

	public int getLocalClickAction() {
		if (optLocalShowAlign.isSelected())
			return ClientGlobals.LOCALCLICK_SHOWALIGN;
		else if (optLocalExport.isSelected())
			return ClientGlobals.LOCALCLICK_EXPORT;
		else
			return ClientGlobals.LOCALCLICK_NOTHING;
	}

	private FileChooseResult showFileDialog(String title, boolean open, boolean imageexport) {

		// create file chooser object
		JFileChooser f;
		if (!imageexport)
			f = new JFileChooser(Config.getInstance().getStringVal("lastopendir", ""));
		else
			f = new JFileChooser(Config.getInstance().getStringVal("lastopendir_export", ""));

		if (imageexport) {
			// remove first filter
			f.removeChoosableFileFilter(f.getFileFilter());

			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension("jpg");
			filter.addExtension("jpeg");
			filter.setDescription("JPEG image files");
			f.addChoosableFileFilter(filter);

			filter = new ExampleFileFilter();
			filter.addExtension("png");
			filter.setDescription("PNG image files");
			f.addChoosableFileFilter(filter);

			filter = new ExampleFileFilter();
			filter.addExtension("bmp");
			filter.setDescription("Bitmap image files");
			f.addChoosableFileFilter(filter);

		}

		// show correct dialog
		int retval;
		if (open)
			retval = f.showOpenDialog(this);
		else
			retval = f.showSaveDialog(this);

		if (retval == JFileChooser.APPROVE_OPTION && f.getSelectedFile() != null) {
			// store dir
			if (!imageexport)
				Config.getInstance().setVal("lastopendir", f.getSelectedFile().getParent());
			else
				Config.getInstance().setVal("lastopendir_export", f.getSelectedFile().getParent());

			String filename = f.getSelectedFile().getAbsolutePath();
			String selextension;
			// append extension if needed and if in image mode
			if (imageexport) {
				// get extension of selected type
				ExampleFileFilter sel = (ExampleFileFilter) f.getFileFilter();
				selextension = sel.getFirstExtension();
				// if this is a save file dialog and the entered file name
				// does not have an extension -> add extension
				if (!open && (filename.lastIndexOf(".") == -1))
					filename += "." + selextension;
			} else
				selextension = "";

			// return filename and type
			return new FileChooseResult(filename, selextension);

		} else
			return new FileChooseResult(null, null);

	}

	private SubstitutionMatrix loadSubstMatrix(String file) {
		// call static load method
		try {
			SubstitutionMatrix mat = SubstitutionMatrix.loadFromResource(file);
			return mat;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IO error while opening matrix file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (InvalidSubMatFileException e) {
			//
			JOptionPane.showMessageDialog(null, "Could not load matrix file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

	}

	class IntVerifier extends InputVerifier {

		private String m_name;
		private int m_min, m_max;

		public IntVerifier(String name, int min, int max) {
			m_name = name;
			m_min = min;
			m_max = max;
		}

		public boolean verify(JComponent input) {
			// get text
			String contents = ((JTextField) input).getText();

			// assume valid input
			boolean valueok = true;
			// check if text field contains a valid integer between m_min and
			// m_max
			try {
				int value = Integer.parseInt(contents);
				if ((value < m_min) || (value > m_max))
					valueok = false;
			} catch (NumberFormatException nfe) {
				valueok = false;
			}

			if (valueok == false)
				// show warning message
				JOptionPane.showMessageDialog(input.getParent().getParent(),
						m_name + " must be a valid integer between " + INTFMT.format(m_min) + " and " + INTFMT.format(m_max), "Invalid value",
						JOptionPane.WARNING_MESSAGE);
			return valueok;
		}

	}

	private class FileChooseResult {
		public String file;
		public String type;

		public FileChooseResult(String file, String type) {
			this.file = file;
			this.type = type;
		}
	}

	private long calcCurrentSADiskSpace() {

		long totalSize = 0;
		// iterate over file list
		String[] saFiles = new File(ClientGlobals.SETTINGS_DIR).list();
		for (int i = 0; i < saFiles.length; i++) {
			// check for .sa extension
			if (saFiles[i].substring(saFiles[i].length() - 3).equals(".sa"))
				totalSize += new File(ClientGlobals.SETTINGS_DIR + System.getProperty("file.separator") + saFiles[i]).length();

		}

		return totalSize;
	}

	private void deleteSAFiles() {

		// iterate over file list
		String[] saFiles = new File(ClientGlobals.SETTINGS_DIR).list();
		for (int i = 0; i < saFiles.length; i++) {
			// check for .sa extension
			if (saFiles[i].substring(saFiles[i].length() - 3).equals(".sa"))
				new File(ClientGlobals.SETTINGS_DIR + System.getProperty("file.separator") + saFiles[i]).delete();

		}
	}

	public void updateSADiskSpace() {
		lblSASpace.setText("Suffix arrays diskspace: " + ClientGlobals.convenientFileSize(calcCurrentSADiskSpace()));
	}

	public void stateChanged(ChangeEvent arg0) {
		// disable funcat tab in local mode
		optTabs.setEnabledAt(3, seqTabs.getSelectedIndex() == 1);
		setupForAlignments();
	}

	public boolean smallPlots() {
		return chkSmallPlots.isSelected();
	}

	public void uncheckFirstComp() {
		chkFirstComp.setSelected(false);
	}

	public void uncheckSecondComp() {
		chkSecondComp.setSelected(false);
	}

	public String getGUIDump() {
		StringBuilder guidump = new StringBuilder();

		// local sequences
		guidump.append("==== LOCAL SEQUENCES ====\n");
		guidump.append("Sequence 1: " + this.txtLocalSeq1.getText() + "\n");
		guidump.append("Seq1comp  : " + this.chkFirstComp.isSelected() + "\n");
		guidump.append("Sequence 2: " + this.txtLocalSeq2.getText() + "\n");
		guidump.append("Seq2comp  : " + this.chkSecondComp.isSelected() + "\n");
		guidump.append("\n");

		// plot settings
		guidump.append("==== PLOT SETTINGS ====\n");
		guidump.append("Seq1 coords : " + this.txtStart1.getText() + "-" + this.txtStop1.getText() + "\n");
		guidump.append("Seq2 coords : " + this.txtStart2.getText() + "-" + this.txtStop2.getText() + "\n");
		guidump.append("Auto zoom  : " + this.chkAutoZoom.isSelected() + "\n");
		guidump.append("Small plots: " + this.chkSmallPlots.isSelected() + "\n");
		guidump.append("Zoom       : " + this.txtZoom.getText() + "\n");
		guidump.append("Auto params : " + this.chkAutoParams.isSelected() + "\n");
		guidump.append("Word length : " + this.txtWordlen.getText() + "\n");
		guidump.append("Window size : " + this.txtWinsize.getText() + "\n");
		guidump.append("Auto matrix: " + this.chkAutoMatrix.isSelected() + "\n");
		guidump.append("Matrix     : " + this.cmbMatrices.getSelectedItem() + "\n");
		guidump.append("\n");

		// misc settings
		guidump.append("==== MISC SETTINGS ====\n");
		guidump.append("Save SAs: " + this.chkSaveSA.isSelected() + "\n");
		String localaction;
		if (this.optLocalDoNothing.isSelected())
			localaction = "Do nothing";
		else
			localaction = "Show alignment";
		guidump.append("Local click action: " + localaction + "\n");
		guidump.append("\n");

		return guidump.toString();
	}

	public void setRevComplAlign(boolean revcomp) {
		chkRevCompAlign.setSelected(revcomp);
	}

	public boolean reverseComplementaryAlignments() {
		return chkRevCompAlign.isSelected();
	}

	public void setNucleotidePlot(boolean nuclplot) {
		if (nuclplot) {
			chkRevCompAlign.setEnabled(true);
		} else {
			chkRevCompAlign.setEnabled(false);
			chkRevCompAlign.setSelected(false);
		}

	}

}
