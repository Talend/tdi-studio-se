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
 * DOC nrousseau class global comment. Detailled comment
 */
public class AddConnectorNameInConnections extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        try {
            boolean modified = addConnectorName(item);
            if (modified) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * DOC nrousseau Comment method "addConnectorName".
     *
     * @param item
     * @return
     */
    private boolean addConnectorName(Item item) throws PersistenceException {
    	ProcessType processType = getProcessType(item);
		if (processType == null) {
			return false;
		}
		ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;

        for (Object o : processType.getConnection()) {

            ConnectionType currentConnection = (ConnectionType) o;

            if (currentConnection.getConnectorName() == null) {
                EConnectionType connectionType = EConnectionType.getTypeFromId(currentConnection.getLineStyle());
                currentConnection.setConnectorName(connectionType.getName());
                modified = true;
            }

        }
        if (modified) {
            factory.save(item, true);
        }
        return modified;
    }
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
