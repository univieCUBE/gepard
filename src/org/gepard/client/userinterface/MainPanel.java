package org.gepard.client.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.gepard.client.ClientGlobals;

// main panel (content panel of container window)

public class MainPanel extends JPanel {
	
	private static final long serialVersionUID = 9048209022860256682L;
	
	private JScrollPane drawScrollPane;
	
	public MainPanel(ControlPanel cp, DrawPanel dp,	InfoPanel ip) {
		// create main panel
		setLayout(new BorderLayout());
		
		// create draw panel scroll pane
		drawScrollPane = new JScrollPane(dp);
		InputMap im =drawScrollPane.getInputMap();
		// no keyboard input shall be accepted
		im.clear();
		drawScrollPane.setInputMap(JComponent.WHEN_FOCUSED, im);
		drawScrollPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);
		drawScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		drawScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		JScrollPane controlScrollPane = new JScrollPane(cp);
		controlScrollPane.setPreferredSize(new Dimension(ClientGlobals.CONTROLPANEL_WIDTH,1));
		//controlScrollPane.setMinimumSize(new Dimension(200,300));
		controlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		controlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		
		// create sub panel
		JPanel subpane = new JPanel();
		subpane.setLayout(new BorderLayout());
		subpane.add(BorderLayout.CENTER, drawScrollPane);
		subpane.add(BorderLayout.SOUTH, ip);
		// create layout
		add(BorderLayout.WEST, controlScrollPane );
		add(BorderLayout.CENTER, subpane);
	}
	
	public Dimension getRealDrawPanelDim() {
		return drawScrollPane.getSize();
	}
}
