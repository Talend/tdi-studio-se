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
package org.talend.designer.mapper.ui.visualmap.table;

import org.eclipse.swt.graphics.Color;
import org.talend.designer.mapper.ui.color.ColorInfo;
import org.talend.designer.mapper.ui.color.ColorProviderMapper;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public enum EntryState {
    HIGHLIGHT(ColorInfo.COLOR_ENTRY_HIGHLIGHTED),
    HIGHLIGHTALL(ColorInfo.COLOR_ENTRY_HIGHLIGHTEDALL),
    SEARCH_HIGHLIGHT(ColorInfo.COLOR_ENTRY_SEARCH_HIGHLIGHTED),
    ERROR(ColorInfo.COLOR_ENTRY_ERROR),
    WARNING(ColorInfo.COLOR_ENTRY_WARNING),
    NONE(ColorInfo.COLOR_ENTRY_NONE);

    private ColorInfo colorInfo;

    private EntryState(ColorInfo colorInfo) {
        this.colorInfo = colorInfo;
    }

    public Color getColor() {
        return ColorProviderMapper.getColor(colorInfo);
    }

}
