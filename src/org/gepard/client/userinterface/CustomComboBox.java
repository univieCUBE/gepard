package org.gepard.client.userinterface;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComboBox;

// a combo box whose dropdown list width adapts to its contents
// (quite dirty solution)

public class CustomComboBox extends JComboBox {

	private static final long serialVersionUID = 5298990683390051916L;

	private boolean realSize;
	
	private int maxitemwidth=0;

	public Dimension getSize() {
		Dimension size = super.getSize();
		if(realSize)
			return size;
		else {

			if (maxitemwidth > size.width)
				size.width = maxitemwidth;
			
			return size;
		}
	}

	public void doLayout() {
		realSize = true;
		super.doLayout();
		realSize = false;
	}
	
	public void adaptPopupWidth() {
		// create graphics dummy & font metrics
		Graphics2D g = (Graphics2D)(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).createGraphics());
		g.setFont(this.getFont());
		FontMetrics fontMetrics = g.getFontMetrics(this.getFont());
		// iterate over list & calc max item width
		int max=0;
		for (int i=0; i<getItemCount(); i++) {
			String curitem = (String)getItemAt(i);
			if (curitem != null && curitem.length() > 0) {
				int curwidth = fontMetrics.stringWidth(curitem);
				if (curwidth > max) max = curwidth;
			}
		}
		
		maxitemwidth = max+22;	

	}
}
