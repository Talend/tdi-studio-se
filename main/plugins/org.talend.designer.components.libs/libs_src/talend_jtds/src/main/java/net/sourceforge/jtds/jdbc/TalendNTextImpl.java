package net.sourceforge.jtds.jdbc;

import java.io.IOException;
import java.sql.SQLException;

import net.sourceforge.jtds.util.BlobBuffer;

public class TalendNTextImpl {

    ClobImpl clobImpl;

    public TalendNTextImpl(ClobImpl clobImpl) {
        this.clobImpl = clobImpl;
    }

    public String getValue() throws SQLException, IOException {
        BlobBuffer blobBuffer = clobImpl.getBlobBuffer();
        int length = Integer.parseInt(String.valueOf(blobBuffer.getLength()));
        byte[] b = blobBuffer.getBytes(1L, length);
        return readUnicodeString(b, length / 2);
    }

    /**
     * Reads a UCS2-LE (Unicode) encoded String object from the server response stream.
     * 
     * @param buffer the content of the string to read
     * @param len the length of the string to read <b>in characters</b>
     * @return the result as a <code>String</code>
     * @throws IOException if an I/O error occurs
     */
    public static String readUnicodeString(byte[] buffer, int len) throws IOException {
        char[] chars = new char[len];
        int bufferPos = 0;

        for (int i = 0; i < len; i++) {

            int b1 = buffer[bufferPos++] & 0xFF;

            int b2 = buffer[bufferPos++] << 8;

            chars[i] = (char) (b2 | b1);
        }

        return new String(chars, 0, len);
    }
}