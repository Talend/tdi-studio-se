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
package org.talend.designer.unifiedcomponent.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;
import org.talend.designer.unifiedcomponent.i18n.Messages;

/**
 * created by wchen on Dec 6, 2017 Detailled comment
 *
 */
public class UnifiedComponentController extends AbstractElementPropertySectionController {

    private int HSPACE = 5;

    private int VSPACE = 4;

    /**
     *
     * DOC wchen UnifiedComponentController constructor comment.
     *
     * @param dp
     */
    public UnifiedComponentController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        CCombo combo;

        Control lastControlUsed = lastControl;

        combo = new CCombo(subComposite, SWT.BORDER);
        FormData data;

        combo.setItems(getDatabases((Node) elem));
        combo.setEditable(false);
        combo.setEnabled(!param.isReadOnly());
        combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        combo.setLayoutData(data);
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName());
        lastControlUsed = combo;
        lastControlUsed = addButton(subComposite, param, lastControlUsed, numInRow, top);

        hashCurControls.put(param.getName(), combo);

        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + VSPACE);
        return lastControlUsed;
    }

    private Control addButton(Composite subComposite, final IElementParameter param, Control lastControl, int numInRow, int top) {
        Button button;
        Point buttonSize;
        FormData data;
        // if (!createFile) {
        button = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        button.setText(Messages.getString("UnifiedComponentController.applybtn.text"));
        button.setData(PARAMETER_NAME, param.getName());
        button.setEnabled(!param.isReadOnly());

        button.addSelectionListener(listenerSelection);

        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        // data.right = new FormAttachment(lastControl, STANDARD_LABEL_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(lastControl, 0, SWT.TOP);
        data.bottom = new FormAttachment(lastControl, 0, SWT.BOTTOM);

        button.setLayoutData(data);

        int buttonHeight = buttonSize.y + VSPACE;
        if (dynamicProperty.getCurRowSize() < buttonHeight) {
            dynamicProperty.setCurRowSize(buttonHeight);
        }

        return button;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        CCombo combo = new CCombo(subComposite, SWT.BORDER);
        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        combo.dispose();

        return initialSize.y + VSPACE;

    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            if (event.getSource() instanceof CCombo) {
                CCombo combo = (CCombo) event.getSource();
                String paramName = (String) combo.getData(PARAMETER_NAME);
                IElementParameter param = ((Node) elem).getElementParameter(paramName);
                String oldValue = String.valueOf(param.getValue());
                if (!combo.getText().equals(oldValue)) {
                    combo.setBackground(combo.getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
                }
            } else if (event.getSource() instanceof Button) {
                Button button = (Button) event.getSource();
                String paramName = (String) button.getData(PARAMETER_NAME);
                IElementParameter param = ((Node) elem).getElementParameter(paramName);
                CCombo combo = (CCombo) hashCurControls.get(paramName);
                if (combo == null || combo.isDisposed()) {
                    return;
                }
                ChangeComponentCommand cmd = new ChangeComponentCommand((Node) elem, param, combo.getText());

                executeCommand(cmd);
            }

        }
    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        String paramName = param.getName();
        CCombo combo = (CCombo) hashCurControls.get(paramName);
        if (combo == null || combo.isDisposed()) {
            return;
        }
        String value = getDatabaseName((Node) elem, param);
        if (value != null) {
            String[] paramItems = getDatabases((Node) elem);
            String[] comboItems = combo.getItems();

            if (!Arrays.equals(paramItems, comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText(value);
            combo.setVisible(true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    private String[] getDatabases(Node node) {
        List<String> database = new ArrayList<String>();
        if (node.getDelegateComponent() instanceof DelegateComponent) {
            DelegateComponent dComp = (DelegateComponent) node.getDelegateComponent();
            Set<UnifiedObject> unifiedComponents = dComp.getUnifiedObjectsByPalette(dComp.getPaletteType());
            for (UnifiedObject obj : unifiedComponents) {
                database.add(obj.getDatabase());
            }
        }
        Collections.sort(database);
        return database.toArray(new String[database.size()]);
    }

    private String getDatabaseName(Node node, IElementParameter param) {
        if (node.getDelegateComponent() instanceof DelegateComponent) {
            DelegateComponent dComp = (DelegateComponent) node.getDelegateComponent();
            Object value = param.getValue();
            String componentName = value == null ? null : value.toString();
            if (componentName != null) {
                UnifiedObject unifiedObjectByName = dComp.getUnifiedObjectByName(componentName);
                if (unifiedObjectByName != null) {
                    return unifiedObjectByName.getDatabase();
                }
            }

        }
        return null;
    }
}
