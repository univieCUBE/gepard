package org.gepard.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


// this class handles compression and decompression by 
// utilizing Javas Deflater & Inflater class

public class Compression {
	
	public static byte[] compressByteArray(byte[] input) {
    
		// create the compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
    
		// 	give the compressor the data to compress
		compressor.setInput(input);
		compressor.finish();
    
		// create an expandable byte array to hold the compressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
    
	    // compress the data
	    byte[] buf = new byte[1024];
	    while (!compressor.finished()) {
	        int count = compressor.deflate(buf);
	        bos.write(buf, 0, count);
	    }
	    try {
	        bos.close();
	    } catch (IOException e) {
	    }
    
	    //return the compressed data
	    return bos.toByteArray();
	}
	
	public static byte[] decompressByteArray(byte[] input) {
	   
		// ceate the decompressor and give it the data to compress
	    Inflater decompressor = new Inflater( );
	    decompressor.setInput(input);
	    
	    // create an expandable byte array to hold the decompressed data
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
	    
	    // decompress the data
	    byte[] buf = new byte[1024];
	    while (!decompressor.finished()) {
	        try {
	            int count = decompressor.inflate(buf);
	            bos.write(buf, 0, count);
	        } catch (DataFormatException e) {
	        	e.printStackTrace();
	        	System.exit(0);
	        }
	    }
	    try {
	        bos.close();
	    } catch (IOException e) {
	    }
	    
	    // return decompressed data
	    return bos.toByteArray();
	}

}
