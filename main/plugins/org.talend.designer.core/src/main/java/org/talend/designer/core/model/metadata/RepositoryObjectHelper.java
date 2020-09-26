// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.metadata;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.utils.UnifiedComponentUtil;

/**
 * ggu class global comment. Detailled comment
 */
public final class RepositoryObjectHelper {

    private static final String COLON = ":"; //$NON-NLS-1$

    private static final String LINE = " - "; //$NON-NLS-1$

    public static String getRepositoryAliasName(Item item) {
        String aliasName = null;
        if (item != null) {
            ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(item);
            aliasName = repositoryObjectType.getAlias();
            if (aliasName == null && repositoryObjectType.getType().equals("SERVICES")) {//$NON-NLS-1$
                aliasName = "SERVICES";//$NON-NLS-1$
            }
            if (aliasName != null && item instanceof ConnectionItem) {
                ConnectionItem connectionItem = (ConnectionItem) item;
                Connection connection = connectionItem.getConnection();
                if (connection instanceof DatabaseConnection) {
                    String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
                    String productId = ((DatabaseConnection) connection).getProductId();
                    if (UnifiedComponentUtil.isAdditionalJDBC(productId)) {
                        currentDbType = productId;
                    }
                    aliasName += " (" + currentDbType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return aliasName;
    }

    public static String getRepositoryLabel(Item item) {
        String repositoryAliasName = getRepositoryAliasName(item);
        if (repositoryAliasName != null) {
            return repositoryAliasName + COLON + item.getProperty().getLabel();
        }
        return null;
    }

    public static String getRepositoryLabel(Item item, Query query) {
        String repositoryLabel = getRepositoryLabel(item);
        if (repositoryLabel != null) {
            return repositoryLabel + LINE + query.getLabel();
        }
        return null;
    }

    public static String getRepositoryLabel(Item item, IMetadataTable table) {
        String repositoryLabel = getRepositoryLabel(item);
        if (repositoryLabel != null && table != null) {
            if (table.getLabel() != null) {
                repositoryLabel += LINE + table.getLabel();
            } else if (table.getTableName() != null) {
                repositoryLabel += LINE + table.getTableName();
            }
            return repositoryLabel;
        }
        return null;

    }
}
