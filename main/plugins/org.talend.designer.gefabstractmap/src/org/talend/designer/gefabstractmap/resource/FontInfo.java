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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: FontInfo.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public enum FontInfo {

    FONT_SYSTEM_BOLD(SWT.BOLD); // system font bold

    private FontData[] fontDatas;

    // private FontInfo(String name) {
    // fontDatas = getSystemFontDatas();
    // fontDatas[0].setName(name);
    // }

    /**
     * DOC amaumont Comment method "getSystemFontDatas".
     *
     * @return
     */
    private FontData[] getSystemFontDatas() {
        return Display.getCurrent().getSystemFont().getFontData();
    }

    // private FontInfo(Integer height) {
    // fontDatas = getSystemFontDatas();
    // fontDatas[0].setHeight(height);
    // }

    private FontInfo(int style) {
        fontDatas = getSystemFontDatas();
        fontDatas[0].setStyle(style);
    }

    // private FontInfo(String name, int style) {
    // this(name);
    // fontDatas[0].setStyle(style);
    // }
    //
    // private FontInfo(int height, String name) {
    // this(name);
    // fontDatas[0].setHeight(height);
    // }
    //
    // private FontInfo(Integer height, int style) {
    // this(style);
    // fontDatas[0].setHeight(height);
    // }
    //
    // private FontInfo(FontData fontData) {
    // fontDatas = new FontData[] { fontData };
    // }

    public FontData[] getFontDatas() {
        return this.fontDatas;
    }

}
