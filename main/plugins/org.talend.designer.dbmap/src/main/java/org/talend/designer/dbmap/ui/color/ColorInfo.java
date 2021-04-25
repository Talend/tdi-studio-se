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
package org.talend.designer.dbmap.ui.color;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: ColorInfo.java 1157 2006-12-21 09:53:10Z amaumont $
 *
 */
public enum ColorInfo {

    COLOR_ENTRY_HIGHLIGHTED(176, 231, 0), // green
    COLOR_ENTRY_HIGHLIGHTEDALL(255, 255, 0), // yellow
    COLOR_ENTRY_SEARCH_HIGHLIGHTED(255, 150, 20), // orange
    COLOR_ENTRY_ERROR(100, 200, 255), // ?
    COLOR_ENTRY_WARNING(0, 200, 60), // ?
    COLOR_ENTRY_NORMAL(170, 170, 170), // ?
    COLOR_RED(255, 0, 0), // red
    COLOR_ENTRY_NONE(255, 255, 255), // white

    COLOR_BACKGROUND_LINKS_ZONE(210, 210, 196), // gray

    // COLOR_UNSELECTED_ZONE_TO_ZONE_LINK(235, 235, 0), // light yellow
    COLOR_SELECTED_ZONE_TO_ZONE_LINK(255, 255, 0), // yellow
    COLOR_UNSELECTED_ZONE_TO_ZONE_LINK(196, 196, 180), // light gray

    COLOR_SELECTED_LOOKUP_LINKS(160, 40, 210), // violet
    COLOR_UNSELECTED_LOOKUP_LINKS(200, 186, 225), // pastel violet

    COLOR_SELECTED_FILTER_LINK(255, 150, 20), // orange
    // COLOR_UNSELECTED_FILTER_LINK(255, 190, 150), // light orange
    COLOR_UNSELECTED_FILTER_LINK(255, 200, 70), // light gray

    COLOR_HIGHLIGHTED_TEXT_ROW(240, 240, 240), // light gray

    COLOR_BACKGROUND_ERROR_EXPRESSION_CELL(255, 0, 0), // red
    COLOR_BACKGROUND_WARNING_EXPRESSION_CELL(255, 190, 150), // light orange
    COLOR_FOREGROUND_ERROR_EXPRESSION_CELL(255, 255, 255), // red
    COLOR_BACKGROUND_VALID_EXPRESSION_CELL(255, 255, 255), // white
    COLOR_BACKGROUND_TRANSPRENT(0, 0, 0, 0), // transparent

    COLOR_DRAGGING_INSERTION_INDICATOR(0, 78, 152),

    COLOR_WARN_INDICATOR(0, 78, 152) // warnning yellow
    ; // blue

    private int red;

    private int green;

    private int blue;

    private int alpha = 255;

    private int systemColor = -1;

    private ColorInfo(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    private ColorInfo(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    // private ColorInfo(int systemColor) {
    // this.systemColor = systemColor;
    // }

    public int getBlue() {
        return this.blue;
    }

    public int getGreen() {
        return this.green;
    }

    public int getRed() {
        return this.red;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public RGB getRGB() {
        if (systemColor != -1) {
            return Display.getCurrent().getSystemColor(systemColor).getRGB();
        } else {
            return new RGB(red, green, blue);
        }
    }

}
