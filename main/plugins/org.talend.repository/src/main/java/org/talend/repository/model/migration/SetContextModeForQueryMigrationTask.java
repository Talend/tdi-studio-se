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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */

public class SetContextModeForQueryMigrationTask extends AbstractItemMigrationTask {

    /*
     * add by hyWang (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        boolean changed = false;
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem connItem = (DatabaseConnectionItem) item;
            DatabaseConnection conn = (DatabaseConnection) connItem.getConnection();
            if (conn.getQueries() == null)
                return ExecutionResult.NOTHING_TO_DO;
            for (Query query : (List<Query>) conn.getQueries().getQuery()) {
                String value = query.getValue();
                if (value != null
                        && (value.startsWith(TalendTextUtils.getQuoteChar()) || !TalendQuoteUtils.isCommonString(value))) {
                    query.setContextMode(true);
                    changed = true;

                }
            }

        }
        if (changed) {
            try {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 1, 7, 12, 0, 0);
        return gc.getTime();
    }

}
