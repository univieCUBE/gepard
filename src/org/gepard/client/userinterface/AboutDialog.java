package org.gepard.client.userinterface;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.gepard.client.BrowserLauncher;
import org.gepard.client.ClientGlobals;

public class AboutDialog extends JFrame implements MouseListener, ActionListener {

	private static final long serialVersionUID = 2421453233837541464L;
	
	private static int LEFT_MARGIN = 10;
	private static int VERT_SPACE = 10;
	private static int VERT_SPACE_AFTER_TOP = 20;
	private static int INIT_VERT_SPACE = 2;
	private static double LEFT_WEIGHT=1;
	private static double RIGHT_WEIGHT=2;
	private static int BELOW_CLOSE = 7;
	private static int EXTRA_AFTER_AUTHOR = 10;
	private static int EXTRA_AFTER_CONTACT = 10;
	private static int BETWEEN_CONTACT_WEB = 5;
	private static int LOGO_TOPMARG = 10;
	
	private static int WIN_WIDTH = 450;
	private static int WIN_HEIGHT = 315;
	
	JButton close, ref;

	public AboutDialog() {
		
		setupGUI();
        setSize(WIN_WIDTH,WIN_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        
        // set icon
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass()
	    		.getResource(ClientGlobals.RES_APPICON)));
        
		pack();
        setVisible(true);
	}
	
	private JLabel getBoldJLabel(String caption) {
		JLabel ret = new JLabel(caption);
		Font oldFont = ret.getFont();
		ret.setFont(new Font(oldFont.getName(), 
				oldFont.getStyle() | Font.BOLD, oldFont.getSize()));
		return ret;
	}
	
	private JLabel getHeaderJLabel(String caption) {
		JLabel ret = new JLabel(caption);
		Font oldFont = ret.getFont();
		ret.setFont(new Font(oldFont.getName(), 
				oldFont.getStyle() | Font.BOLD, oldFont.getSize() + 2));
		return ret;
	}
	
	private JLabel getNormalJLabel(String caption) {
		JLabel ret = new JLabel(caption);
		Font oldFont = ret.getFont();
		ret.setFont(new Font(oldFont.getName(), 
				oldFont.getStyle() &~ Font.BOLD, oldFont.getSize()));
		return ret;
	}
	
	private JLabel getLinkJLabel(String caption) {
		JLabel ret = new JLabel("<html><u>" + caption + "</u></html>");
		Font oldFont = ret.getFont();
		ret.setFont(new Font(oldFont.getName(), 
				oldFont.getStyle() &~ Font.BOLD, oldFont.getSize() ));
		ret.setForeground(Color.BLUE);
		ret.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		ret.addMouseListener(this);
		return ret;
	}
	
	private void setupGUI() {
		
		setTitle("About " + ClientGlobals.APPNAME);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(0,LEFT_MARGIN, 0,0);
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1;
		// LEFT COLUMN
		c.weightx = LEFT_WEIGHT; 
		c.gridx = 0; c.gridy = 0;
		
		
		c.gridwidth = 1;   c.insets = new Insets(INIT_VERT_SPACE,LEFT_MARGIN,0 ,0);
		add(getHeaderJLabel(ClientGlobals.APPNAME),c);
		
		// logo
		c.gridx++;
		c.gridwidth = 1; c.gridheight = 2	;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(LOGO_TOPMARG,0,0,0);
		add(new JLabel(new ImageIcon(this.getClass().getResource("/resources/images/logo.gif"))),c);
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = 1; c.gridheight = 1;
		c.gridx=0;
		c.gridy++; c.insets = new Insets(0,LEFT_MARGIN, VERT_SPACE_AFTER_TOP,0);
		add(getNormalJLabel(ClientGlobals.VERSION ),c);
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0; c.gridheight = 1;
		c.gridwidth = 1; c.insets = new Insets(0,LEFT_MARGIN, EXTRA_AFTER_AUTHOR,0);
		c.gridy++;
		add(getBoldJLabel("Developed by:"),c);
		c.insets = new Insets(0,LEFT_MARGIN, 0,0);
		c.gridy++;  c.insets = new Insets(0,LEFT_MARGIN, EXTRA_AFTER_CONTACT,0);
		add(getBoldJLabel("Web:"),c);
		c.gridy++;    c.insets = new Insets(0,LEFT_MARGIN, VERT_SPACE,0);
		add(getBoldJLabel("Thanks to:"),c); 
		c.gridy+=3; 
		add(getBoldJLabel("Logo:"),c);
		c.gridy++;
		add(getBoldJLabel("Institution:"),c);

		
		// RIGHT COLUMN
		c.weightx =  RIGHT_WEIGHT;
		c.gridx = 1; c.gridy = 2; 
		add(getBoldJLabel("Jan Krumsiek, Thomas Rattei"),c); 
		c.gridy++; c.insets = new Insets(0,LEFT_MARGIN, BETWEEN_CONTACT_WEB,0);
		add(getLinkJLabel(ClientGlobals.WEB_ADRESS),c);
//		c.gridy++;
		c.gridy++; 	c.insets = new Insets(0,LEFT_MARGIN, 0,0);
		add(getNormalJLabel("Roland Arnold, Patrick Tischler, "),c);
		c.gridy++;  c.insets = new Insets(0,LEFT_MARGIN, VERT_SPACE,0);
		add(getNormalJLabel("Dominik Lindner, Volker Stuempflen, Tini"  ),c);
		c.gridy++;c.gridy++; 
		add(getNormalJLabel("Tobias Petri"  ),c);
		c.gridy++;
		add(getNormalJLabel("Division of Computational Systems Biology"  ),c);
		c.gridy++;
		add(getNormalJLabel("University of Vienna"  ),c);
		
		
		c.gridy++; c.gridy++; c.anchor = GridBagConstraints.EAST;		
		c.insets = new Insets(0,0,BELOW_CLOSE,LEFT_MARGIN);
		close = new JButton("Close");
		add(close,c);
		close.addActionListener(this);
		
		c.gridx--;
		c.insets = new Insets(0,LEFT_MARGIN,0,0);
		c.anchor = GridBagConstraints.WEST;
		ref = new JButton("Reference");
		add(ref,c);
		ref.addActionListener(this);
		
	}
	
	public void mouseClicked(MouseEvent arg0) {
		// input is a jlabel
		JLabel input = (JLabel)arg0.getSource();
		
		try {
			// check if this is an email adress or a URL
			if (input.getText().indexOf('@') > -1) 
				BrowserLauncher.openURL("mailto:" + ClientGlobals.AUTHOR_EMAIL);
			else
				BrowserLauncher.openURL(ClientGlobals.WEB_ADRESS);
		} catch (Exception e) {
			ClientGlobals.errMessage("Could not open browser.\n\nError:\n" + e.getMessage() );
		}
	}
	

	public void actionPerformed(ActionEvent arg0) {
		
		if (arg0.getSource() == close) {
			// close dialog
			this.dispose();
			System.exit(0);
		} else if (arg0.getSource() == ref) {
			UserMessageDialog.showUserMessageIfExisting(ClientGlobals.FILE_REFERENCE);
		}
	}
		
	
	// unused listener events
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}

	
	public static void main(String[] args) {
		new AboutDialog();
	}

}
