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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC PLV class global comment. Detailled comment
 */
public class AddDBConnectionVersionMigrationTask extends AbstractJobMigrationTask {

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    /**
     * DOC PLV AddDBConnectionVersionMigrationTask constructor comment.
     */
    public AddDBConnectionVersionMigrationTask() {
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
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 11, 14, 17, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = (DatabaseConnection) connection;
                if (EDatabaseTypeName.PSQL.equals(EDatabaseTypeName.getTypeFromDbType(dbConnection.getDatabaseType()))) {
                    boolean modified = false;
                    if (dbConnection.getDbVersionString() == null) {
                        dbConnection.setDbVersionString(EDatabaseVersion4Drivers.PSQL_PRIOR_TO_V9.getVersionValue());
                        modified = true;
                    }
                    if (modified) {
                        try {
                            factory.save(item, true);
                            return ExecutionResult.SUCCESS_WITH_ALERT;
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        }
                    }
                }
            }

        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
