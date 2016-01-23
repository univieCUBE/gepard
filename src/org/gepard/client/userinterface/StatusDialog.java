package org.gepard.client.userinterface;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.gepard.client.Controller;
import org.gepard.common.DotMatrixCallback;

// calcuation status dialog for local dotplots

public class StatusDialog extends JDialog implements DotMatrixCallback, ActionListener {

	private static final long serialVersionUID = -7182005894700504482L;
	
	JLabel lblStatus;
	JProgressBar progress;
	JButton btnAbort;
	JDialog dialog;
	
	Controller ctrl;
	
	public StatusDialog(Controller ctrl) {
		
		super(ctrl.getContainer());
		
		// store controller reference
		this.ctrl = ctrl;
		
		//dialog = new JDialog();
		setTitle("Working...");
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(5,5,5,5);
		add(lblStatus = new JLabel(), c);
		c.gridy++;
		c.weighty = 1.5;
		add(progress = new JProgressBar(0,1000), c);
		c.gridy++;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.NONE;
		add(btnAbort = new JButton("Abort"),c);
		
		btnAbort.addActionListener(this);
		

		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setResizable(false);
		pack();
		setSize(220,120);
		setVisible(true);
		
	}
	
	public void setStatusText(final String text) {
		Runnable doSetProgressBarValue = new Runnable() {
            public void run() {
            	lblStatus.setText(text);
            }
        };
        SwingUtilities.invokeLater(doSetProgressBarValue);	
	}
	

	public void close() {
		this.setVisible(false);
		try {
			this.dispose();
		} catch (Throwable e) {
			// don't do anything
		}
	}
	
	
	// callback function
	public void dotmatrixCalcStatus(final float percent) {
		Runnable doSetProgressBarValue = new Runnable() {
            public void run() {
            	progress.setValue((int)(percent*10));
            }
        };
        SwingUtilities.invokeLater(doSetProgressBarValue);	
		
	}
	
	// callback function
	public int tellCallbackStep(int wordlen, int windowsize) {
		
		if (wordlen != 0)
			return 100000;
		else
			return 200;

	}

	public void actionPerformed(ActionEvent arg0) {
		// cancel now!
		ctrl.abortDotplot();
		// set text
		setStatusText("Aborting, please wait...");
	}

	public void tellAborted() {
	}

}
