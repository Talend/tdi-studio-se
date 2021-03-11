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
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

/**
 * For feature 0000949: Encryption of DB passwords in XMI repository files
 */
public class EncryptDbPasswordMigrationTask extends AbstractItemMigrationTask {

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        toReturn.add(ERepositoryObjectType.CONTEXT);
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.PROCESS);
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
            return encryptItem((DatabaseConnectionItem) item);
        } else if (item instanceof ContextItem) {
            List<ContextType> contextTypeList = ((ContextItem) item).getContext();
            return encryptContextPassword(item, contextTypeList);
        } else if (item instanceof ProcessItem) {
            List<ContextType> contextTypeList = ((ProcessItem) item).getProcess().getContext();
            return encryptContextPassword(item, contextTypeList);

        } else if (item instanceof JobletProcessItem) {
            List<ContextType> contextTypeList = ((JobletProcessItem) item).getJobletProcess().getContext();
            return encryptContextPassword(item, contextTypeList);
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /**
     * DOC chuang Comment method "encryptItem".
     *
     * @param item
     * @return
     */
    private ExecutionResult encryptItem(DatabaseConnectionItem item) {
        Connection conn = item.getConnection();
        if (conn instanceof DatabaseConnection) {
            DatabaseConnection dbConn = (DatabaseConnection) conn;
            if (needEncrypted(dbConn)) {
                try {
                    encryptPassword(dbConn);
                    factory.save(item, true);

                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    private boolean needEncrypted(DatabaseConnection dbConn) {
        return !dbConn.isContextMode() && dbConn.getPassword() != null;
    }

    private void encryptPassword(DatabaseConnection dbConn) throws Exception {
        String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(dbConn.getPassword()));
        dbConn.setPassword(password);
    }

    private void encryptPassword(ContextParameterType param) throws Exception {
        // before migration task, the value should be raw. so keep it.
        String password = PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(param.getValue()));
        param.setValue(password);
    }

    /**
     * DOC chuang Comment method "encryptContextPassword".
     *
     * @param item
     * @param contextTypeList
     * @return
     */
    private ExecutionResult encryptContextPassword(Item item, List<ContextType> contextTypeList) {
        boolean modify = false;
        if (contextTypeList != null) {
            for (ContextType type : contextTypeList) {
                List<ContextParameterType> paramTypes = type.getContextParameter();
                if (paramTypes != null) {
                    for (ContextParameterType param : paramTypes) {
                        try {
                            // before migration task, the value should be raw. so keep it.
                            if (param.getValue() != null && PasswordEncryptUtil.isPasswordType(param.getType())) {
                                encryptPassword(param);
                                modify = true;
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        }
                    }
                }
            }

        }
        if (modify) {
            try {
                factory.save(item, true);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 11, 27, 12, 0, 0);
        return gc.getTime();
    }

}
