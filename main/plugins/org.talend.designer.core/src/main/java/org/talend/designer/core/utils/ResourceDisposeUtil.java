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

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

/**
 *
 * nma class global comment. Detailled comment
 */
public class ResourceDisposeUtil {

    public static void disposeColor(Color color) {
        if (color != null && !color.isDisposed()) {
            color.dispose();
        }
    }

    public static void setAndCheckColor(IFigure figure, Color newColor, boolean foreground) {
        if (figure == null || newColor == null) {
            return;
        }
        // Color oldColor = figure.getBackgroundColor();
        // if (foreground) {
        // oldColor = figure.getForegroundColor();
        // }
        if (foreground) {
            figure.setForegroundColor(newColor);
        } else {
            figure.setBackgroundColor(newColor);
        }
        // maybe, no need to dispose, because have used the cache

        // if (oldColor != null && !oldColor.isDisposed()) {
        // if (oldColor.equals(newColor)) {
        // newColor.dispose();
        // } else {
        // oldColor.dispose();
        // }
        // }
    }
}
