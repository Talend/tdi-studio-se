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

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TAmazonOracleDndFilter extends DefaultRepositoryComponentDndFilter {

    public TAmazonOracleDndFilter() {
        super();
    }

    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (component != null) {
            // for tAmazonOracleInput/Output/Connection/Row
            if (component.getName().startsWith("tAmazonOracle")) { //$NON-NLS-1$
                IProcess process = null;
                Node node = null;
                try {
                    String dbType = null;
                    if (item != null && item instanceof DatabaseConnectionItem) {
                        if (((DatabaseConnectionItem) item).getConnection() instanceof DatabaseConnection) {
                            dbType = ((DatabaseConnection) ((DatabaseConnectionItem) item).getConnection()).getDatabaseType();
                        }
                    }
                    node = new Node(component);
                    process = node.getProcess();
                    if (node != null) {
                        IElementParameter param = node.getElementParameter("CONNECTION_TYPE");//$NON-NLS-1$
                        if (param != null) {
                            Object[] valuesList = param.getListItemsValue();
                            for (Object element : valuesList) {
                                String conType = EDatabaseTypeName.getTypeFromDbType(element.toString()).getDisplayName();
                                if (conType != null && dbType != null && conType.equals(dbType)) {
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                } finally {
                    if (node != null && process instanceof Process) {
                        ((Process) process).removeNodeContainer(new NodeContainer(node));
                    }
                }
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
