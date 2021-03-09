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
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.migration.IProjectMigrationTask;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class RenameAccessDbVersionInConnection extends AbstractItemMigrationTask implements IProjectMigrationTask {

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);

        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = (DatabaseConnection) connection;
                if (EDatabaseTypeName.ACCESS.equals(EDatabaseTypeName.getTypeFromDbType(dbConnection.getDatabaseType()))) {
                    boolean modified = false;
                    if (EDatabaseVersion4Drivers.ACCESS_2003.getVersionValue().toUpperCase().equals(
                            dbConnection.getDbVersionString())) {
                        dbConnection.setDbVersionString(EDatabaseVersion4Drivers.ACCESS_2003.getVersionValue());
                        modified = true;
                    } else if (EDatabaseVersion4Drivers.ACCESS_2007.getVersionValue().toUpperCase().equals(
                            dbConnection.getDbVersionString())) {
                        dbConnection.setDbVersionString(EDatabaseVersion4Drivers.ACCESS_2007.getVersionValue());
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

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 6, 5, 12, 0, 0);
        return gc.getTime();
    }

}
