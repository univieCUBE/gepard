package org.gepard.client.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

import org.gepard.client.Controller;
import org.gepard.client.DrawTarget;
import org.gepard.client.Plotter;

public class DrawPanel extends JPanel implements DrawTarget {

	private static final long serialVersionUID = 923132750680455953L;

	// plotter
	Plotter p = null;

	// control class
	Controller ctrl = null;

	public DrawPanel(Controller ictrl) {
		
		// add event handlers
		
		addMouseListener(
				new MouseAdapter() {
					
					public void mouseClicked(MouseEvent evt) {
						// report event if a dotplot is shown
						if (p != null) {
							ctrl.eventMouseClick(evt.getX(), evt.getY(), evt.getButton());
							// request focus (to receive keyboard input)
							requestFocus();
						}
					}
					
					public void mouseReleased(MouseEvent evt) {
						// report event if a dotplot is shown
						if (p != null)
							ctrl.eventMouseRelease(evt.getX(), evt.getY());
					}
				}
				
		);
		
		addMouseMotionListener(
				new MouseMotionAdapter() {
					
					public void mouseMoved(MouseEvent evt) {
						// report event if a dotplot is shown
						if (p != null)
							ctrl.eventMouseMove(evt.getX(), evt.getY());
					}
					
					public void mouseDragged(MouseEvent evt) {
						// report event if a dotplot is shown
						if (p != null)
							ctrl.eventMouseDrag(evt.getX(), evt.getY());
					}
					
				}
		);
		
		addKeyListener(
				new KeyAdapter() {
					
					public void keyPressed(KeyEvent evt) {
						// pass event to controller
						ctrl.eventKeyPressed(evt);
					}
					
					public void keyReleased(KeyEvent evt) {
						// pass event to controller
						ctrl.eventKeyReleased(evt);
					}
				}
		);
		
		// store controller reference
		ctrl = ictrl;	

		setFocusable(true);

	}

	// plotter accessor method
	public void setPlotter(Plotter ip) {
		p = ip;
	}
	
	public void paintComponent(Graphics g) {
		// whiten
		g.setColor(Color.WHITE);
		g.fillRect(0,0,2000,2000);
		if (p != null)  {
			// redraw
			p.redraw((Graphics2D)g);
		}
		
	}
	
}