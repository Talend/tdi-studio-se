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
package org.talend.sqlbuilder.erdiagram.ui.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ColumnPaneFigure extends Figure {

    public ColumnPaneFigure() {
        ToolbarLayout layout = new ToolbarLayout();
        layout.setStretchMinorAxis(true);
        setLayoutManager(layout);

        setBackgroundColor(ColorConstants.white);
        setForegroundColor(CustomTableFigure.BG_COLOR);
        setOpaque(true);
        // setBorder(new ColumnFigureBorder());
    }

}
