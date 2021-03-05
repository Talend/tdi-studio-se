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
package org.talend.designer.xmlmap.parts.directedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class TextAreaBehavior extends CellEditorDialogBehavior {

    /**
     * wchen CellEditorDialogBehavior constructor comment.
     */
    public TextAreaBehavior() {
        super();
    }

    private Composite panel;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.rowgenerator.ui.tabs.IExtendedCellEditorBehavior#createBehaviorControls(org.eclipse.swt.widgets
     * .Composite)
     */
    public Control createBehaviorControls(Composite parent) {
        panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 2;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);

        GridData gd = new GridData(GridData.FILL_BOTH | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        panel.setLayoutData(gd);

        GridData controlGD = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);

        Control text = getExtendedTextCellEditor().createText(panel);
        text.setLayoutData(controlGD);
        text.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

        return panel;

    }

}
