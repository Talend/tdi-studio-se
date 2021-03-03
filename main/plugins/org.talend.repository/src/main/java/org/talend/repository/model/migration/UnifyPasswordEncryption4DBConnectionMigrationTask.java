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
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * created by ggu on Aug 21, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4DBConnectionMigrationTask extends UnifyPasswordEncryption4ItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            Connection connection = ((DatabaseConnectionItem) item).getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConn = (DatabaseConnection) connection;
                try {
                    if (!dbConn.isContextMode()) {
                        String pass = dbConn.getPassword();
                        String rawPass = null;
                        pass = cleanPassword(pass);
                        try {
                            rawPass = PasswordEncryptUtil.decryptPassword(pass);
                        } catch (Exception e) {
                            rawPass = pass;
                        }
                        // decrypt the old password.
                        dbConn.setRawPassword(rawPass);
                        factory.save(item, true);
                        return ExecutionResult.SUCCESS_NO_ALERT;
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
