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
package org.talend.designer.runprocess.data;

/**
 * Describes process trace data. <br/>
 *
 * $Id$
 *
 */
public final class TraceData implements IData {

    /** Field separator. */
    private static final String FIELD_SEP = "|"; //$NON-NLS-1$

    /** Action : trace report. */
    public static final String ACTION_TRACE = "trace"; //$NON-NLS-1$

    /** Action : process stoped. */
    public static final String ACTION_STOP = "stop"; //$NON-NLS-1$

    /** Action : process started. */
    public static final String ACTION_START = "start"; //$NON-NLS-1$

    /** Data string to be parsed. */
    private String data;

    /**
     * Constructs a new traceData.
     *
     * @param data Data string to be parsed.
     */
    public TraceData(final String data) {
        super();

        this.data = data;
    }

    public String getElementId() {
        int sepIndex = data.indexOf(FIELD_SEP);
        return sepIndex != -1 ? data.substring(0, sepIndex) : null;
    }

    public long getLineCount() {
        int sepIndex1 = data.indexOf(FIELD_SEP);
        int sepIndex2 = sepIndex1 != -1 ? data.indexOf(FIELD_SEP, sepIndex1 + 1) : -1;
        long lineCount;
        if (sepIndex2 != -1) {
            String lineCountStr = data.substring(sepIndex1 + 1, sepIndex2);
            try {
                lineCount = Long.parseLong(lineCountStr);
            } catch (NumberFormatException nfe) {
                lineCount = 0;
            }
        } else {
            lineCount = 0;
        }
        return lineCount;
    }

    public long getProcessingTime() {
        int sepIndex1 = data.indexOf(FIELD_SEP);
        int sepIndex2 = sepIndex1 != -1 ? data.indexOf(FIELD_SEP, sepIndex1 + 1) : -1;
        int sepIndex3 = sepIndex2 != -1 ? data.indexOf(FIELD_SEP, sepIndex2 + 1) : -1;
        if (sepIndex3 == -1 && sepIndex2 != -1) {
            sepIndex3 = data.length();
        }
        long time;
        if (sepIndex3 != -1) {
            String timeStr = data.substring(sepIndex2 + 1, sepIndex3);
            try {
                time = Long.parseLong(timeStr);
            } catch (NumberFormatException nfe) {
                time = 0;
            }
        } else {
            time = 0;
        }
        return time;
    }

    public String getAction() {
        int sepIndex1 = data.indexOf(FIELD_SEP);
        int sepIndex2 = sepIndex1 != -1 ? data.indexOf(FIELD_SEP, sepIndex1 + 1) : -1;
        int sepIndex3 = sepIndex2 != -1 ? data.indexOf(FIELD_SEP, sepIndex2 + 1) : -1;
        String action = sepIndex3 != -1 ? data.substring(sepIndex3 + 1, data.length()) : ACTION_TRACE;
        return action;
    }
}
