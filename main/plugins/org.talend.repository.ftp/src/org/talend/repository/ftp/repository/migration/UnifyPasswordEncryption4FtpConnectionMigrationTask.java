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
package org.talend.repository.ftp.repository.migration;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.migration.UnifyPasswordEncryption4ItemMigrationTask;
import org.talend.utils.security.CryptoMigrationUtil;

/**
 * created by ggu on Sep 1, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4FtpConnectionMigrationTask extends UnifyPasswordEncryption4ItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_FILE_FTP);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof FTPConnectionItem) {
            Connection connection = ((FTPConnectionItem) item).getConnection();
            connection.setEncryptAndDecryptFuncPair(CryptoMigrationUtil.encryptFunc(), CryptoMigrationUtil.decryptFunc());
            if (connection instanceof FTPConnection) {
                FTPConnection ftpConn = (FTPConnection) connection;
                try {
                    if (!ftpConn.isContextMode()) {
                        boolean modified = false;
                        //
                        String password = reencryptIfNeed(ftpConn, ftpConn.getPassword());
                        if (password != null) { // re-encrypted
                            ftpConn.setPassword(password);
                            modified = true;
                        }
                        //
                        String passphrase = reencryptIfNeed(ftpConn, ftpConn.getPassphrase());
                        if (passphrase != null) {// re-encrypted
                            ftpConn.setPassphrase(passphrase);
                            modified = true;
                        }
                        //
                        String keystorePassword = reencryptIfNeed(ftpConn, ftpConn.getKeystorePassword());
                        if (keystorePassword != null) {// re-encrypted
                            ftpConn.setKeystorePassword(keystorePassword);
                            modified = true;
                        }
                        //
                        String proxypassword = reencryptIfNeed(ftpConn, ftpConn.getProxypassword());
                        if (proxypassword != null) {// re-encrypted
                            ftpConn.setProxypassword(proxypassword);
                            modified = true;
                        }
                        if (modified) {
                            factory.save(item, true);
                            return ExecutionResult.SUCCESS_NO_ALERT;
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private String reencryptIfNeed(FTPConnection ftpConn, String passValue) throws Exception {
        if (passValue != null) {
            String value = passValue;
            int index = value.lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
            if (index != -1) {
                value = new StringBuilder(value).replace(index, index + PasswordEncryptUtil.ENCRYPT_KEY.length(), "").toString(); //$NON-NLS-1$
                String rawValue = PasswordEncryptUtil.decryptPassword(value);
                return ftpConn.getValue(rawValue, true);
            }
        }
        return null; // if encryption, shouldn't be this.
    }
}
