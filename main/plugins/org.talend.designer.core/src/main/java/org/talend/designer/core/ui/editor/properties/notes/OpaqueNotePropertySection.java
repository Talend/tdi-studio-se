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
package org.talend.designer.core.ui.editor.properties.notes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.ChangeNoteOpacityCommand;

/**
 */
public class OpaqueNotePropertySection extends AbstractNotePropertySection {

    private Button check;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        if (composite.getLayout() instanceof FormLayout) {
            FormLayout formLayout = (FormLayout) composite.getLayout();
            formLayout.spacing = 0;
        }
        FormData data;

        check = getWidgetFactory().createButton(composite, "", SWT.CHECK); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        check.setLayoutData(data);

        CLabel labelLabel = getWidgetFactory().createCLabel(composite, Messages.getString("OpaqueNoteSection.Label")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(check);
        data.top = new FormAttachment(check, 0, SWT.TOP);
        labelLabel.setLayoutData(data);

        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (check.getSelection() != (note.isOpaque())) {
                    ChangeNoteOpacityCommand command = new ChangeNoteOpacityCommand(note, check.getSelection());
                    getCommandStack().execute(command);
                }
            }
        });
    }

    @Override
    public void refresh() {
        if (!check.isDisposed()) {
            check.setEnabled(!note.isReadOnly());
            check.setSelection(note.isOpaque());
        }
    }

}
