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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.UniqueNodeNameGenerator;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 * @param <B> bean
 */
public class PropertyTablePasteCommand<B> extends ExtendedTablePasteCommand {

    /**
     * DOC amaumont MetadataPasteCommand constructor comment.
     *
     * @param extendedTable
     * @param validAssignableType
     * @param indexStartAdd
     */
    public PropertyTablePasteCommand(ExtendedTableModel extendedTable, Integer indexStartAdd) {
        super(extendedTable, indexStartAdd);
    }

    /**
     * DOC amaumont MetadataPasteCommand constructor comment.
     *
     * @param extendedTable
     * @param instanceOfType
     */
    public PropertyTablePasteCommand(ExtendedTableModel extendedTable) {
        super(extendedTable);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand#createPastableBeansList(java
     * .util.List)
     */
    @Override
    public List createPastableBeansList(ExtendedTableModel extendedTable, List copiedObjectsList) {

        ArrayList list = new ArrayList();

        IElementParameter param = null;
        INode node = null;
        if (extendedTable instanceof PropertiesTableEditorModel) {
            PropertiesTableEditorModel<B> model = (PropertiesTableEditorModel<B>) extendedTable;
            if (model.getElement() instanceof INode) {
                node = (INode) model.getElement();
                param = model.getElemParameter();
            }
        }

        for (Object current : copiedObjectsList) {
            if (current instanceof HashMap) {
                // create a new column as a copy of this column
                Map<String, Object> clonedRow = (Map<String, Object>) ((HashMap) current).clone();
                if (param != null) {
                    for (String key : clonedRow.keySet()) {
                        Object value = clonedRow.get(key);
                        if (value instanceof String && !"".equals(value)) {
                            IElementParameter childParam = null;
                            for (Object o : param.getListItemsValue()) {
                                if (o instanceof IElementParameter) {
                                    if (((IElementParameter) o).getName().equals(key)) {
                                        childParam = (IElementParameter) o;
                                        break;
                                    }
                                }
                            }
                            if (childParam != null && childParam.getFieldType() == EParameterFieldType.SCHEMA_TYPE) {
                                IMetadataTable originalMetadata = node.getMetadataTable((String) value);
                                IMetadataTable clonedMetadata = originalMetadata.clone();
                                List<String> tableList = new ArrayList<String>();
                                for (IMetadataTable table : node.getMetadataList()) {
                                    tableList.add(table.getTableName());
                                }
                                String newTableName = UniqueNodeNameGenerator.generateUniqueNodeName(
                                        originalMetadata.getTableName(), tableList);
                                clonedMetadata.setTableName(newTableName);
                                clonedMetadata.setLabel(newTableName);
                                node.getMetadataList().add(clonedMetadata);
                                clonedRow.put(key, newTableName);
                            }
                        }
                    }
                }
                list.add(clonedRow);
            }
        }
        return list;
    }

}
