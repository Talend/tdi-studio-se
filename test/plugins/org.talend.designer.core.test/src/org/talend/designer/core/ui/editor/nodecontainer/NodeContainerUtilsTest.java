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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * DOC rdubois class global comment. Detailled comment
 */
public class NodeContainerUtilsTest {

    private final static String SPACE = " "; //$NON-NLS-1$

    private final static String MILLISECOND = "ms"; //$NON-NLS-1$

    private final static String SECOND = "s"; //$NON-NLS-1$

    private final static String MINUTE = "m"; //$NON-NLS-1$

    @Test
    public void testformatTime500ms() {
        String result = NodeContainerUtils.formatTime("500"); //$NON-NLS-1$
        assertEquals(result, "500" + MILLISECOND); //$NON-NLS-1$
    }

    @Test
    public void testformatTime1000ms() {
        String result = NodeContainerUtils.formatTime("1000"); //$NON-NLS-1$
        assertEquals(result, "1" + SECOND); //$NON-NLS-1$
    }

    @Test
    public void testformatTime1500ms() {
        String result = NodeContainerUtils.formatTime("1500"); //$NON-NLS-1$
        assertEquals(result, "1" + SECOND + SPACE + "500" + MILLISECOND); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testformatTime60000ms() {
        String result = NodeContainerUtils.formatTime("60000"); //$NON-NLS-1$
        assertEquals(result, "1" + MINUTE); //$NON-NLS-1$
    }

    @Test
    public void testformatTime60500ms() {
        String result = NodeContainerUtils.formatTime("60500"); //$NON-NLS-1$
        assertEquals(result, "1" + MINUTE + SPACE + "500" + MILLISECOND); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testformatTime62500ms() {
        String result = NodeContainerUtils.formatTime("62500"); //$NON-NLS-1$
        assertEquals(result, "1" + MINUTE + SPACE + "2" + SECOND + SPACE + "500" + MILLISECOND); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testformatTime1000000000ms() {
        String result = NodeContainerUtils.formatTime("1000000000"); //$NON-NLS-1$
        assertEquals(result, "16666" + MINUTE + SPACE + "40" + SECOND); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testformatTimeNotNumeric() {
        String result = NodeContainerUtils.formatTime("abc"); //$NON-NLS-1$
        assertEquals(result, ""); //$NON-NLS-1$
    }

    @Test
    public void testformatTimeNull() {
        String result = NodeContainerUtils.formatTime(null);
        assertEquals(result, ""); //$NON-NLS-1$
    }
}
