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
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class MysqlOutputDBVersionForBug13250 extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            boolean modify = false;
            DatabaseConnectionItem mysqlConnItem = (DatabaseConnectionItem) item;
            if (mysqlConnItem.getConnection() instanceof DatabaseConnection) {
                DatabaseConnection mysqlConnection = (DatabaseConnection) mysqlConnItem.getConnection();
                if ("MySQL".equalsIgnoreCase(mysqlConnection.getDatabaseType())) {
                    if (mysqlConnection.getDbVersionString() == null || "".equals(mysqlConnection.getDbVersionString())) {
                        mysqlConnection.setDbVersionString("MYSQL_5");
                        modify = true;
                    }
                }
            }
            if (modify) {
                try {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    factory.save(item, true);
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 5, 21, 17, 0, 0);
        return gc.getTime();
    }

}
