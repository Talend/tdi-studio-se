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
package org.talend.designer.gefabstractmap.resource;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: ColorInfo.java 46748 2010-08-13 07:51:40Z wchen $
 *
 */
public enum ColorInfo {
    COLOR_ENTRY_HIGHLIGHTED(176, 231, 0), // green
    COLOR_ENTRY_HIGHLIGHTEDALL(255, 255, 0), // yellow
    COLOR_ENTRY_SEARCH_HIGHLIGHTED(255, 150, 20), // orange
    COLOR_ENTRY_NONE(255, 255, 255), // white

    COLOR_BACKGROUND_LINKS_ZONE(210, 210, 196), // gray

    COLOR_SELECTED_LOOKUP_LINKS(160, 40, 210), // violet
    COLOR_UNSELECTED_LOOKUP_LINKS(200, 186, 225), // pastel violet

    COLOR_SELECTED_FILTER_LINK(255, 150, 20), // orange
    COLOR_UNSELECTED_FILTER_LINK(255, 200, 70), // light gray

    COLOR_HIGHLIGHTED_TEXT_ROW(240, 240, 240), // light gray

    ZONE_BACKGROUND_COLOR(241, 239, 226), // zone background color

    COLOR_TREE_BORDER(153, 186, 243),

    COLOR_TMAP_PREVIEW(235, 234, 230),

    COLOR_EXPREESION_DISABLE(240, 240, 240),

    COLOR_SEPARATOR_TOP_LEFT(172, 168, 153), // dark gray

    COLOR_TREE_LINES(128, 128, 128),

    COLOR_DRAGGING_INSERTION_INDICATOR(0, 78, 152), // blue
    COLOR_COLUMN_TREE_SETTING(200, 225, 250),
    COLOR_COLUMN_SELECTION(90, 180, 255), // ligth blue

    ;

    private int red;

    private int green;

    private int blue;

    private int systemColor = -1;

    private ColorInfo(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getGreen() {
        return this.green;
    }

    public int getRed() {
        return this.red;
    }

    public RGB getRGB() {
        if (systemColor != -1) {
            return Display.getCurrent().getSystemColor(systemColor).getRGB();
        } else {
            return new RGB(red, green, blue);
        }
    }

}
