package org.gepard.client.userinterface;

import java.awt.Point;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.gepard.client.Controller;


// main client window class

public class ContainerWindow extends JFrame	{

	private static final long serialVersionUID = -5944334167138374870L;
	
	private Controller ctrl;

	public ContainerWindow() {
	}
	
	public void setup(Controller ictrl, MainPanel mp) {
		
		setSize(600, 600);

		// store controller
		ctrl = ictrl;
		
		// add main panel
		setContentPane(mp);
		
		pack();
		
		// event handler
		addWindowListener(new WindowAdapter() {
			
			public void windowActivated(WindowEvent e) {
				// bring any child windows to the front
				Window[] children = getOwnedWindows();
				for(int i = 0; i < children.length; i++) 
					children[i].toFront();
			}
			
			public void windowClosing(WindowEvent e) {
				// tell controller program is exitting
				ctrl.eventExit();
			}
			
		});
		
		setExtendedState(this.getExtendedState() | MAXIMIZED_BOTH);
		setVisible(true);
		
	}
	
	public WindowPos getPosition() {
		int width = getWidth();
		int height = getHeight();
		Point loc = getLocation();
				
		return new WindowPos(width,height,loc.x,loc.y);
	}
	
	public void close() {
		dispose();
		ctrl.eventExit();
	}
	

}