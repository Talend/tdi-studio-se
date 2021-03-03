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
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * DOC zli class global comment. Detailled comment For
 *
 * bug 16729: unencrypted passwords in .item files
 */
public class EncryptDbPasswordforItemFileMigrationTask extends AbstractItemMigrationTask {

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem item1 = (DatabaseConnectionItem) item;
            Connection connection = item1.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConn = (DatabaseConnection) connection;
                try {
                    if (!dbConn.isContextMode()) {
                        String pass = dbConn.getPassword();
                        String rawPass = dbConn.getRawPassword();
                        if (pass != null && pass.equals(rawPass)) {
                            encryptPassword(dbConn);
                            factory.save(item, true);
                        }
                    }
                } catch (Exception e1) {
                    ExceptionHandler.process(e1);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    public void encryptPassword(DatabaseConnection dbConn) throws Exception {
        String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(dbConn.getPassword()));
        dbConn.setPassword(password);
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
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 10, 28, 12, 0, 0);
        return gc.getTime();
    }

}
