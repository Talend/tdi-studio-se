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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class RadioFieldEditor extends BooleanFieldEditor {

    /**
     * Style constant (value <code>0</code>) indicating the default layout where the field editor's check box appears
     * to the left of the label.
     */
    public static final int DEFAULT = 0;

    /**
     * Style constant (value <code>1</code>) indicating a layout where the field editor's label appears on the left
     * with a check box on the right.
     */
    public static final int SEPARATE_LABEL = 1;

    /**
     * Style bits. Either <code>DEFAULT</code> or <code>SEPARATE_LABEL</code>.
     */
    private int style;

    /**
     * The previously selected, or "before", value.
     */
    private boolean wasSelected;

    /**
     * The radio control, or <code>null</code> if none.
     */
    private Button radio = null;

    private Composite parent;

    public RadioFieldEditor(String name, String label, Composite aParent) {
        this(name, label, DEFAULT, aParent);
        this.parent = aParent;
    }

    /**
     * Creates a boolean field editor in the given style.
     *
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param style the style, either <code>DEFAULT</code> or <code>SEPARATE_LABEL</code>
     * @param parent the parent of the field editor's control
     * @see #DEFAULT
     * @see #SEPARATE_LABEL
     */
    public RadioFieldEditor(String name, String labelText, int style, Composite parent) {
        init(name, labelText);
        this.style = style;
        createControl(parent);
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    protected void adjustForNumColumns(int numColumns) {
        if (style == SEPARATE_LABEL) {
            numColumns--;
        }
        ((GridData) radio.getLayoutData()).horizontalSpan = numColumns;
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        String text = getLabelText();
        switch (style) {
        case SEPARATE_LABEL:
            getLabelControl(parent);
            numColumns--;
            text = null;
        default:
            radio = getChangeControl(parent);
            GridData gd = new GridData();
            gd.horizontalSpan = numColumns;
            radio.setLayoutData(gd);
            if (text != null) {
                radio.setText(text);
            }
        }
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor. Loads the value from the preference store and sets it to the check
     * box.
     */
    @Override
    protected void doLoad() {
        if (radio != null) {
            boolean value = getPreferenceStore().getBoolean(getPreferenceName());
            radio.setSelection(value);
            wasSelected = value;
        }
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor. Loads the default value from the preference store and sets it to
     * the check box.
     */
    @Override
    protected void doLoadDefault() {
        if (radio != null) {
            boolean value = getPreferenceStore().getDefaultBoolean(getPreferenceName());
            radio.setSelection(value);
            wasSelected = value;
        }
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), radio.getSelection());
    }

    /**
     * Returns this field editor's current value.
     *
     * @return the value
     */
    @Override
    public boolean getBooleanValue() {
        return radio.getSelection();
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    public int getNumberOfControls() {
        switch (style) {
        case SEPARATE_LABEL:
            return 2;
        default:
            return 1;
        }
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    public void setFocus() {
        if (radio != null) {
            radio.setFocus();
        }
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    @Override
    public void setLabelText(String text) {
        super.setLabelText(text);
        Label label = getLabelControl();
        if (label == null && radio != null) {
            radio.setText(text);
        }
    }

    /**
     * Informs this field editor's listener, if it has one, about a change to the value (<code>VALUE</code> property)
     * provided that the old and new values are different.
     *
     * @param oldValue the old value
     * @param newValue the new value
     */
    @Override
    protected void valueChanged(boolean oldValue, boolean newValue) {
        setPresentsDefaultValue(false);
        if (oldValue != newValue) {
            fireStateChanged(VALUE, oldValue, newValue);
        }
    }

    /*
     * @see FieldEditor.setEnabled
     */
    @Override
    public void setEnabled(boolean enabled, Composite parent) {
        // Only call super if there is a label already
        if (style == SEPARATE_LABEL) {
            super.setEnabled(enabled, parent);
        }
        getChangeControl(parent).setEnabled(enabled);
    }

    @Override
    protected Button getChangeControl(Composite parent) {
        if (radio == null) {
            radio = new Button(parent, SWT.RADIO | SWT.LEFT);
            radio.setFont(parent.getFont());
            radio.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean isSelected = radio.getSelection();
                    valueChanged(wasSelected, isSelected);
                    wasSelected = isSelected;
                }
            });
            radio.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent event) {
                    radio = null;
                }
            });
        } else {
            checkParent(radio, parent);
        }
        return radio;
    }

    public Button getRadio() {
        return getChangeControl(parent);
    }

}
