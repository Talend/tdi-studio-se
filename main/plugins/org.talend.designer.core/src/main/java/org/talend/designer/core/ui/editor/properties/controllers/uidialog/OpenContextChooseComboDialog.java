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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.process.IContext;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class OpenContextChooseComboDialog extends Dialog {

    @Override
    protected Point getInitialSize() {
        return new Point(300, 100);
    }

    private List<IContext> allContexts;

    private Combo contextCombo;

    private Composite basicComp;

    private Shell parentShell;

    private IContext selectedContext;

    public OpenContextChooseComboDialog(Shell parentShell, List<IContext> allContexts) {
        super(parentShell);
        this.allContexts = allContexts;
        this.parentShell = parentShell;
    }

    protected void okPressed() {
        if (contextCombo.getItem(contextCombo.getSelectionIndex()) != null) {
            String selectedContextName = contextCombo.getItem(contextCombo.getSelectionIndex());
            for (IContext tempContext : allContexts) {
                if (tempContext.getName().equals(selectedContextName)) {
                    setSelectedContext(tempContext);
                    break;
                }
            }

        }
        close();
    }

    public IContext getSelectedContext() {
        return this.selectedContext;
    }

    public void setSelectedContext(IContext selectedContext) {
        this.selectedContext = selectedContext;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // basicComp = new Composite(parent.getShell(), SWT.NONE);
        basicComp = (Composite) super.createDialogArea(parent);
        basicComp.setLayout(new FormLayout());
        basicComp.setSize(200, 200);

        Label label = new Label(basicComp, SWT.NONE);
        label.setText(Messages.getString("OpenContextChooseComboDialog.context")); //$NON-NLS-1$

        contextCombo = new Combo(basicComp, SWT.DROP_DOWN | SWT.READ_ONLY);

        // layout for label
        FormData labelCompData = new FormData();
        labelCompData.top = new FormAttachment(20, 0);
        labelCompData.bottom = new FormAttachment(50, 10);
        labelCompData.left = new FormAttachment(0, 10);
        labelCompData.right = new FormAttachment(contextCombo, 0);
        label.setLayoutData(labelCompData);

        // layout for combo
        FormData contexComboFormData = new FormData();
        contexComboFormData.top = new FormAttachment(10, 0);
        contexComboFormData.bottom = new FormAttachment(50, 10);
        contexComboFormData.left = new FormAttachment(label, 30);
        contexComboFormData.right = new FormAttachment(60, 10);
        contextCombo.setLayoutData(contexComboFormData);

        List<String> names = new ArrayList<String>();
        for (IContext context : allContexts) {
            if (context != null) {
                String name = context.getName();
                names.add(name);
            }
        }

        contextCombo.setItems(names.toArray(new String[0]));
        contextCombo.select(0);

        return basicComp;
    }

}
