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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

/**
 * Manage the direct edit of a text in GEF. <br/>
 *
 * $Id$
 *
 */
public class NodeLabelEditManager extends DirectEditManager {

    Font scaledFont;

    private Text text;

    public NodeLabelEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator) {
        super(source, editorType, locator);
    }

    @Override
    protected void bringDown() {
        Font disposeFont = scaledFont;
        scaledFont = null;
        super.bringDown();
        if (disposeFont != null) {
            disposeFont.dispose();
        }
    }

    @Override
    protected void initCellEditor() {
        Connection connection = ((ConnectionLabel) this.getEditPart().getModel()).getConnection();
        if (getCellEditor() instanceof NodeLabelCellEditor) {
            ((NodeLabelCellEditor) getCellEditor()).setCurrentConnection(connection);
        }
        Label label = (Label) (getEditPart()).getFigure();
        String initialLabelText = label.getText();
        getCellEditor().setValue(initialLabelText);
        text = (Text) getCellEditor().getControl();
        IFigure figure = (getEditPart()).getFigure();
        scaledFont = figure.getFont();
        FontData data = scaledFont.getFontData()[0];
        Dimension fontSize = new Dimension(0, data.getHeight());
        label.translateToAbsolute(fontSize);
        data.setHeight(fontSize.height);
        scaledFont = new Font(null, data);
        text.setFont(scaledFont);
        text.selectAll();
    }

    /**
     * yzhang Comment method "getTextControl".
     *
     * @return
     */
    public Text getTextControl() {
        if (getCellEditor() == null) {
            return null;
        }
        return (Text) getCellEditor().getControl();
    }
}
