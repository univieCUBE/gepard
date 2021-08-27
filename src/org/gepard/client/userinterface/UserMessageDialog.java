package org.gepard.client.userinterface;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gepard.client.ClientGlobals;
import org.gepard.client.Controller;

// dialog for developer-defined user messages

public class UserMessageDialog extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = -6831146639624661450L;

	
	public static void showUserMessageIfExisting(String file) {
		// try opening the resource
		InputStream stream;
		if ( (stream = Controller.class.getResourceAsStream(file)) != null) {
			try {
				// open reader
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				// first line = caption
				String caption = reader.readLine();
				// second line = empty
				reader.readLine();
				// rest: message
				StringBuilder msg = new StringBuilder();
				String line;
				while ( (line = reader.readLine()) != null) 
					msg.append(line + "\n");
				
				new UserMessageDialog(caption, msg.toString());
				
			} catch (IOException e) {
				// this should not happen, user message file probably has wrong format 
				// don't do anything
			}
		}
		
	}

	private JTextArea txtMessage;
		
	private UserMessageDialog(String caption, String message) {
		
		super(caption);
		
		setSize(new Dimension(480,340));
		setupGUI();
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass()
	    		.getResource(ClientGlobals.RES_APPICON)));
		
		setVisible(true);
		setResizable(false);
		// get containers position and center dialog
		Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(SCREEN_SIZE.width / 2) - (getWidth()/2),
			(SCREEN_SIZE.height / 2) - (getHeight()/2)
		);
		
		// set user message
		txtMessage.setText(message);
		txtMessage.setCaretPosition(0);
		
	}

	private void setupGUI() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1000;
		c.fill = GridBagConstraints.BOTH;  
		
		txtMessage = new JTextArea();
		JScrollPane scrolling = new JScrollPane(txtMessage);
		add(scrolling, c);
		
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.EAST;
		JButton btnQuit;
		add(btnQuit = new JButton("Close"),c);
		
		// create border
		//txtMessage.setBorder(BorderFactory.createEtchedBorder() );
		// scrolling
		scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrolling.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// set font
		txtMessage.setFont(new Font("Courier", Font.PLAIN, 11));
		// additional flags
		txtMessage.setEditable(false);
		txtMessage.setLineWrap(true);
		txtMessage.setWrapStyleWord(true);
		
		// event handler
		btnQuit.addActionListener(this);
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// close dialog
		dispose();		
	}
	
	
}
