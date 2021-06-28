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
package org.talend.designer.core.ui.editor.process;

import java.util.Arrays;
import java.util.List;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TAmazonOracleDndFilter extends DefaultRepositoryComponentDndFilter {

    private List<String> componentList;

    public TAmazonOracleDndFilter() {
        super();
        componentList = Arrays.asList("tAmazonOracleInput", "tAmazonOracleOutput", "tAmazonOracleConnection", "tAmazonOracleRow"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (component != null) {
            // for tAmazonOracleInput/Output/Connection/Row
            if (componentList.contains(component.getName())) {
                String dbType = null;
                if (item != null && item instanceof DatabaseConnectionItem) {
                    if (((DatabaseConnectionItem) item).getConnection() instanceof DatabaseConnection) {
                        dbType = ((DatabaseConnection) ((DatabaseConnectionItem) item).getConnection()).getDatabaseType();
                        String conType = EDatabaseTypeName.getTypeFromDbType("ORACLE_SID").getDisplayName(); //$NON-NLS-1$
                        if (conType != null && dbType != null && conType.equals(dbType)) {
                            return false;
                        }
                    }
                }
                return true;
            } else if (("tOracleCDCOutput").equals(component.getName())) { //$NON-NLS-1$
                if (item != null && item instanceof DatabaseConnectionItem) {
                    if (((DatabaseConnectionItem) item).getConnection() instanceof DatabaseConnection) {
                        DatabaseConnection connection = ((DatabaseConnection) ((DatabaseConnectionItem) item).getConnection());
                        if (MetadataConnectionUtils.isOracle(connection)) {
                            String version = connection.getDbVersionString();
                            if (EDatabaseVersion4Drivers.ORACLE_12.name().equals(version)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean valid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        return false; // no valid component
    }

}
