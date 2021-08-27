package org.gepard.client;

import java.awt.Dimension;

// TODO remove this interface => better implementation of Plotter class
public interface DrawTarget {

	void setPreferredSize(Dimension dimension);

	int getWidth();

	int getHeight();
	

}
