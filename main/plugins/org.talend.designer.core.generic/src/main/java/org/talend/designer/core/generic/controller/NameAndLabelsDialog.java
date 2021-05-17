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
package org.talend.designer.core.generic.controller;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.core.ui.composite.ElementsSelectionComposite;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.designer.core.generic.i18n.Messages;

/**
 * created by ycbai on 2015年10月12日 Detailled comment
 *
 */
public class NameAndLabelsDialog extends Dialog {

    private List<NamedThing> nameAndLabels;

    private String result;

    private ElementsSelectionComposite<NamedThing> selectionComposite;

    private LabelledText customObjNameText;

    boolean isInWizard;

    public NameAndLabelsDialog(Shell parentShell, List<NamedThing> nameAndLabels, boolean isInWizard) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL);
        this.nameAndLabels = nameAndLabels;
        this.isInWizard = isInWizard;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(450, 550);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Composite comp = new Composite(composite, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new GridLayout());

        selectionComposite = new ElementsSelectionComposite<NamedThing>(comp) {

            @Override
            protected IBaseLabelProvider getLabelProvider() {
                return new LabelProvider() {

                    @Override
                    public String getText(Object obj) {
                        NamedThing nal = (NamedThing) obj;
                        return nal.getDisplayName();
                    }

                    @Override
                    public Image getImage(Object obj) {
                        return null;
                    }
                };
            };
        }.setMultipleSelection(false).create();
        selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectionComposite.setViewerData(nameAndLabels);

        Composite customComposite = new Composite(comp, SWT.NONE);
        customComposite.setLayout(new GridLayout());
        customComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button useCustomBtn = new Button(customComposite, SWT.CHECK);
        useCustomBtn.setText(Messages.getString("NameAndLabelsDialog.custom.button")); //$NON-NLS-1$
        useCustomBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFieldsStatus(useCustomBtn.getSelection());
            }
        });
        customObjNameText = new LabelledText(customComposite, Messages.getString("NameAndLabelsDialog.custom.text")); //$NON-NLS-1$
        updateFieldsStatus(useCustomBtn.getSelection());
        if (!isInWizard) {
            // set empty quotes to show the user he should fill the name between quotes
            customObjNameText.setText("\"\""); //$NON-NLS-1$
        }

        return composite;
    }

    private void updateFieldsStatus(boolean isUseCustom) {
        selectionComposite.setEnabled(!isUseCustom);
        customObjNameText.getTextControl().setEditable(isUseCustom);
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point size = getShell().getSize();
        Point location = getInitialLocation(size);
        getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
    }

    @Override
    protected void okPressed() {
        result = getSelectedName();
        super.okPressed();
    }

    private String getSelectedName() {
        if (selectionComposite.isEnabled()) {
            List<NamedThing> selectedElements = selectionComposite.getSelectedElements();
            if (selectedElements.size() > 0) {
                if (!isInWizard) {
                    return TalendQuoteUtils.addQuotes(selectedElements.get(0).getName());
                } else {
                    return selectedElements.get(0).getName();
                }
            }
        } else { // Custom object
            return customObjNameText.getText();
        }
        return ""; //$NON-NLS-1$
    }

    public String getResult() {
        return this.result;
    }

}
