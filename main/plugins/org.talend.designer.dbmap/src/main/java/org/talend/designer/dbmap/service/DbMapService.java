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
package org.talend.designer.dbmap.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.service.IDbMapService;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DbMapService implements IDbMapService {

    @Override
    public boolean isDbMapComponent(IExternalNode node) {
        if (node instanceof DbMapComponent) {
            return true;
        }
        return false;
    }

    @Override
    public void updateEMFDBMapData(NodeType nodeType, String oldValue, String newValue) {
        AbstractExternalData nodeData = nodeType.getNodeData();
        if (nodeData instanceof DBMapData) {
            DBMapData dbMapData = (DBMapData) nodeData;
            // may have several tables with different aliases.
            boolean isAliasIncludeTableName = false;
            for (InputTable input : dbMapData.getInputTables()) {
                if (input.getName().equals(oldValue) || input.getTableName().equals(oldValue)) {
                    input.setName(newValue);
                    input.setTableName(newValue);
                    String alias = input.getAlias();
                    if (alias != null) {
                        if (alias.contains(oldValue)) {
                            isAliasIncludeTableName = true;
                        }
                    }
                }
            }

            // do this when no alias in expression(or has alias which alias = old tableName, when tableName change to new, don't change alias in expression)
            for (OutputTable output : dbMapData.getOutputTables()) {
                List<DBMapperTableEntry> entries = output.getDBMapperTableEntries();
                for (DBMapperTableEntry entry : entries) {
                    String expression = entry.getExpression();
                    if (!StringUtils.isBlank(expression) && expression.contains(oldValue) && !isAliasIncludeTableName) {
                        entry.setExpression(expression.replace(oldValue, newValue));
                    }
                }
            }
        }

    }

    @Override
    public void undoConnectionDelete(IExternalNode node, AbstractExternalData oldEmfData, String connectionLabel) {
        if (oldEmfData instanceof DBMapData && node instanceof DbMapComponent) {
            DbMapComponent dbMapCDBMapDataomp = (DbMapComponent) node;
            DBMapData emfData = (DBMapData) oldEmfData;
            for (int i = 0; i < emfData.getInputTables().size(); i++) {
                InputTable inputTable = emfData.getInputTables().get(i);
                if (inputTable.getTableName().equals(connectionLabel)) {
                    ExternalDbMapTable externalTable = new ExternalDbMapTable();
                    externalTable.setName(inputTable.getName());
                    externalTable.setMinimized(inputTable.isMinimized());
                    externalTable.setAlias(inputTable.getAlias());
                    externalTable.setJoinType(inputTable.getJoinType());
                    externalTable.setTableName(inputTable.getTableName());
                    List<ExternalDbMapEntry> entities = new ArrayList<ExternalDbMapEntry>();
                    for (DBMapperTableEntry pEntity : inputTable.getDBMapperTableEntries()) {
                        ExternalDbMapEntry entity = new ExternalDbMapEntry();
                        entity.setExpression(pEntity.getExpression());
                        entity.setJoin(pEntity.isJoin());
                        entity.setName(pEntity.getName());
                        entity.setOperator(pEntity.getOperator());
                        entities.add(entity);
                    }
                    externalTable.setMetadataTableEntries(entities);
                    dbMapCDBMapDataomp.getExternalData().getInputTables().add(i, externalTable);
                    break;
                }
            }
        }

    }

}
