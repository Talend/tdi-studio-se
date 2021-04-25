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
package org.talend.designer.xmlmap.ui.tabs.table;

import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.data.AccessorUtils;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlCellModifier extends DefaultCellModifier {

    private TableViewerCreator tableViewerCreator;

    public XmlCellModifier(TableViewerCreator tableViewerCreator) {
        super(tableViewerCreator);
        this.tableViewerCreator = tableViewerCreator;
    }

    @Override
    public Object getValue(Object bean, String idColumn) {
        if (!AbstractXmlTreeSchemaTableView.ID_COLUMN_XPATH.equals(idColumn)) {
            return super.getValue(bean, idColumn);
        } else {
        	TableViewerCreatorColumnNotModifiable column = tableViewerCreator.getColumn(idColumn);
            ModifiedObjectInfo modifiedObjectInfo = this.tableViewerCreator.getModifiedObjectInfo();
            modifiedObjectInfo.setCurrentModifiedBean(bean);
            modifiedObjectInfo.setCurrentModifiedColumn(column);
            modifiedObjectInfo.setCurrentModifiedIndex(this.tableViewerCreator.getInputList().indexOf(bean));

            Object returnValue = null;
            if (column.getColumnCellModifier() != null) {
                returnValue = column.getColumnCellModifier().getValue(bean);
            }
            if (returnValue == null) {
                Object value = AccessorUtils.get(bean, column);

                if (column.getCellEditorValueAdapter() != null) {
                    returnValue = column.getCellEditorValueAdapter().getCellEditorTypedValue(column.getCellEditor(), value);
                } else {
                    returnValue = value;
                }
                if (returnValue == null && column.getDefaultInternalValue() != null) {
                    returnValue = column.getDefaultInternalValue();
                }
            }
            modifiedObjectInfo.setOriginalPropertyBeanValue(returnValue);
            modifiedObjectInfo.setPreviousPropertyBeanValue(returnValue);
            return returnValue;
        }
    }
}
