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
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class RenameConnectionRunErrorToComponentErrorTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
        try {
            renameConnections(item,processType);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * yzhang Comment method "renameConnections".
     *
     * @param item
     * @param processType
     */
    private void renameConnections(Item item, ProcessType processType) throws PersistenceException {

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        for (Object o : processType.getConnection()) {

            ConnectionType currentConnection = (ConnectionType) o;

            if (currentConnection.getConnectorName().equals("RUN_ERROR")) { //$NON-NLS-1$

                currentConnection.setConnectorName(EConnectionType.ON_COMPONENT_ERROR.getName());
                currentConnection.setLabel(EConnectionType.ON_COMPONENT_ERROR.getDefaultLinkName());
                currentConnection.setLineStyle(EConnectionType.ON_COMPONENT_ERROR.getId());

                modified = true;
            }

        }
        if (modified) {
            factory.save(item,true);
        }

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
