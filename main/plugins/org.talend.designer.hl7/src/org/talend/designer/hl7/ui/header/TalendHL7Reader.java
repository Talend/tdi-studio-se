// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.header;

import java.io.IOException;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class TalendHL7Reader implements ca.uhn.hl7v2.llp.HL7Reader {

    private java.io.BufferedReader myReader;

    private char endMsg = '\u001c'; // character indicating the

    private char startMsg = '\u000b';// character indicating the

    public TalendHL7Reader(java.io.InputStream in, String charsetName) throws IOException {
        setInputStream(in, charsetName);
    }

    public void setEndMsgChar(char endMsg) {
        this.endMsg = endMsg;
    }

    public void setStartMsgChar(char startMsg) {
        this.startMsg = startMsg;
    }

    public synchronized void setInputStream(java.io.InputStream in, String charsetName) throws IOException {
        myReader = new java.io.BufferedReader(new java.io.InputStreamReader(in, charsetName));
    }

    public void setInputStream(java.io.InputStream arg0) throws IOException {
    }

    private boolean isFirst = true;

    public synchronized String getMessage() throws java.io.IOException {
        StringBuffer s_buffer = new StringBuffer();

        boolean end_of_message = false;

        int c = 0;
        try {
            do {
                c = myReader.read();
            } while (c == ' ' || c == '\t' || c == '\r' || c == '\n');
        } catch (Exception e) {
            return null;
        }

        // trying to read when there is no data (stream may have
        // been closed at other end)
        if (c == -1) {
            return null;
        }

        if (c != startMsg && !isFirst) {
            // throw new java.io.IOException("no start of message indicator was found.");
            return null;
        }

        if (isFirst && c != startMsg) {
            s_buffer.append((char) c);
        }

        while (!end_of_message) {
            c = myReader.read();

            if (c == -1) {
                break;
            }

            if (c == endMsg) {
                end_of_message = true;
            } else {
                s_buffer.append((char) c);
            }
        } // end while
        isFirst = false;
        if (s_buffer.length() > 0) {
            return s_buffer.toString();
        } else {
            return null;
        }
    }

    /**
     * Closes the underlying BufferedReader.
     */
    public synchronized void close() throws java.io.IOException {
        myReader.close();
    }
}
