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
package org.talend.designer.mapper.ui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class CommonListener {

    private static CommonListener instance = null;

    public static synchronized CommonListener getInstance() {
        if (instance == null) {
            instance = new CommonListener();
        }
        return instance;
    }

    private CommonListener() {
    }

    public Listener getNumberListener() {
        return new NumberListener();
    }

    /**
     * DOC ycbai CommonListener class global comment. Detailled comment
     */
    class NumberListener implements Listener {

        public void handleEvent(Event e) {
            String string = e.text;
            char[] chars = new char[string.length()];
            string.getChars(0, chars.length, chars, 0);
            for (int i = 0; i < chars.length; i++) {
                if (!('0' <= chars[i] && chars[i] <= '9')) {
                    e.doit = false;
                    return;
                }
            }
        }
    }
}
