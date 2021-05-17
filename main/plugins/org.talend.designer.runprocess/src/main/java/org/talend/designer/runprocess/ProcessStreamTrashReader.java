// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.talend.core.model.utils.ProcessStreamTrashReaderUtil;

/**
 * Read streams of a process. <br/>
 *
 * $Id$
 *
 */
public final class ProcessStreamTrashReader {

    /**
     * Constructs a new ProcessStreamTrashReader.
     */
    private ProcessStreamTrashReader() {
        super();
    }

    /**
     *
     * DOC YeXiaowei Comment method "readErrorStream".
     *
     * @param process
     * @return
     */
    public static String readErrorStream(final Process process) {
        if (true) {
            /*
             * have changed and moved to class ProcessStreamTrashReaderUtil. bug 9735
             */
            return ProcessStreamTrashReaderUtil.readErrorStream(process);
        }
        String lineSep = System.getProperty("line.separator"); //$NON-NLS-1$

        StringBuilder builder = new StringBuilder();

        InputStream is = process.getErrorStream();
        InputStreamReader din = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(din);
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(lineSep);
            }
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }

        if (builder.toString().equals("")) { //$NON-NLS-1$
            return null;
        }

        return builder.toString();
    }

    public static void readAndForget(final Process process) {
        if (true) {
            /*
             * have moved to class ProcessStreamTrashReaderUtil, bug 9735
             */
            ProcessStreamTrashReaderUtil.readAndForget(process);
            return;
        }
        try {
            new Thread() {

                public void run() {
                    InputStream is = process.getInputStream();
                    InputStreamReader din = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(din);
                    try {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("getInputStream " + line); //$NON-NLS-1$
                        }
                    } catch (Exception ex) {
                        ex.getMessage();
                    } finally {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }.start();

            // int len = is.available();
            // if (len > 0) {
            // byte[] data = new byte[len];
            // is.read(data);
            // }

            new Thread() {

                public void run() {
                    InputStream is = process.getErrorStream();
                    InputStreamReader din = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(din);
                    try {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("getErrorStream " + line); //$NON-NLS-1$
                        }
                    } catch (Exception ex) {
                        ex.getMessage();
                    } finally {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }.start();

            // len = is.available();
            // if (len > 0) {
            // byte[] data = new byte[len];
            // is.read(data);
            // }

            boolean stopped = false;
            while (!stopped) {
                try {
                    process.exitValue();
                    stopped = true;
                } catch (IllegalThreadStateException itse) {
                    // Do nothing
                }
            }
        } catch (Exception ioe) {// IO
            // Do nothing
        }
    }
}
