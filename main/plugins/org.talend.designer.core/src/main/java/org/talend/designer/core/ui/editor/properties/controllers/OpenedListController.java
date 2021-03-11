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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC zli class global comment. Detailled comment
 */
public class OpenedListController extends AbstractElementPropertySectionController {

    /**
     * DOC zli OpenedListController constructor comment.
     *
     * @param dp
     */
    public OpenedListController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createComboCommand(SelectionEvent event) {
        Set<String> elementsName;
        Control ctrl;
        elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }
                CCombo combo = (CCombo) event.getSource();

                Object data = ctrl.getData(PARAMETER_NAME);
                if (!(ctrl instanceof CCombo)) {
                    continue;
                }
                boolean isDisposed = ((CCombo) ctrl).isDisposed() || combo.isDisposed();
                if (isDisposed) {
                    continue;
                }
                String text = ((CCombo) ctrl).getText();
                if (data != null && data.equals(combo.getData(PARAMETER_NAME))) {
                    if (!text.equals(elem.getPropertyValue(name))) {

                        String value = new String(""); //$NON-NLS-1$

                        for (int i = 0; i < elem.getElementParameters().size(); i++) {
                            IElementParameter param = elem.getElementParameters().get(i);
                            if (param.getName().equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (j < param.getListItemsDisplayName().length
                                            && text.equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                        break;
                                    }
                                }
                            }
                        }
                        if (value.equals(elem.getPropertyValue(name))) { // same value so no need to do anything
                            return null;
                        }
                        return new PropertyChangeCommand(elem, name, value);
                    }
                }
            }
        }
        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("ComboController.valueFromRepository")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setItems(getListToDisplay(param));
        combo.setEditable(!param.isReadOnly() && !param.isRepositoryValueUsed());
        combo.setEnabled(!param.isReadOnly() && !param.isRepositoryValueUsed());
        cLayout.setBackground(subComposite.getBackground());
        combo.addSelectionListener(listenerSelection);
        combo.addModifyListener(modifySelection);
        combo.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

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

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
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
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
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
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent event) {

            Command cmd = createCommand(event);
            executeCommand(cmd);
        }
    };

    ModifyListener modifySelection = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            Command cmd = modifyCommand(e);
            executeCommand(cmd);
        }

    };

    /**
     * This method is used for getting command base on component type.
     *
     * @param e
     * @return
     */
    private Command createCommand(SelectionEvent event) {
        Command cmd = null;
        if (event.getSource() instanceof CCombo) {
            cmd = createComboCommand(event);
        }
        return cmd;
    }

    /**
     * DOC zli Comment method "modifyCommand".
     *
     * @param event
     * @return
     */
    private Command modifyCommand(ModifyEvent event) {
        Set<String> elementsName;
        Control ctrl;
        elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }
                CCombo combo = (CCombo) event.getSource();

                Object data = ctrl.getData(PARAMETER_NAME);
                if (!(ctrl instanceof CCombo)) {
                    continue;
                }
                boolean isDisposed = ((CCombo) ctrl).isDisposed() || combo.isDisposed();
                if (isDisposed) {
                    continue;
                }

                String paramName = "";

                if (data != null) {
                    paramName = data.toString();
                }
                String value = "";
                if (data != null && data.equals(combo.getData(PARAMETER_NAME))) {
                    for (int i = 0; i < elem.getElementParameters().size(); i++) {
                        IElementParameter param = elem.getElementParameters().get(i);
                        if (param.getFieldType().equals(EParameterFieldType.OPENED_LIST) && paramName.equals(param.getName())) {
                            String text = ((CCombo) ctrl).getText();
                            String[] listItemsDisplayName = param.getListItemsDisplayName();
                            boolean valueSet = false;
                            Object[] listItemsValue = param.getListItemsValue();
                            for (int j = 0; j < listItemsValue.length; j++) {
                                if (j < listItemsDisplayName.length && text.equals(listItemsDisplayName[j])) {
                                    value = (String) listItemsValue[j];
                                    valueSet = true;
                                    break;
                                }
                            }
                            if (!valueSet) {
                                value = text;
                            }
                            // TDI-21313
                            if (value != null && value.equals(param.getValue())) {
                                return null;
                            }
                            param.setValue(value);

                        }
                    }
                    return new PropertyChangeCommand(elem, paramName, value);
                }
            }
        }
        return null;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        String paramName = param.getName();
        CCombo combo = (CCombo) hashCurControls.get(paramName);

        if (combo == null || combo.isDisposed()) {
            return;
        }

        Object value = param.getValue();

        if (value instanceof String) {
            String strValue = ""; //$NON-NLS-1$
            boolean strValueSet = false;
            int nbInList = 0, nbMax = param.getListItemsValue().length;
            String name = (String) value;
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(param.getListItemsValue()[nbInList])) {
                    strValue = (String) param.getListItemsDisplayName()[nbInList];
                    strValueSet = true;
                }
                nbInList++;
            }
            if (!strValueSet) {
                strValue = name;
            }
            String[] paramItems = getListToDisplay(param);
            String[] comboItems = combo.getItems();

            if (!Arrays.equals(paramItems, comboItems)) {
                combo.setItems(paramItems);
            }
            if (param.isRepositoryValueUsed()) {
                combo.removeModifyListener(modifySelection);
                combo.setText(strValue);
                combo.addModifyListener(modifySelection);
            } else {
                combo.setText(strValue);
            }
            combo.setVisible(true);
        }

        if (param.isContextMode()) {
            combo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
            combo.setEnabled(false);
        }

    }

    /**
     * DOC zli Comment method "getListToDisplay".
     *
     * @param param
     * @return
     */
    private String[] getListToDisplay(IElementParameter param) {
        Object value = param.getValue();
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();

        boolean addedValue = true;
        Object[] listItemsValue = param.getListItemsValue();
        for (int i = 0; i < listItemsValue.length; i++) {
            if (listItemsValue[i].equals(value)) {
                addedValue = false;
                break;
            }
        }
        if (addedValue) {
            stringToDisplay.add(value.toString());
        } else {
            stringToDisplay.add(new String());
        }

        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (int i = 0; i < originalList.length; i++) {
                stringToDisplay.add(originalList[i]);
            }
        }
        return stringToDisplay.toArray(new String[0]);

    }
}
