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
package org.talend.designer.core.ui.editor.nodecontainer;

/**
 * DOC rdubois class global comment. Detailled comment
 */
public class NodeContainerUtils {

    private final static String SPACE = " "; //$NON-NLS-1$

    private final static String MILLISECOND = "ms"; //$NON-NLS-1$

    private final static String SECOND = "s"; //$NON-NLS-1$

    private final static String MINUTE = "m"; //$NON-NLS-1$

    public static String formatTime(String time) {
        Long time_ms = 0L;
        try {
            time_ms = Long.parseLong(time);
        } catch (NumberFormatException nfe) {
            // nfe.printStackTrace();
            return ""; //$NON-NLS-1$
        }

        if (time_ms < 1000) {
            return time_ms + MILLISECOND;
        } else {
            Long time_s = time_ms / 1000;
            Long time_s_ms = time_ms % 1000;

            if (time_s < 60) {
                StringBuilder sb = new StringBuilder();
                sb.append(time_s + SECOND);
                if (time_s_ms != 0) {
                    sb.append(SPACE + time_s_ms + MILLISECOND);
                }
                return sb.toString();
            } else {
                Long time_m = time_s / 60;
                Long time_m_s = time_s % 60;
                StringBuilder sb = new StringBuilder();
                sb.append(time_m + MINUTE);
                if (time_m_s != 0) {
                    sb.append(SPACE + time_m_s + SECOND);
                }
                if (time_s_ms != 0) {
                    sb.append(SPACE + time_s_ms + MILLISECOND);
                }

                return sb.toString();
            }
        }
    }
}
