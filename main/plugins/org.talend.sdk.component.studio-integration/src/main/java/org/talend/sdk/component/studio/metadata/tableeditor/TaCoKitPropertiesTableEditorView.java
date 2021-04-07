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
package org.talend.sdk.component.studio.metadata.tableeditor;

import java.util.Arrays;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorView;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * created by hcyi on Mar 17, 2021
 * Detailled comment
 *
 */
public class TaCoKitPropertiesTableEditorView<B> extends PropertiesTableEditorView<B> {

    public TaCoKitPropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, toolbarVisible, labelVisible);
    }

    @Override
    protected Object getComboBoxCellEditorTypedValue(final TableViewerCreator<B> tableViewerCreator, IElement element,
            IElementParameter currentParam, CellEditor cellEditor, String currentKey, Object originalTypedValue) {
        CCombo combo = (CCombo) cellEditor.getControl();
        int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
        TaCoKitUtil.updateElementParameter(element, currentParam, rowNumber);
        String[] listToDisplay = getItemsToDisplay(element, currentParam, rowNumber);
        if (!Arrays.equals(listToDisplay, ((ComboBoxCellEditor) cellEditor).getItems())) {
            ((ComboBoxCellEditor) cellEditor).setItems(listToDisplay);
        }
        Object returnedValue = 0;
        if (originalTypedValue != null) {
            String[] namesSet = listToDisplay;
            for (int j = 0; j < namesSet.length; j++) {
                if (namesSet[j].equals(originalTypedValue)) {
                    returnedValue = j;
                    break;
                }
            }
        }
        return returnedValue;
    }

    @Override
    protected void fillDefaultItemsList(IElementParameter currentParam, Object originalValue) {
        String[] listItems = currentParam.getListItemsDisplayName();
        if (!Arrays.asList(listItems).contains(originalValue)) {
            TaCoKitUtil.fillDefaultItemsList(currentParam, originalValue);
        }
    }
}
