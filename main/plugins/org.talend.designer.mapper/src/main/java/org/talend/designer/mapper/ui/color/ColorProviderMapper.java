// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.mapper.ui.color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ColorProviderMapper {

    private static Map<ColorInfo, Color> colorsCache = new HashMap<ColorInfo, Color>();

    public static Color getColor(ColorInfo colorInfo) {
        Color colorFromCache = colorsCache.get(colorInfo);
        if (colorFromCache != null) {
            return colorFromCache;
        }
        Color color = new Color(Display.getCurrent(), colorInfo.getRed(), colorInfo.getGreen(), colorInfo.getBlue());
        colorsCache.put(colorInfo, color);
        return color;
    }
    
    public static Color getRGBAColor(ColorInfo colorInfo) {
        Color colorFromCache = colorsCache.get(colorInfo);
        if (colorFromCache != null) {
            return colorFromCache;
        }
        Color color = new Color(Display.getCurrent(), colorInfo.getRed(), colorInfo.getGreen(), colorInfo.getBlue(), colorInfo.getAlpha());
        colorsCache.put(colorInfo, color);
        return color;
    }

    public static Color getColor(Display display, int swtColor) {
        return display.getSystemColor(swtColor);
    }

    /**
     * DOC amaumont Comment method "release".
     */
    public static void releaseColors() {
        Collection<Color> colors = colorsCache.values();
        for (Color color : colors) {
            if (!color.isDisposed()) {
                color.dispose();
            }
        }
        colorsCache.clear();
    }

    public static void releaseColor(ColorInfo colorInfo) {
        Color color = colorsCache.get(colorInfo);
        if (!color.isDisposed()) {
            color.dispose();
        }
        colorsCache.remove(colorInfo);
    }

}
