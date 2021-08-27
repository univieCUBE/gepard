package org.gepard.client.cmdline;

import java.awt.Dimension;
import java.io.File;

import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.gepard.client.Plotter;
import org.gepard.client.getScaledDimension;

public class CommandLineImageSave{

public static void ExportImage(String file, String format, Plotter p, Integer n) {
	try {
		// export image
		//pdf
		if (format == null) {format = "png";}
		if ( format.toLowerCase().indexOf("pdf") != -1 ) {  
			File efile = new File(file);
			boolean exists = efile.exists();
			//if file exists, it appends a new page else it creates a new file
			if (exists) {
				//Dimensions of page for resizing
				int actualPDFWidth = 0;
				int actualPDFHeight = 0;
			    actualPDFWidth = (int) PDRectangle.A4.getWidth();
			    actualPDFHeight = (int) PDRectangle.A4.getHeight();
			    //creating image
				ImageIO.write(p.getFullImage(), "jpeg", new File(file.replace(".pdf", ".jpeg")));
				//opening file
				File pdfile = new File(file);
				PDDocument doc = PDDocument.load(pdfile);
				//adding page
				PDPage page = new PDPage();
				doc.addPage( page );
				//image insertion and resizing
				PDImageXObject pdImage = PDImageXObject.createFromFile(file.replace(".pdf",".jpeg"), doc);
				PDPageContentStream contentStream = new PDPageContentStream(doc, page);// scale image
				Dimension scaledDim = getScaledDimension.getScaledDimension(new Dimension(pdImage.getWidth(), pdImage.getHeight()), new Dimension(actualPDFWidth, actualPDFHeight));
				int x = ((int) PDRectangle.A4.getWidth() - scaledDim.width) / 2;
			    int y = ((int) PDRectangle.A4.getHeight() - scaledDim.height) / 2;
			    contentStream.drawImage(pdImage, x, y, scaledDim.width, scaledDim.height);
				contentStream.close();
				//saving and image file since it isnt needed anymore
				doc.save(file);
				File to_delete = new File(file.replace(".pdf", ".jpeg"));
				to_delete.delete();
				doc.close();
			}else {
				//Dimensions of page for resizing
				int actualPDFWidth = 0;
				int actualPDFHeight = 0;
			    actualPDFWidth = (int) PDRectangle.A4.getWidth();
			    actualPDFHeight = (int) PDRectangle.A4.getHeight();
			    //creating image
				ImageIO.write(p.getFullImage(), "jpeg", new File(file.replace(".pdf", ".jpeg")));
				//Creating PDF document object 
				PDDocument document = new PDDocument();
				//adding page
				PDPage blankPage = new PDPage();
				document.addPage( blankPage );
				PDPage page = document.getPage(0);
				//image insertion and resizing
				PDImageXObject pdImage = PDImageXObject.createFromFile(file.replace(".pdf", ".jpeg"), document);
				PDPageContentStream contentStream = new PDPageContentStream(document, page);// scale image
				Dimension scaledDim = getScaledDimension.getScaledDimension(new Dimension(pdImage.getWidth(), pdImage.getHeight()), new Dimension(actualPDFWidth, actualPDFHeight));
				int x = ((int) PDRectangle.A4.getWidth() - scaledDim.width) / 2;
			    int y = ((int) PDRectangle.A4.getHeight() - scaledDim.height) / 2;
			    contentStream.drawImage(pdImage, x, y, scaledDim.width, scaledDim.height);
				contentStream.close();
				//Saving the document
				document.save(file);
				File to_delete = new File(file.replace(".pdf", ".jpeg"));
				to_delete.delete();
				//Closing the document  
				document.close();
			}
			   }  
		if (format != "pdf"){
		ImageIO.write(p.getFullImage(), format, new File(file.replace("." + format, n + "." + format)));
		}
		// show success message
	} catch (Exception e) {System.err.println(e);
	}
}
}
