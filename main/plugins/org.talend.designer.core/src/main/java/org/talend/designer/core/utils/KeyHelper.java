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
package org.talend.designer.core.utils;

import org.eclipse.swt.SWT;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: KeyHelper.java 21728 2009-02-09 10:23:23Z plegall $
 *
 */
public class KeyHelper {

    public boolean ctrlPressed(int keyCode) {
        return modifierPressed(keyCode, SWT.CTRL);
    }

    public boolean altPressed(int keyCode) {
        return modifierPressed(keyCode, SWT.ALT);
    }

    public boolean shiftPressed(int keyCode) {
        return modifierPressed(keyCode, SWT.SHIFT);
    }

    public boolean anyModifierPressed(int keyCode) {
        return ctrlPressed(keyCode) || shiftPressed(keyCode) || altPressed(keyCode);
    }

    public boolean cursorPressed(int keyCode) {
        boolean arrow = false;
        arrow = arrow || keyCode == SWT.ARROW_UP;
        arrow = arrow || keyCode == SWT.ARROW_DOWN;
        arrow = arrow || keyCode == SWT.ARROW_LEFT;
        arrow = arrow || keyCode == SWT.ARROW_RIGHT;
        return arrow;
    }

    private boolean modifierPressed(int keyCode, int modifier) {
        return (keyCode & modifier) != 0;
    }
}
