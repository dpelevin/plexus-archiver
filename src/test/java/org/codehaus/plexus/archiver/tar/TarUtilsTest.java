package org.codehaus.plexus.archiver.tar;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Arrays;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * @author Dmitry Pelevin
 * @version $Id$
 */
public class TarUtilsTest
    extends PlexusTestCase
{

    private Logger logger;

    public void setUp()
        throws Exception
    {
        super.setUp();

        logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "test" );
    }

    public void testNameParsing()
        throws Exception
    {
        int offset = 1;
        byte bufferExistingData = 1;
        String name = "test";
        byte[] nameBytesReference = new byte[TarConstants.NAMELEN];
        nameBytesReference[0] = 116;
        nameBytesReference[1] = 101;
        nameBytesReference[2] = 115;
        nameBytesReference[3] = 116;
        byte[] nameBytesReference2 = new byte[TarConstants.NAMELEN + offset];
        nameBytesReference2[0] = bufferExistingData;
        nameBytesReference2[1] = 116;
        nameBytesReference2[2] = 101;
        nameBytesReference2[3] = 115;
        nameBytesReference2[4] = 116;

        StringBuffer referenceNameBuffer = new StringBuffer( name );
        byte[] convertedNameBytes = new byte[TarConstants.NAMELEN];
        byte[] convertedNameBytes2 = new byte[TarConstants.NAMELEN + offset];
        Arrays.fill( convertedNameBytes, bufferExistingData );
        Arrays.fill( convertedNameBytes2, bufferExistingData );

        TarUtils.getNameBytes( referenceNameBuffer, convertedNameBytes, 0, TarConstants.NAMELEN );
        TarUtils.getNameBytes( referenceNameBuffer, convertedNameBytes2, offset, TarConstants.NAMELEN );
        assertTrue( Arrays.equals( nameBytesReference, convertedNameBytes ) );
        assertTrue( Arrays.equals( nameBytesReference2, convertedNameBytes2 ) );

        StringBuffer parsedName = TarUtils.parseName( convertedNameBytes, 0, TarConstants.NAMELEN );
        StringBuffer parsedName2 = TarUtils.parseName( convertedNameBytes2, offset, TarConstants.NAMELEN );

        assertEquals( referenceNameBuffer.toString(), parsedName.toString() );
        assertEquals( referenceNameBuffer.toString(), parsedName2.toString() );
    }

    public void testNameParsingWithEncoding()
        throws Exception
    {
        byte bufferExistingData = 1;
        String encoding = "CP866";
        String name = "тест";
        byte[] nameBytesReference = new byte[TarConstants.NAMELEN];
        nameBytesReference[0] = -30;
        nameBytesReference[1] = -91;
        nameBytesReference[2] = -31;
        nameBytesReference[3] = -30;

        StringBuffer referenceNameBuffer = new StringBuffer( name );
        byte[] convertedNameBytes = new byte[TarConstants.NAMELEN];
        Arrays.fill( convertedNameBytes, bufferExistingData );

        TarUtils.getNameBytes( referenceNameBuffer, convertedNameBytes, 0, TarConstants.NAMELEN, encoding );
        assertTrue( Arrays.equals( nameBytesReference, convertedNameBytes ) );

        StringBuffer parsedName = TarUtils.parseName( convertedNameBytes, 0, TarConstants.NAMELEN, encoding );

        assertEquals( referenceNameBuffer.toString(), parsedName.toString() );
    }

    public void testOctalConversions()
        throws Exception
    {
        int offset = 1;
        long referencePermissionMask100640 = 33184;
        byte[] referencePermissionBytes = { 49, 48, 48, 54, 52, 48, 32, 0 };
        byte[] referencePermissionBytes2 = { 0, 49, 48, 48, 54, 52, 48, 32, 0 };
        long referenceSize = 100;
        byte[] referenceSizeBytes = { 32, 32, 32, 32, 49, 52, 52, 32, 0, 0, 0, 0 };
        byte[] referenceSizeBytes2 = { 0, 32, 32, 32, 32, 49, 52, 52, 32, 0, 0, 0, 0 };

        long result = TarUtils.parseOctal( referencePermissionBytes, 0, TarConstants.MODELEN );
        long result2 = TarUtils.parseOctal( referencePermissionBytes2, offset, TarConstants.MODELEN );

        assertEquals( referencePermissionMask100640, result );
        assertEquals( referencePermissionMask100640, result2 );

        byte[] resultPermissionBytes = new byte[TarConstants.MODELEN];
        byte[] resultSizeBytes = new byte[TarConstants.SIZELEN];
        byte[] resultPermissionBytes2 = new byte[TarConstants.MODELEN + offset];
        byte[] resultSizeBytes2 = new byte[TarConstants.SIZELEN + offset];
        TarUtils.getOctalBytes( referencePermissionMask100640, resultPermissionBytes, 0, TarConstants.MODELEN );
        TarUtils.getLongOctalBytes( referenceSize, resultSizeBytes, 0, TarConstants.MODELEN );
        TarUtils.getOctalBytes( referencePermissionMask100640, resultPermissionBytes2, offset, TarConstants.MODELEN );
        TarUtils.getLongOctalBytes( referenceSize, resultSizeBytes2, offset, TarConstants.MODELEN );

        assertTrue( Arrays.equals( referencePermissionBytes, resultPermissionBytes ) );
        assertTrue( Arrays.equals( referenceSizeBytes, resultSizeBytes ) );
        assertTrue( Arrays.equals( referencePermissionBytes2, resultPermissionBytes2 ) );
        assertTrue( Arrays.equals( referenceSizeBytes2, resultSizeBytes2 ) );
    }

    public void testCheckSum()
        throws Exception
    {
        int offset = 1;
        byte[] referenceDataBytes = { 1, 2, 3 };
        long referenceCheckSum = 6;
        byte[] referenceCheckSumBytes = { 32, 32, 32, 32, 32, 54, 0, 32 };
        byte[] referenceCheckSumBytes2 = { 0, 32, 32, 32, 32, 32, 54, 0, 32 };

        long checkSum = TarUtils.computeCheckSum( referenceDataBytes );
        assertEquals( referenceCheckSum, checkSum );

        byte[] checkSumBytes = new byte[TarConstants.CHKSUMLEN];
        byte[] checkSumBytes2 = new byte[TarConstants.CHKSUMLEN + offset];

        TarUtils.getCheckSumOctalBytes( checkSum, checkSumBytes, 0, TarConstants.CHKSUMLEN );
        TarUtils.getCheckSumOctalBytes( checkSum, checkSumBytes2, offset, TarConstants.CHKSUMLEN );
        assertTrue( Arrays.equals( referenceCheckSumBytes, checkSumBytes ) );
        assertTrue( Arrays.equals( referenceCheckSumBytes2, checkSumBytes2 ) );
    }

}
