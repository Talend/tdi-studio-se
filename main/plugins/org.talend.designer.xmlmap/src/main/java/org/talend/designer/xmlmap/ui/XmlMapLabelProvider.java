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
package org.talend.designer.xmlmap.ui;

import org.eclipse.jface.viewers.CellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ComboEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.data.AccessorUtils;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.utils.sql.TalendTypeConvert;

/**
 * created by kongxiaohan on Jul 17, 2015 Detailled comment
 *
 */
public class XmlMapLabelProvider extends DefaultTableLabelProvider {

    /**
     * DOC kongxiaohan DefaultXmlLabelProvider constructor comment.
     *
     * @param tableViewerCreator
     */
    public XmlMapLabelProvider(TableViewerCreatorNotModifiable tableViewerCreator) {
        super(tableViewerCreator);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        String returnValue = null;
        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) this.tableViewerCreator
                .getColumns().get(columnIndex);

        if (column.getLabelProvider() != null) {
            returnValue = column.getLabelProvider().getLabel(element);
        }

        if (returnValue == null) {
            if (column.getDisplayedValue() != null || column.getDefaultDisplayedValue() != null
                    || column.getBeanPropertyAccessors() == null) {
                String defaultValue = column.getDefaultDisplayedValue();
                String imposedDisplayedValue = column.getDisplayedValue();
                if (imposedDisplayedValue != null) {
                    returnValue = imposedDisplayedValue;
                } else if (defaultValue == null) {
                    returnValue = ""; //$NON-NLS-1$
                } else {
                    returnValue = defaultValue;
                }
            } else {
                Object value = AccessorUtils.get(element, column);
                CellEditor cellEditor = column.getCellEditor();
                CellEditorValueAdapter retrieverValue = column.getCellEditorValueAdapter();
                // add for bug TDI-21505
                if (value != null && column.getCellEditorValueAdapter() != null
                        && column.getCellEditorValueAdapter() instanceof ComboEditorValueAdapter) {
                    Object returnObject = column.getCellEditorValueAdapter().getCellEditorTypedValue(column.getCellEditor(),
                            value);
                    if ("-1".equals(returnObject + "")) {
                        return "";
                    }
                }
                if (cellEditor != null && retrieverValue != null && value != null) {
                    if (element instanceof TreeNode) {
                        if ((((TreeNode) element).getNodeType()).equals(NodeType.ELEMENT)) {
                            returnValue = retrieverValue.getColumnText(cellEditor, element, value);
                        }
                    }
                } else if (value != null) {
                    returnValue = String.valueOf(value);
                } else {
                    returnValue = ""; //$NON-NLS-1$
                }
            }
            // return the type
            if (columnIndex == 3) {
                if (element instanceof TreeNode) {
                    TreeNode node = (TreeNode) element;
                    returnValue = TalendTypeConvert.convertToJavaType(node.getType());
                }
            }
        }
        return returnValue;
    }

}
