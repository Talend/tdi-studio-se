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
package org.talend.repository.generic.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.service.IMetadataManagmentService;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.SubContainer;

/**
 *
 * created by ycbai on 2014-6-16 Detailled comment
 *
 */
public class GenericUpdateManager extends RepositoryUpdateManager {

    private ConnectionItem connectionItem;

    private List<IMetadataTable> oldMetadataTable;

    public GenericUpdateManager(ConnectionItem connectionItem, List<IMetadataTable> oldMetadataTable, List<Relation> relations) {
        super(connectionItem, relations);
        this.connectionItem = connectionItem;
        this.oldMetadataTable = oldMetadataTable;
    }

    public static boolean updateGenericConnection(ConnectionItem connection, List<IMetadataTable> oldMetadataTable) {
        return updateGenericConnection(connection, oldMetadataTable, true, false);
    }

    public static boolean updateGenericConnection(ConnectionItem connectionItem, List<IMetadataTable> oldMetadataTable,
            boolean show, final boolean onlySimpleShow) {
        List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(connectionItem.getProperty().getId(),
                RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.PROPERTY_RELATION);
        RepositoryUpdateManager repositoryUpdateManager = new GenericUpdateManager(connectionItem, oldMetadataTable, relations);
        return repositoryUpdateManager.doWork(true, false);
    }

    public static Map<String, String> getOldTableIdAndNameMap(ConnectionItem connItem, MetadataTable metadataTable,
            boolean creation) {
        Map<String, String> oldTableMap = getTableIdAndNameMap(connItem);
        if (creation && metadataTable != null) {
            oldTableMap.remove(metadataTable.getId());
        }
        return oldTableMap;
    }

    public static Map<String, String> getTableIdAndNameMap(ConnectionItem connItem) {
        if (connItem == null) {
            return Collections.emptyMap();
        }
        Map<String, String> idAndNameMap = new HashMap<>();
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(connItem.getConnection(), SubContainer.class);
        for (MetadataTable table : metadataTables) {
            idAndNameMap.put(table.getId(), table.getLabel());
        }
        return idAndNameMap;
    }

    public static boolean updateSingleSchema(ConnectionItem connItem, final MetadataTable newTable,
            final IMetadataTable oldMetadataTable, Map<String, String> oldTableMap) {
        if (connItem == null) {
            return false;
        }
        Map<String, String> schemaRenamedMap = getSchemaRenamedMap(connItem, oldTableMap);
        boolean update = !schemaRenamedMap.isEmpty();
        if (!update) {
            if (newTable != null && oldMetadataTable != null && oldTableMap.containsKey(newTable.getId())) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                    IMetadataManagmentService service = (IMetadataManagmentService) GlobalServiceRegister.getDefault()
                            .getService(IMetadataManagmentService.class);
                    IMetadataTable newMetadataTable = service.convertMetadataTable(newTable);
                    update = !oldMetadataTable.sameMetadataAs(newMetadataTable, IMetadataColumn.OPTIONS_NONE);
                    isAddColumn = isAddColumn(newMetadataTable, oldMetadataTable);
                }
            }
        }
        if (update) {
            return updateSchema(newTable, connItem, schemaRenamedMap, true, false);
        }
        return false;
    }

    public static Map<String, String> getSchemaRenamedMap(ConnectionItem connItem, Map<String, String> oldTableMap) {
        if (connItem == null || oldTableMap == null) {
            return Collections.emptyMap();
        }
        Map<String, String> schemaRenamedMap = new HashMap<>();
        final String prefix = connItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(connItem.getConnection(), SubContainer.class);
        for (MetadataTable table : metadataTables) {
            String oldName = oldTableMap.get(table.getId());
            String newName = table.getLabel();
            if (oldName != null && !oldName.equals(newName)) {
                schemaRenamedMap.put(prefix + oldName, prefix + newName);
            }
        }
        return schemaRenamedMap;
    }

    public static List<IMetadataTable> getConversionMetadataTables(Connection conn) {
        if (conn == null) {
            return Collections.emptyList();
        }
        List<IMetadataTable> tables = new ArrayList<>();
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(conn, SubContainer.class);
        for (MetadataTable metadataTable : metadataTables) {
            tables.add(ConvertionHelper.convert(metadataTable));
        }
        return tables;
    }

    @Override
    public Map<String, EUpdateResult> getDeletedOrReselectTablesMap() {
        Map<String, EUpdateResult> drMap = new HashMap<>();
        List<IMetadataTable> newTables = getConversionMetadataTables(connectionItem.getConnection());
        for (IMetadataTable oldTable : oldMetadataTable) {
            String prefix = connectionItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
            boolean isDeleted = true;
            String oldtableLabel = oldTable.getLabel();
            String oldtableId = oldTable.getId();
            for (IMetadataTable newTable : newTables) {
                String tableLabel = newTable.getLabel();
                String tableId = newTable.getId();
                if (tableLabel.equals(oldtableLabel)) {
                    isDeleted = false;
                    /* if table name is same but tableId is not same,means table has been deselect and reselect */
                    if (!tableId.equals(oldtableId)) {
                        // don't handle reload for generic
                        // drMap.put(prefix + tableLabel, EUpdateResult.RELOAD);
                    }
                }
            }
            /* if can't find the name when looping the new tables,means the table has been removed */
            if (isDeleted) {
                drMap.put(prefix + oldtableLabel, EUpdateResult.DELETE);
            }
        }
        return drMap;
    }

    @Override
    public Set<EUpdateItemType> getTypes() {
        Set<EUpdateItemType> types = new HashSet<>();
        types.add(EUpdateItemType.NODE_PROPERTY);
        types.add(EUpdateItemType.NODE_SCHEMA);
        return types;
    }

}
