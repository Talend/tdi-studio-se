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

package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * For bug 0014214: change the name of flow connection in salesforceOutput from FLOW to MAIN
 */
public class RenameConnectionNameofSalesforceOutput extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            renameConnections(item, processType);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private void renameConnections(Item item, ProcessType processType) throws PersistenceException {

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        for (Object o : processType.getConnection()) {

            ConnectionType currentConnection = (ConnectionType) o;

            if ("FLOW".equals(currentConnection.getConnectorName())) {//$NON-NLS-1$
                String nodeUniqueName = currentConnection.getSource();

                for (Object n : processType.getNode()) {
                    NodeType node = (NodeType) n;
                    if ("tSalesforceOutput".equals(node.getComponentName())) {//$NON-NLS-1$

                        for (Object e : node.getElementParameter()) {
                            ElementParameterType p = (ElementParameterType) e;
                            if ("UNIQUE_NAME".equals(p.getName()) && nodeUniqueName.equals(p.getValue())) {//$NON-NLS-1$
                                currentConnection.setConnectorName("MAIN");//$NON-NLS-1$
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        if (modified) {
            factory.save(item, true);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 7, 13, 17, 0, 0);
        return gc.getTime();
    }

}
