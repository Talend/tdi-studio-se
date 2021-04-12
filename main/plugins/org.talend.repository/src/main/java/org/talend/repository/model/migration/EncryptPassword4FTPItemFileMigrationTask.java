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
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * created by hcyi on Jul 8, 2014 Detailled comment
 *
 */
public class EncryptPassword4FTPItemFileMigrationTask extends AbstractItemMigrationTask {

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof FTPConnectionItem) {
            FTPConnectionItem item1 = (FTPConnectionItem) item;
            Connection connection = item1.getConnection();
            if (connection instanceof FTPConnection) {
                FTPConnection ftpConn = (FTPConnection) connection;
                try {
                    if (encryptPassword(ftpConn)) {
                        factory.save(item, true);
                    }
                } catch (Exception e1) {
                    ExceptionHandler.process(e1);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    public boolean encryptPassword(FTPConnection ftpConn) throws Exception {
        boolean modified = false;
        if (ftpConn.getPassword() != null) {
            String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(ftpConn.getPassword()));
            ftpConn.setPassword(password + PasswordEncryptUtil.ENCRYPT_KEY);
            modified = true;
        }

        if (ftpConn.getPassphrase() != null) {
            String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(ftpConn.getPassphrase()));
            ftpConn.setPassphrase(password + PasswordEncryptUtil.ENCRYPT_KEY);
            modified = true;
        }

        if (ftpConn.getKeystorePassword() != null) {
            String password = PasswordEncryptUtil
                    .encryptPassword(ConnectionHelper.getCleanPassword(ftpConn.getKeystorePassword()));
            ftpConn.setKeystorePassword(password + PasswordEncryptUtil.ENCRYPT_KEY);
            modified = true;
        }

        if (ftpConn.getProxypassword() != null) {
            String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(ftpConn.getProxypassword()));
            ftpConn.setProxypassword(password + PasswordEncryptUtil.ENCRYPT_KEY);
            modified = true;
        }
        return modified;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_FILE_FTP);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 7, 8, 12, 0, 0);
        return gc.getTime();
    }
}
