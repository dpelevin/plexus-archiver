package org.codehaus.plexus.archiver.tar;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/*
 * Copyright  2000,2002,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/*
 * This package is based on the work done by Timothy Gerard Endres
 * (time@ice.com) to whom the Ant project is very grateful for his great code.
 */

/**
 * This class provides static utility methods to work with byte streams.
 *
 * @version $Revision$ $Date$
 *          from org.apache.ant.tools.tar.TarUtils v1.11
 */
public class TarUtils
{

    /**
     * Parse an octal string from a header buffer. This is used for the
     * file permission mode value.
     *
     * @param header The header buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The long value of the octal string.
     */
    public static long parseOctal( byte[] header, int offset, int length )
    {
        long result = 0;
        boolean stillPadding = true;
        int end = offset + length;

        for ( int i = offset; i < end; ++i )
        {
            if ( header[ i ] == 0 )
            {
                break;
            }

            if ( header[ i ] == (byte) ' ' || header[ i ] == '0' )
            {
                if ( stillPadding )
                {
                    continue;
                }

                if ( header[ i ] == (byte) ' ' )
                {
                    break;
                }
            }

            stillPadding = false;
            result = ( result << 3 ) + ( header[ i ] - '0' );
        }

        return result;
    }
    
    /**
     * Parse an entry name from a header buffer.
     *
     * @param header The header buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The header's entry name.
     */
    public static StringBuffer parseName( byte[] header, int offset, int length )
    {
        return parseName( header, offset, length, null );
    }

    /**
     * Parse an entry name from a header buffer.
     *
     * @param header The header buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @param encoding The encoding to use for filenames.
     * @return The header's entry name.
     */
    public static StringBuffer parseName( byte[] header, int offset, int length, String encoding )
    {
        StringBuffer result = new StringBuffer( length );
        int end = offset + length;

        int i;
        for ( i = offset; i < end; ++i )
        {
            if ( header[ i ] == 0 )
            {
                break;
            }
        }
        byte[] nameBytes = Arrays.copyOfRange( header, offset, i );
        try
        {
            if ( encoding == null )
            {
                result.append( new String( nameBytes ) );
            }
            else
            {
                result.append( new String( nameBytes, encoding ) );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    /**
     * Determine the number of bytes in an entry name.
     *
     * @param name   The header name from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The number of bytes in a header's entry name.
     */
    public static int getNameBytes( StringBuffer name, byte[] buf, int offset, int length )
    {
        return getNameBytes( name, buf, offset, length, null );
    }

    /**
     * Determine the number of bytes in an entry name.
     *
     * @param name   The header name from which to parse.
     * @param buf the buf
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @param encoding The encoding to use for filenames.
     * @return The number of bytes in a header's entry name.
     */
    public static int getNameBytes( StringBuffer name, byte[] buf, int offset, int length, String encoding )
    {
        int i;
        byte[] nameBytes;
        if ( encoding == null )
        {
            nameBytes = name.toString().getBytes();
        }
        else
        {
            try
            {
                nameBytes = name.toString().getBytes(encoding);
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException(e);
            }
        }
        for ( i = 0; i < length && i < nameBytes.length; ++i )
        {
            buf[ offset + i ] = nameBytes[i];
        }

        for ( ; i < length; ++i )
        {
            buf[ offset + i ] = 0;
        }

        return offset + length;
    }

    /**
     * Parse an octal integer from a header buffer.
     *
     * @param value  The header value
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The integer value of the octal bytes.
     */
    public static int getOctalBytes( long value, byte[] buf, int offset, int length )
    {
        int idx = length - 1;

        buf[ offset + idx ] = 0;
        --idx;
        buf[ offset + idx ] = (byte) ' ';
        --idx;

        if ( value == 0 )
        {
            buf[ offset + idx ] = (byte) '0';
            --idx;
        }
        else
        {
            for ( long val = value; idx >= 0 && val > 0; --idx )
            {
                buf[ offset + idx ] = (byte) ( (byte) '0' + (byte) ( val & 7 ) );
                val = val >> 3;
            }
        }

        for ( ; idx >= 0; --idx )
        {
            buf[ offset + idx ] = (byte) ' ';
        }

        return offset + length;
    }

    /**
     * Parse an octal long integer from a header buffer.
     *
     * @param value  The header value
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The long value of the octal bytes.
     */
    public static int getLongOctalBytes( long value, byte[] buf, int offset, int length )
    {
        byte[] temp = new byte[length + 1];

        getOctalBytes( value, temp, 0, length + 1 );
        System.arraycopy( temp, 0, buf, offset, length );

        return offset + length;
    }

    /**
     * Parse the checksum octal integer from a header buffer.
     *
     * @param value  The header value
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The integer value of the entry's checksum.
     */
    public static int getCheckSumOctalBytes( long value, byte[] buf, int offset, int length )
    {
        getOctalBytes( value, buf, offset, length );

        buf[ offset + length - 1 ] = (byte) ' ';
        buf[ offset + length - 2 ] = 0;

        return offset + length;
    }

    /**
     * Compute the checksum of a tar entry header.
     *
     * @param buf The tar entry's header buffer.
     * @return The computed checksum.
     */
    public static long computeCheckSum( byte[] buf )
    {
        long sum = 0;

        for ( int i = 0; i < buf.length; ++i )
        {
            sum += 255 & buf[ i ];
        }

        return sum;
    }
}
