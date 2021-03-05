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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * zli class global comment. Detailled comment
 */
public class SchemaDetailsCheckBoxCellEditor extends CellEditor {

    private Button checkBtn;

    private FocusListener buttonFocusListener;

    private Boolean value;

    public SchemaDetailsCheckBoxCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
        checkBtn = new Button(parent, SWT.CHECK);
        checkBtn.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (e.character == '\u001b') { // Escape
                    fireCancelEditor();
                }
            }
        });
        checkBtn.addFocusListener(getButtonFocusListener());

        checkBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {

                markDirty();
                doSetValue(checkBtn.getSelection());
                // fireApplyEditorValue();
            }
        });

        setValueValid(true);

        return checkBtn;
    }

    private FocusListener getButtonFocusListener() {
        if (buttonFocusListener == null) {
            buttonFocusListener = new FocusListener() {

                public void focusGained(FocusEvent e) {
                    // Do nothing
                }

                public void focusLost(FocusEvent e) {
                    SchemaDetailsCheckBoxCellEditor.this.focusLost();
                }
            };
        }

        return buttonFocusListener;
    }

    @Override
    protected Object doGetValue() {
        return value;
    }

    @Override
    protected void doSetValue(Object value) {
        this.value = ((Boolean) value).booleanValue();
        // checkBtn.setSelection(this.value);
    }

    @Override
    protected void doSetFocus() {
        checkBtn.setFocus();
        checkBtn.addFocusListener(getButtonFocusListener());
    }

    public void deactivate() {
        if (checkBtn != null && !checkBtn.isDisposed()) {
            checkBtn.removeFocusListener(getButtonFocusListener());
        }

        super.deactivate();
    }
}
