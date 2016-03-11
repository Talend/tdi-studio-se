// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.update.EUpdateItemType;
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

    public GenericUpdateManager(Object parameter, List<Relation> relations) {
        super(parameter, relations);
    }

    public static boolean updateGenericConnection(ConnectionItem connection) {
        return updateGenericConnection(connection, true, false);
    }

    public static boolean updateGenericConnection(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(connectionItem.getProperty().getId(),
                RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.PROPERTY_RELATION);
        RepositoryUpdateManager repositoryUpdateManager = new GenericUpdateManager(connectionItem, relations);
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

    @Override
    public Set<EUpdateItemType> getTypes() {
        Set<EUpdateItemType> types = new HashSet<>();
        types.add(EUpdateItemType.NODE_PROPERTY);
        return types;
    }

}
