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

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 */
public class NoteDirectEditManager extends DirectEditManager {

    public NoteDirectEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator) {
        super(source, editorType, locator);
    }

    @Override
    protected CellEditor createCellEditorOn(Composite composite) {
        return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP);
    }

    @Override
    protected void initCellEditor() {
        NoteFigure noteFigure = (NoteFigure) getEditPart().getFigure();
        getCellEditor().setValue(noteFigure.getText());
    }

    @Override
    public void show() {
        super.show();

        // unselect text in celleditor's control
        if (getCellEditor().getControl() instanceof Text) {
            Text text = (Text) getCellEditor().getControl();
            text.setSelection(text.getText().length(), text.getText().length());
        }
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
        return (Text) ((TextCellEditor) getCellEditor()).getControl();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.DirectEditManager#getCellEditor()
     */
    @Override
    public CellEditor getCellEditor() {
        return super.getCellEditor();
    }
}
