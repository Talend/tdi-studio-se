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
package org.talend.designer.rowgenerator.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.impl.ConnectionFactoryImpl;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.extended.command.MetadataPasteCommand;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;

/**
 * chuang class global comment. Detailled comment
 */
public class MetadataExtPasteCommand extends MetadataPasteCommand {

    private FunctionManagerExt functionManagerExt;
    /**
     * chuang MetadataExtPasteCommand constructor comment.
     *
     * @param extendedTable
     */
    public MetadataExtPasteCommand(ExtendedTableModel extendedTable, FunctionManagerExt functionManagerExt) {
        super(extendedTable);
        this.functionManagerExt = functionManagerExt;
    }

    MetadataExtPasteCommand(ExtendedTableModel extendedTable, Integer indexStartAdd, FunctionManagerExt functionManagerExt) {
        super(extendedTable, indexStartAdd);
        this.functionManagerExt = functionManagerExt;
    }

    @Override
    public List createPastableBeansList(ExtendedTableModel extendedTable, List copiedObjectsList) {
        ArrayList list = new ArrayList();
        ArrayList countList = new ArrayList();
        ArrayList<String> labelsExisted = getLabelsExisted(extendedTable);
        for (Object current : copiedObjectsList) {
            if (current instanceof IMetadataColumn) {
                IMetadataColumn copy = ((IMetadataColumn) current).clone();
                String nextGeneratedColumnName = ((MetadataTableEditor) extendedTable)
                        .getNextGeneratedColumnName(copy.getLabel());
                if (labelsExisted.contains(nextGeneratedColumnName)) {
                    nextGeneratedColumnName = validateColumnName(nextGeneratedColumnName, labelsExisted);
                }
                labelsExisted.add(nextGeneratedColumnName);
                copy.setLabel(nextGeneratedColumnName);
                if (copy instanceof MetadataColumnExt) {
                    list.add(copy);
                } else {
                    MetadataColumnExt metadataColumnExt = new MetadataColumnExt((MetadataColumn) copy);
                    metadataColumnExt.setFunction(functionManagerExt.getDefaultFunction(metadataColumnExt,
                            metadataColumnExt.getTalendType()));
                    list.add(metadataColumnExt);
                }
            } else if (current instanceof org.talend.core.model.metadata.builder.connection.MetadataColumn) {
                MetadataTableEditor tableEditor = (MetadataTableEditor) extendedTable;
                org.talend.core.model.metadata.builder.connection.MetadataColumn metadataColumn = (org.talend.core.model.metadata.builder.connection.MetadataColumn) current;
                String nextGeneratedColumnName = metadataColumn.getLabel();
                String tempNewColumnName = ""; //$NON-NLS-1$
                boolean iMetaColumnUnique = false;
                boolean metaColumnUnique = false;
                while (iMetaColumnUnique == false || metaColumnUnique == false) {
                    nextGeneratedColumnName = tableEditor.getNextGeneratedColumnName(nextGeneratedColumnName, null);
                    iMetaColumnUnique = true;
                    metaColumnUnique = false;
                    if (list.size() == 0)
                        metaColumnUnique = true;
                    else {
                        tempNewColumnName = this.getUniqueString(list, nextGeneratedColumnName);
                        if (tempNewColumnName.equals(nextGeneratedColumnName))
                            metaColumnUnique = true;
                        else {
                            metaColumnUnique = false;
                            nextGeneratedColumnName = tempNewColumnName;
                        }
                    }
                }
                org.talend.core.model.metadata.builder.connection.MetadataColumn newColumnCopy = new ConnectionFactoryImpl()
                        .copy(metadataColumn, nextGeneratedColumnName);
                IMetadataColumn copy = (ConvertionHelper.convertToIMetaDataColumn(newColumnCopy)).clone();
                copy.setLabel(nextGeneratedColumnName);
                MetadataColumnExt metadataColumnExt = new MetadataColumnExt((MetadataColumn) copy);
                metadataColumnExt.setFunction(functionManagerExt.getDefaultFunction(metadataColumnExt,
                        metadataColumnExt.getTalendType()));
                list.add(metadataColumnExt);
                countList.add(nextGeneratedColumnName);
            }
        }

        return list;
    }

}
