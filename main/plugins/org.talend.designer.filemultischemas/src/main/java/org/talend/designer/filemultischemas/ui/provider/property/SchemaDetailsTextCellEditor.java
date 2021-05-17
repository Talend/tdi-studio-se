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
package org.talend.designer.filemultischemas.ui.provider.property;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.filemultischemas.data.EPropertyName;
import org.talend.designer.filemultischemas.managers.UIManager;

/**
 * cLi class global comment. Detailled comment
 */
public class SchemaDetailsTextCellEditor extends TextCellEditor {

    private final TreeViewer schemaDetailsViewer;

    private final Color redColor;

    public SchemaDetailsTextCellEditor(final TreeViewer schemaDetailsViewer, final EPropertyName property) {
        super(schemaDetailsViewer.getTree());
        this.schemaDetailsViewer = schemaDetailsViewer;
        //
        this.redColor = schemaDetailsViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_RED);

        setValidator(new ICellEditorValidator() {

            public String isValid(Object input) {
                String mess = UIManager.validSchemaDetailsColumns(schemaDetailsViewer, null, property, input);
                validate(mess == null);
                return mess;
            }
        });

    }

    protected Text getText() {
        return this.text;
    }

    protected void validate(boolean valid) {
        if (valid) {
            getText().setBackground(null);
        } else {
            getText().setBackground(this.redColor);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellEditor#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (this.redColor != null) {
            this.redColor.dispose();
        }
    }

}
