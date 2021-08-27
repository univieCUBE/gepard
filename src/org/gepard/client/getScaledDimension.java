package org.gepard.client;

import java.awt.Dimension;

public class getScaledDimension{

	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
	  int new_width = imgSize.width;
	  int new_height = imgSize.height;
	  if (imgSize.width > boundary.width) {
	    new_width = boundary.width;
	    new_height = (new_width * imgSize.height) / imgSize.width;
	  }
	  if (new_height > boundary.height) {
	    new_height = boundary.height;

	    new_width = (new_height * imgSize.width) / imgSize.height;
	  }
	  return new Dimension(new_width, new_height);
	}
}
