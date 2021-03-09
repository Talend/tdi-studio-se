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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: FontProviderMapper.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class FontProviderMapper {

    private static Map<FontInfo, Font> fontsCache = new HashMap<FontInfo, Font>();

    public static Font getFont(FontInfo fontInfo) {
        Font fontFromCache = fontsCache.get(fontInfo);
        if (fontFromCache != null) {
            return fontFromCache;
        }
        Font font = new Font(Display.getCurrent(), fontInfo.getFontDatas());
        fontsCache.put(fontInfo, font);
        return font;
    }

    /**
     * DOC amaumont Comment method "release".
     */
    public static void releaseFonts() {
        Collection<Font> fonts = fontsCache.values();
        for (Font font : fonts) {
            if (!font.isDisposed()) {
                font.dispose();
            }
        }
        fontsCache.clear();
    }

    public static void releaseFont(FontInfo fontInfo) {
        Font font = fontsCache.get(fontInfo);
        if (!font.isDisposed()) {
            font.dispose();
        }
        fontsCache.remove(fontInfo);
    }

}
