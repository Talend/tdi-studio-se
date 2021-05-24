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
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeAS400DBVersionMigrationTask extends AbstractItemMigrationTask {

    public final static String NOTEXIST = "V5R3 to V6R1";

    public final static String EXIST = "V5R4 to V6R1";

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 8, 23, 18, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ConnectionItem connectionItem = (ConnectionItem) item;
        DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
        if (connection != null) {
        if (connection.getDatabaseType().equals("AS400")) {
            if (connection.getDbVersionString() != null && connection.getDbVersionString().equals(NOTEXIST)) {
                connection.setDbVersionString(EXIST);
                try {
                    ProxyRepositoryFactory.getInstance().save(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } else {
            return ExecutionResult.NOTHING_TO_DO;
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

}
