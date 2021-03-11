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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.EncodingTypeChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 * This class is used for adding a selected combo box for encoding type.
 *
 * $Id: EncodingTypeController.java 2007-2-11,上午10:23:00 ftang $
 *
 */
public class EncodingTypeController extends AbstractElementPropertySectionController {

    private static final String NAME_SEPARATOR = "--";

    @Override
    protected Command getTextCommandForHelper(String paramName, String value) {
        return new EncodingTypeChangeCommand(elem, paramName, value, false);
    }

    /**
     *
     */
    private SelectionEvent selectionEvent;

    /**
     * DOC ftang EncodingTypeController constructor comment.
     *
     * @param dtp
     */
    public EncodingTypeController(IDynamicProperty dp) {
        super(dp);
    }

    /**
     * DOC ftang Comment method "createComboCommand".
     *
     * @param combo
     * @return
     */
    private Command createComboCommand(CCombo combo) {
        String value = new String(""); //$NON-NLS-1$
        String paramName = ""; //$NON-NLS-1$
        Object data = combo.getData(PARAMETER_NAME);
        if (data != null) {
            paramName = data.toString();
        }

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getFieldType().equals(EParameterFieldType.ENCODING_TYPE) && paramName.equals(param.getName())) {
                IElementParameter comboParam = param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName());
                for (int j = 0; j < comboParam.getListItemsValue().length; j++) {
                    if (combo.getText().equals(comboParam.getListItemsDisplayName()[j])) {
                        value = (String) comboParam.getListItemsValue()[j];
                    }
                }
            }
        }
        if ("".equals(paramName)) { //$NON-NLS-1$
            return null;
        }

        String tempValue = (String) value;
        if (!EmfComponent.ENCODING_TYPE_CUSTOM.equals(value)) {
            tempValue = tempValue.replaceAll("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
            tempValue = tempValue.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
            tempValue = TalendTextUtils.addQuotes(tempValue);
        }

        return new EncodingTypeChangeCommand(elem, paramName, tempValue, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        CCombo combo;
        Control lastControlUsed = lastControl;

        combo = new CCombo(subComposite, SWT.BORDER);
        IElementParameter encodingTypeParameter = param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName());
        // see 10693 ,only for component tChangeFileEncoding
        if ("INENCODING".equals(param.getName())) {
            addEncodingType(encodingTypeParameter);
        }

        FormData data;
        String[] originalList = encodingTypeParameter.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = encodingTypeParameter.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = encodingTypeParameter.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (encodingTypeParameter.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (int i = 0; i < originalList.length; i++) {
                stringToDisplay.add(originalList[i]);
            }
        }

        combo.setItems(stringToDisplay.toArray(new String[0]));
        combo.setEditable(false);
        combo.setEnabled(!encodingTypeParameter.isReadOnly() && !encodingTypeParameter.isRepositoryValueUsed());
        combo.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + encodingTypeParameter.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, encodingTypeParameter.getDisplayName());
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
        Point labelSize = gc.stringExtent(encodingTypeParameter.getDisplayName());
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
        combo.setLayoutData(data);
        combo.addSelectionListener(selectionChangeListener);
        lastControlUsed = combo;

        String tempValue = (String) param.getValue();
        tempValue = tempValue.replaceAll("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
        tempValue = tempValue.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$

        if (!ArrayUtils.contains(encodingTypeParameter.getListItemsValue(), tempValue)) {
            encodingTypeParameter.setValue(EmfComponent.ENCODING_TYPE_CUSTOM);
        }
        String encodingType = (String) encodingTypeParameter.getValue();

        if (encodingType != null && encodingType.equals(EmfComponent.ENCODING_TYPE_CUSTOM)) {
            lastControlUsed = addCustomEncodingTypeText(subComposite, param, lastControlUsed, numInRow, nbInRow, top);
        }

        // **********************
        hashCurControls.put(param.getName() + NAME_SEPARATOR + encodingTypeParameter.getName(), combo);

        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return lastControlUsed;
    }

    private void addEncodingType(IElementParameter encodingTypeParameter) {
        List<String> newEncodingTypes = Arrays.asList(new String[] { "ISO8859-1", "utf-16", "GB2312" });
        String[] listItemsDisplayCodeName = encodingTypeParameter.getListItemsDisplayCodeName();
        String[] listItemsdisplayName = encodingTypeParameter.getListItemsDisplayName();
        Object[] listItemsValue = encodingTypeParameter.getListItemsValue();
        List<String> newDisplayCodeName = new ArrayList<String>();
        newDisplayCodeName.addAll(Arrays.asList(listItemsDisplayCodeName));
        if (!newDisplayCodeName.containsAll(newEncodingTypes)) {
            newDisplayCodeName.remove(newDisplayCodeName.size() - 1);
            newDisplayCodeName.addAll(newEncodingTypes);
            newDisplayCodeName.add(listItemsDisplayCodeName[listItemsDisplayCodeName.length - 1]);
            encodingTypeParameter.setListItemsDisplayCodeName(newDisplayCodeName.toArray(new String[newDisplayCodeName.size()]));
        }

        List<String> newDisplayNames = new ArrayList<String>();
        newDisplayNames.addAll(Arrays.asList(listItemsdisplayName));
        if (!newDisplayNames.containsAll(newEncodingTypes)) {
            newDisplayNames.remove(newDisplayNames.size() - 1);
            newDisplayNames.addAll(newEncodingTypes);
            newDisplayNames.add(listItemsdisplayName[listItemsdisplayName.length - 1]);
            encodingTypeParameter.setListItemsDisplayName(newDisplayNames.toArray(new String[newDisplayNames.size()]));
        }

        List<Object> newValues = new ArrayList<Object>();
        newValues.addAll(Arrays.asList(listItemsValue));
        if (!newValues.containsAll(newEncodingTypes)) {
            newValues.remove(newValues.size() - 1);
            newValues.addAll(newEncodingTypes);
            newValues.add(listItemsValue[listItemsValue.length - 1]);
            encodingTypeParameter.setListItemsValue(newValues.toArray());
        }
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
        IElementParameter encodingTypeParameter = elem.getElementParameter(EParameterName.ENCODING_TYPE.getName());
        combo.setItems(encodingTypeParameter.getListItemsDisplayName());
        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        combo.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /**
     * DOC ftang Comment method "addCustomEncodingTypeText".
     *
     * @param subComposite
     * @param param
     * @param lastControl
     * @param numInRow
     * @param nbInRow
     * @param top
     * @return
     */
    private Control addCustomEncodingTypeText(Composite subComposite, IElementParameter param, Control lastControl, int numInRow,
            int nbInRow, int top) {
        FormData data;
        Text labelText;
        IElementParameter encodingTypeParameter = param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName());
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired() || EmfComponent.ENCODING_TYPE_CUSTOM.equals(encodingTypeParameter.getValue())) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("TextController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();
        labelText.setData(PARAMETER_NAME, param.getName());

        editionControlHelper.register(param.getName(), labelText);

        if (param.isRepositoryValueUsed()) {
            addRepositoryPropertyListener(labelText);
        }

        addDragAndDropTarget(labelText);

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly() && !param.isRepositoryValueUsed());
        labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        IElementParameter encodingTypeParameter = param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName());
        Object value = encodingTypeParameter.getValue();

        CCombo combo = (CCombo) hashCurControls.get(param.getName() + NAME_SEPARATOR + EParameterName.ENCODING_TYPE.getName());
        if (combo == null || combo.isDisposed()) {
            return;
        }

        if ((value instanceof String)
                && ((hashCurControls.get(param.getName()) == null) || !EmfComponent.ENCODING_TYPE_CUSTOM.equals(combo.getText()))) {
            String strValue = ""; //$NON-NLS-1$
            int nbInList = 0, nbMax = encodingTypeParameter.getListItemsValue().length;
            String name = (String) value;
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(encodingTypeParameter.getListItemsValue()[nbInList])) {
                    strValue = encodingTypeParameter.getListItemsDisplayName()[nbInList];
                }
                nbInList++;
            }
            String[] paramItems = encodingTypeParameter.getListItemsDisplayName();
            String[] comboItems = combo.getItems();
            if (!paramItems.equals(comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText(strValue);
        }

        if (hashCurControls.get(param.getName()) instanceof Text) {
            Text labelText = (Text) hashCurControls.get(param.getName());
            if (labelText == null) {
                return;
            }
            Object value1 = param.getValue();
            boolean valueChanged = false;
            if (value1 == null) {
                labelText.setText(""); //$NON-NLS-1$
            } else {
                if (!value1.equals(labelText.getText())) {
                    labelText.setText((String) value1);
                    valueChanged = true;
                }
            }

            if (checkErrorsWhenViewRefreshed || valueChanged) {
                checkErrorsForPropertiesOnly(labelText);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    /**
     *
     */
    SelectionListener selectionChangeListener = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing.
        }

        public void widgetSelected(SelectionEvent e) {
            selectionEvent = e;
            Command cmd = createComboCommand((CCombo) selectionEvent.getSource());
            executeCommand(cmd);
        }
    };
}
