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
package org.talend.designer.core.ui.editor.notes;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

/**
 */
public class NoteCellEditorLocator implements CellEditorLocator {

    private static final int BORDER = 10;

    private NoteFigure noteFigure;

    public NoteCellEditorLocator(NoteFigure noteFigure) {
        this.noteFigure = noteFigure;
    }

    public void relocate(CellEditor celleditor) {
        Text text = (Text) celleditor.getControl();
        Rectangle rect = noteFigure.getClientArea();
        noteFigure.translateToAbsolute(rect);
        org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(BORDER, BORDER, -2 * BORDER, -2 * BORDER);
        rect.translate(trim.x, trim.y);
        rect.width += trim.width;
        rect.height += trim.height;
        text.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

}
