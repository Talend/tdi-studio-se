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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * Migration task added only to update the version of VERTICA_6 to VERTICA_6_0.
 */
public class UpdateDbVersionForVerticaMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 05, 27, 14, 39, 0);
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
            DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
            if (dbItem.getConnection() instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                if (EDatabaseTypeName.VERTICA.equals(EDatabaseTypeName.getTypeFromDbType(dbConnection.getDatabaseType()))) {
                    boolean modified = false;
                    if (dbConnection.getDbVersionString().equals(EDatabaseVersion4Drivers.VERTICA_6.name())) {
                        dbConnection.setDbVersionString(EDatabaseVersion4Drivers.VERTICA_6.getVersionValue());
                        modified = true;
                    }
                    if (modified) {
                        try {
                            ProxyRepositoryFactory.getInstance().save(item, true);
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
