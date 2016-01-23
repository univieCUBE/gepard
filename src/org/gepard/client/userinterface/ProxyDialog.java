package org.gepard.client.userinterface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.gepard.client.Config;
import org.gepard.client.Controller;

// GUI dialog for setting HTTP proxy data

public class ProxyDialog extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1809215642400993530L;
	
	private JButton btnOK, btnCancel;
	private JTextField txtHost, txtPort;
	private Controller ctrl;
	
	public ProxyDialog(Controller ctrl) {
		super("HTTP proxy settings");
		
		this.ctrl = ctrl;
		
		setSize(new Dimension(280,140));
		setupGUI();
		//pack();
		
		setVisible(true);
		setResizable(false);
		// get containers position and center dialog
		WindowPos cpos = ctrl.getContainer().getPosition();
		setLocation(
			cpos.x + (cpos.width / 2) - (getWidth()/2),
			cpos.y + (cpos.height / 2) - (getHeight()/2)
		);
		
		// load values
		txtHost.setText(Config.getInstance().getStringVal("proxy_host",""));
		String port = Config.getInstance().getStringVal("proxy_port","");
		if (port.trim().equals(""))
			port = "8080";
		txtPort.setText(port);
	
	}
	
	private void setupGUI() {
		
		HelpTexts hlp = HelpTexts.getInstance();
			
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints(); 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		
		JLabel spacer1, spacer2, spacer3, spacer4;
		
		add(spacer1 = new JLabel(""),c);
		c.gridx++;
		add(spacer2 = new JLabel(""),c);
		c.gridx++;
		add(spacer3 = new JLabel(""),c);
		c.gridx++;
		add(spacer4 = new JLabel(""),c);
		
		c.insets = new Insets(10,6,6,6);
		c.gridy++; c.gridx=0;
		add(new JLabel("Host:"),c);
		c.gridx++; c.gridwidth = 3;
		add(txtHost = new JTextField("",10000),c);
		c.gridy++; c.gridx=0; c.gridwidth = 1;
		add(new JLabel("Port:"),c);
		c.gridx++; c.gridwidth = 3;
		c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST;
		add(txtPort = new JTextField(""),c); 
		c.gridx=0;c.gridy++; c.gridwidth = 1; c.insets = new Insets(6,6,6,6);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel(""),c);
		c.gridx++;
		add(new JLabel(""),c);
		c.gridx++;
		add(btnOK = new JButton("OK"),c);
		c.gridx++; 
		add(btnCancel = new JButton("Cancel"),c);
		
		
		spacer1.setPreferredSize(new Dimension(1,1));
		spacer2.setPreferredSize(new Dimension(1,1));
		
		spacer3.setPreferredSize(new Dimension(85,1));
		spacer3.setMinimumSize(new Dimension(85,1));
		spacer3.setMaximumSize(new Dimension(85,1));

		spacer4.setPreferredSize(new Dimension(5,1));
		spacer4.setMinimumSize(new Dimension(5,1));
		spacer4.setMaximumSize(new Dimension(5,1));
		
		txtPort.setMinimumSize(new Dimension(50,20));
		
		// insert stretcher
		c.gridy++;
		c.weighty = 10000;
		c.gridwidth = 1;
		add(new JLabel(),c);
		
		// help texts
		txtHost.setToolTipText(hlp.getHelpText("proxyhost"));
		txtPort.setToolTipText(hlp.getHelpText("proxyport"));
		btnOK.setToolTipText(hlp.getHelpText("okbutton"));
		btnCancel.setToolTipText(hlp.getHelpText("cancelbutton"));
		
		
		// listeners
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);
		
	}

	public void actionPerformed(ActionEvent evt) {
		
		if (evt.getSource() == btnOK) {
			// write to config
			Config.getInstance().setVal("proxy_host", txtHost.getText());
			Config.getInstance().setVal("proxy_port", txtPort.getText());
			// update system proxy settings
			ctrl.updateProxySettings(false);
			
		} 
		
		// close dialog
		this.dispose();
		
	}

}
