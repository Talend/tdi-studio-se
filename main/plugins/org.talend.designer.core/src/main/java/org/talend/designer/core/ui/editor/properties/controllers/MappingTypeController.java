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
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ComboController.java 1 2006-12-12 涓嬪崍01:58:48 +0000 (涓嬪崍01:58:48) yzhang $
 *
 */
public class MappingTypeController extends AbstractElementPropertySectionController {

    /**
     * DOC dev ColumnListController constructor comment.
     *
     * @param parameterBean
     */
    public MappingTypeController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    private Command createCommand(SelectionEvent event) {
        Set<String> elementsName;
        Control ctrl;
        // Connection repositoryConnection = null;

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
                if (data != null && data.equals(combo.getData(PARAMETER_NAME)) && ctrl instanceof CCombo) {
                    boolean isDisposed = ((CCombo) ctrl).isDisposed();
                    if (!isDisposed && isValueChanged(elem.getPropertyValue(name), ((CCombo) ctrl).getText())) {
                        String value = new String(""); //$NON-NLS-1$
                        for (int i = 0; i < elem.getElementParameters().size(); i++) {
                            IElementParameter param = elem.getElementParameters().get(i);
                            if (param.getName().equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                    }
                                }
                            }
                        }
                        return new PropertyChangeCommand(elem, name, value);
                    }
                }
            }
        }
        return null;
    }

    private boolean isValueChanged(Object o1, String s2) {
        if (o1 == null && s2 == null) {
            return false;
        }

        if (o1 == null) {
            return true;
        } else {
            return !o1.equals(s2);
        }
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
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
        updateMappingList(param); // initialize the data with the mappings list
        String[] originalList = param.getListItemsDisplayName();
        combo.setItems(originalList);
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
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

        if (isInWizard()) {
            labelLabel.setAlignment(SWT.RIGHT);
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
            data.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), currentLabelWidth
                    + ITabbedPropertyConstants.HSPACE);

            data = (FormData) labelLabel.getLayoutData();
            data.right = new FormAttachment(cLayout, 0);
            data.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), 0);

            return labelLabel;
        }

        return cLayout;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org. eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
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
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            // dynamicTabbedPropertySection.updateRepositoryList();
            Command cmd = createCommand(event);
            executeCommand(cmd);
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        CCombo combo = (CCombo) hashCurControls.get(param.getName());

        if (combo == null || combo.isDisposed()) {
            return;
        }
        Object value = param.getValue();

        if (value instanceof String) {
            String strValue = ""; //$NON-NLS-1$
            int nbInList = 0, nbMax = param.getListItemsValue().length;
            String name = TalendQuoteUtils.removeQuotesIfExist((String) value);
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(param.getListItemsValue()[nbInList])) {
                    strValue = param.getListItemsDisplayName()[nbInList];
                }
                nbInList++;
            }
            String[] paramItems = param.getListItemsDisplayName();
            String[] comboItems = combo.getItems();

            if (!paramItems.equals(comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText("".equals(strValue) ? (String) value : strValue);
            if (param.isContextMode()) {
                combo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
                combo.setEnabled(false);
            }
        }
    }

    private void updateMappingList(IElementParameter param) {
        String[] mappings, mappingsId;
        List<String> mappingsList = new ArrayList<String>();
        List<String> mappingsIdList = new ArrayList<String>();
        Dbms[] dbms = MetadataTalendType.getAllDbmsArray();
        boolean toAdd;
        for (int i = 0; i < dbms.length; i++) {
            toAdd = true;
            if (param.getFilter() != null && !param.getFilter().equals("") //$NON-NLS-1$
                    && !param.getFilter().equals(dbms.clone()[i].getProduct())) {
                toAdd = false;
            }
            if (toAdd) {
                mappingsList.add(dbms[i].getLabel());
                mappingsIdList.add(dbms[i].getId());
            }
        }
        mappings = mappingsList.toArray(new String[0]);
        mappingsId = mappingsIdList.toArray(new String[0]);

        param.setListItemsDisplayName(mappings);
        param.setListItemsValue(mappingsId);
    }
}
