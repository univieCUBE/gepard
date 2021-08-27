package org.gepard.common;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <pre>
 *       LEDataOutputStream.java
 * <p/>
 *        copyright (c) 1998-2006 Roedy Green,
 *        Canadian Mind Products
 *       #101 - 2536 Wark Street
 *       Victoria,  BC Canada V8T 4G8
 *       hel: (250) 361-9093
 *       mailto:roedyg@mindprod.com
 *       http://mindprod.com
 * <p/>
 *        Version 1.0 1998 January 6
 * <p/>
 *       1.1 1998 January 7 -officially implements DataInput
 * <p/>
 *       1.2 1998 January 9 - add LERandomAccessFile
 * <p/>
 *       1.3 1998 August 28 1.4 1998 November 10 - add new address and phone.
 * <p/>
 *       1.5 1999 October 8 - use com.mindprod.ledatastream
 *       package name. Very similar to DataOutputStream except it writes little-endian
 *       instead of big-endian binary data. We can't extend DataOutputStream directly
 *       since it has only final methods. This forces us implement LEDataOutputStream
 *       with a DataOutputStream object, and use wrapper methods.
 * </pre>
 */

public class LEDataOutputStream implements DataOutput {

    // ------------------------------ FIELDS ------------------------------

    /**
     * work array for composing output
     */
    byte w[];

    /**
     * to get at big-Endian write methods of DataOutPutStream
     */
    protected DataOutputStream d;

    // -------------------------- STATIC METHODS --------------------------

    /**
     * Embeds copyright notice
     *
     * @return copyright notice
     */
    public static final String getCopyright()
        {
        return "LeDataStream 1.7 freeware copyright (c) 1998-2006 Roedy Green, Canadian Mind Products, http://mindprod.com roedyg@mindprod.com";
        }

    // --------------------------- CONSTRUCTORS ---------------------------

    /**
     * constructor
     *
     * @param out the outputstream we ware to write little endian binary data onto
     */
    public LEDataOutputStream( OutputStream out )
        {
        this.d = new DataOutputStream( out );
        w = new byte[ 8 ]; // work array for composing output
        }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface DataOutput ---------------------

    /**
     * This method writes only one byte, even though it says int (non-Javadoc)
     *
     * @inheritDoc
     * @see java.io.DataOutput#write(int)
     */
    public final synchronized void write( int b ) throws IOException
        {
        d.write( b );
        }

    /**
     * @inheritDoc
     * @see java.io.DataOutput#write(byte[])
     */
    public final void write( byte b[] ) throws IOException
        {
        d.write( b, 0, b.length );
        }

    /**
     * @inheritDoc
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    public final synchronized void write( byte b[],
                                          int off,
                                          int len ) throws IOException
        {
        d.write( b, off, len );
        }

    /**
     * @inheritDoc
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    /* Only writes one byte */
    public final void writeBoolean( boolean v ) throws IOException
        {
        d.writeBoolean( v );
        }

    // p u r e l y w r a p p e r m e t h o d s
    // We cannot inherit since DataOutputStream is final.

    /**
     * @inheritDoc
     * @see java.io.DataOutput#writeByte(int)
     */
    public final void writeByte( int v ) throws IOException
        {
        d.writeByte( v );
        }

    /**
     * like DataOutputStream.writeShort. also acts as a writeUnsignedShort
     *
     * @param v the short you want written in little endian binary format
     *
     * @throws IOException
     * @inheritDoc
     */
    public final void writeShort( int v ) throws IOException
        {
        w[ 0 ] = (byte) v;
        w[ 1 ] = (byte) ( v >> 8 );
        d.write( w, 0, 2 );
        }

    /**
     * like DataOutputStream.writeChar. Note the parm is an int even though this
     * as a writeChar
     *
     * @param v
     *
     * @inheritDoc
     */
    public final void writeChar( int v ) throws IOException
        {
        // same code as writeShort
        w[ 0 ] = (byte) v;
        w[ 1 ] = (byte) ( v >> 8 );
        d.write( w, 0, 2 );
        }

    /**
     * like DataOutputStream.writeInt.
     *
     * @param v
     *
     * @throws IOException
     * @inheritDoc
     */
    public final void writeInt( int v ) throws IOException
        {
        w[ 0 ] = (byte) v;
        w[ 1 ] = (byte) ( v >> 8 );
        w[ 2 ] = (byte) ( v >> 16 );
        w[ 3 ] = (byte) ( v >> 24 );
        d.write( w, 0, 4 );
        }

    /**
     * like DataOutputStream.writeLong.
     *
     * @param v
     *
     * @throws IOException
     * @inheritDoc
     */
    public final void writeLong( long v ) throws IOException
        {
        w[ 0 ] = (byte) v;
        w[ 1 ] = (byte) ( v >> 8 );
        w[ 2 ] = (byte) ( v >> 16 );
        w[ 3 ] = (byte) ( v >> 24 );
        w[ 4 ] = (byte) ( v >> 32 );
        w[ 5 ] = (byte) ( v >> 40 );
        w[ 6 ] = (byte) ( v >> 48 );
        w[ 7 ] = (byte) ( v >> 56 );
        d.write( w, 0, 8 );
        }

    /**
     * like DataOutputStream.writeFloat.
     *
     * @inheritDoc
     */
    public final void writeFloat( float v ) throws IOException
        {
        writeInt( Float.floatToIntBits( v ) );
        }

    /**
     * like DataOutputStream.writeDouble.
     *
     * @inheritDoc
     */
    public final void writeDouble( double v ) throws IOException
        {
        writeLong( Double.doubleToLongBits( v ) );
        }

    /**
     * @inheritDoc
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    public final void writeBytes( String s ) throws IOException
        {
        d.writeBytes( s );
        }

    /**
     * like DataOutputStream.writeChars, flip each char.
     *
     * @inheritDoc
     */
    public final void writeChars( String s ) throws IOException
        {
        int len = s.length();
        for ( int i = 0; i < len; i++ )
            {
            writeChar( s.charAt( i ) );
            }
        } // end writeChars

    /**
     * @inheritDoc
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    public final void writeUTF( String str ) throws IOException
        {
        d.writeUTF( str );
        }

    // -------------------------- OTHER METHODS --------------------------

    // L I T T L E E N D I A N W R I T E R S
    // Little endian methods for multi-byte numeric types.
    // Big-endian do fine for single-byte types and strings.
    /**
     * @throws IOException
     */
    public final void close() throws IOException
        {
        d.close();
        }

    // DataOutputStream

    /**
     * @throws IOException
     */
    public void flush() throws IOException
        {
        d.flush();
        }

    /**
     * @return bytes written so far in the stream. Note this is a int, not a
     *         long as you would exect. This because the underlying
     *         DataInputStream has a design flaw.
     */
    public final int size()
        {
        return d.size();
        }
} // end LEDataOutputStream
