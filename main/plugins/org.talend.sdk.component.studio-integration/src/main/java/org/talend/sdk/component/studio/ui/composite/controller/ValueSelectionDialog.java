/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.ui.composite.controller;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.talend.sdk.component.studio.i18n.Messages;

public class ValueSelectionDialog extends Dialog {
    
    private ElementsSelectionComposite<String> selectionComposite;
    
    private final List<String> possibleValues = new ArrayList<>();
    
    private LabelledText customValueText;
    
    private String result;

    public ValueSelectionDialog(final Shell parentShell, final List<String> values) {
        super(parentShell);
        if (values != null) {
            possibleValues.addAll(values);
        }
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(450, 550);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(dialogArea, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        selectionComposite = new ElementsSelectionComposite<String>(composite) {

            @Override
            protected IBaseLabelProvider getLabelProvider() {
                return new LabelProvider() {

                    @Override
                    public String getText(Object obj) {
                        return (String) obj;
                    }

                    @Override
                    public Image getImage(Object obj) {
                        return null;
                    }
                };
            };
        }.setMultipleSelection(false).create();
        selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectionComposite.setViewerData(possibleValues);

        Composite customValueComposite = new Composite(composite, SWT.NONE);
        customValueComposite.setLayout(new GridLayout());
        customValueComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button useCustomButton = new Button(customValueComposite, SWT.CHECK);
        useCustomButton.setText(Messages.getString("suggestion.dialog.custom.button")); //$NON-NLS-1$
        useCustomButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFieldsStatus(useCustomButton.getSelection());
            }
        });
        customValueText = new LabelledText(customValueComposite, Messages.getString("suggestion.dialog.custom.text")); //$NON-NLS-1$
        updateFieldsStatus(useCustomButton.getSelection());
        return dialogArea;
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
        result = getSelectedValue();
        super.okPressed();
    }
    
    public String getResult() {
        return this.result;
    }
    
    private String getSelectedValue() {
        if (selectionComposite.isEnabled()) {
            List<String> selectedElements = selectionComposite.getSelectedElements();
            if (selectedElements.size() > 0) {
                return selectedElements.get(0);
            } else {
                return ""; //$NON-NLS-1$
            }
        } else {
            return customValueText.getText();
        }
    }
    
    private void updateFieldsStatus(boolean usesCustom) {
        selectionComposite.setEnabled(!usesCustom);
        customValueText.getTextControl().setEditable(usesCustom);
    }
}
