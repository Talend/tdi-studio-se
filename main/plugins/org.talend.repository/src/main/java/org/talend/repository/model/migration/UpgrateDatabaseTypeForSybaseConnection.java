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
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class UpgrateDatabaseTypeForSybaseConnection extends AbstractItemMigrationTask {

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 12, 30, 18, 46, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {

        try {
            if (item instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                Connection connection = dbItem.getConnection();
                if (connection instanceof DatabaseConnection) {
                    DatabaseConnection dbconn = (DatabaseConnection) connection;
                    if (dbconn.getProductId().equals(EDatabaseTypeName.SYBASEASE.getProduct())) {
                        dbconn.setDatabaseType(EDatabaseTypeName.SYBASEASE.getDisplayName());
                    }
                }
                ProxyRepositoryFactory.getInstance().save(dbItem, true);
            }
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
