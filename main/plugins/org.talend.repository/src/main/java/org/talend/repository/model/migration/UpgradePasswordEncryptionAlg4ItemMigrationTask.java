// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.service.INOSQLService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.migration.EncryptPasswordInComponentsMigrationTask.FakeNode;
import org.talend.utils.security.PasswordMigrationUtil;

public class UpgradePasswordEncryptionAlg4ItemMigrationTask extends UnifyPasswordEncryption4ItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);
        toReturn.addAll(getAllMetaDataType());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess2());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfTestContainer());
        toReturn.add(ERepositoryObjectType.JDBC);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        boolean modified = false;
        try {
            if (item instanceof ContextItem) {
                modified = updateContextItem((ContextItem) item);
            } else if (item instanceof ConnectionItem) {
                modified = updateConnectionItem((ConnectionItem) item);
            } else if (item instanceof ProcessItem) {
                ProcessItem processItem = (ProcessItem) item;
                modified = updateProcessItem(item, processItem.getProcess());
            } else if (item instanceof JobletProcessItem) {
                JobletProcessItem jobletItem = (JobletProcessItem) item;
                modified = updateProcessItem(item, jobletItem.getJobletProcess());
            }
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
            return ExecutionResult.FAILURE;
        }

        if (modified) {
            try {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean updateConnectionItem(ConnectionItem connectionItem) throws Exception {
        boolean modified = false;
        if (connectionItem != null && connectionItem.getConnection() != null && !connectionItem.getConnection().isContextMode()) {
            Connection connection = connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                modified = updateDatabaseConnection((DatabaseConnection) connection);
            } else if (connection instanceof FTPConnection) {
                modified = updateFTPConnection((FTPConnection) connection);
            } else if (connection instanceof MDMConnection) {
                modified = updateMDMConnection((MDMConnection) connection);
            } else if (connection instanceof SalesforceSchemaConnection) {
                modified = updateSalesforceSchemaConnection((SalesforceSchemaConnection) connection);
            } else if (connection instanceof LDAPSchemaConnection) {
                modified = updateLDAPSchemaConnection((LDAPSchemaConnection) connection);
            } else if (connection instanceof SAPConnection) {
                modified = updateSAPConnection((SAPConnection) connection);
            } else if (isNoSqlConnection(connection)) {
                modified = updateNoSqlConnection(connection);
            }
        }
        return modified;
    }
    
    protected boolean updateNoSqlConnection(Connection connection) throws Exception {
        INOSQLService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INOSQLService.class)) {
            service = (INOSQLService) GlobalServiceRegister.getDefault().getService(INOSQLService.class);
            if (service != null) {
                return service.updateNoSqlConnection(connection);
            }
        }
        return false;
    }
    
    private boolean isNoSqlConnection(Connection connection) {
        INOSQLService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INOSQLService.class)) {
            service = (INOSQLService) GlobalServiceRegister.getDefault().getService(INOSQLService.class);
            if (service != null) {
                return service.isNoSQLConnection(connection);
            }
        }
        return false;
    }

    protected boolean updateDatabaseConnection(DatabaseConnection dbConnection) throws Exception {
        String pass = dbConnection.getPassword();
        if (pass != null) {
            dbConnection.setPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(dbConnection.getPassword()));
            return true;
        }
        return false;
    }

    protected boolean updateFTPConnection(FTPConnection ftpConn) throws Exception {
        boolean modified = false;
        if (ftpConn.getPassword() != null) {
            String password = PasswordMigrationUtil.encryptPasswordIfNeeded(ftpConn.getPassword());
            ftpConn.setPassword(password);
            modified = true;
        }
        if (ftpConn.getPassphrase() != null) {
            String password = PasswordMigrationUtil.encryptPasswordIfNeeded(ftpConn.getPassphrase());
            ftpConn.setPassphrase(password);
            modified = true;
        }
        if (ftpConn.getKeystorePassword() != null) {
            String password = PasswordMigrationUtil.encryptPasswordIfNeeded(ftpConn.getKeystorePassword());
            ftpConn.setKeystorePassword(password);
            modified = true;
        }
        if (ftpConn.getProxypassword() != null) {
            String password = PasswordMigrationUtil.encryptPasswordIfNeeded(ftpConn.getProxypassword());
            ftpConn.setProxypassword(password);
            modified = true;
        }
        return modified;
    }

    protected boolean updateMDMConnection(MDMConnection mdmConn) throws Exception {
        String pass = mdmConn.getPassword();
        mdmConn.setPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(pass));
        return true;
    }

    protected boolean updateSalesforceSchemaConnection(SalesforceSchemaConnection ssConn) throws Exception {
        ssConn.setPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(ssConn.getPassword()));
        ssConn.setProxyPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(ssConn.getProxyPassword()));
        ssConn.setConsumeSecret(PasswordMigrationUtil.encryptPasswordIfNeeded(ssConn.getConsumeSecret()));
        return true;
    }

    protected boolean updateLDAPSchemaConnection(LDAPSchemaConnection ldapConn) throws Exception {
        String pass = ldapConn.getBindPassword();
        ldapConn.setBindPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(pass));
        return true;
    }

    protected boolean updateSAPConnection(SAPConnection sapConn) throws Exception {
        String pass = sapConn.getPassword();
        sapConn.setPassword(PasswordMigrationUtil.encryptPasswordIfNeeded(pass));
        return true;
    }

    private boolean updateProcessItem(Item item, ProcessType processType) throws Exception {
        EmfHelper.visitChilds(processType);

        boolean modified = false;
        // context
        if (checkContext(item, processType.getContext())) {
            modified = true;
        }

        // job settings
        if (checkJobsettintsParameter(item, processType)) {
            modified = true;
        }

        // nodes parameters
        if (checkNodes(item, processType)) {
            modified = true;
        }
        return modified;
    }

    private boolean updateContextItem(ContextItem contextItem) throws Exception {
        List<ContextType> contextTypeList = contextItem.getContext();
        boolean modify = false;
        if (contextTypeList != null) {
            for (ContextType type : contextTypeList) {
                List<ContextParameterType> paramTypes = type.getContextParameter();
                if (paramTypes != null) {
                    for (ContextParameterType param : paramTypes) {
                        String value = param.getValue();
                        if (value != null && PasswordEncryptUtil.isPasswordType(param.getType())) {
                            String decryptValue = PasswordMigrationUtil.decryptPassword(value);
                            if (decryptValue != null) {
                                param.setRawValue(decryptValue);
                            }
                            modify = true;
                        }
                    }
                }
            }
        }
        return modify;
    }

    protected boolean checkContext(Item item, List<ContextType> contextTypeList) throws Exception {
        boolean modified = false;
        for (Object o : contextTypeList) {
            if (o instanceof ContextType) {
                List<ContextParameterType> paramTypes = ((ContextType) o).getContextParameter();
                if (paramTypes != null) {
                    for (ContextParameterType param : paramTypes) {
                        String value = param.getValue();
                        if (value != null && (PasswordEncryptUtil.isPasswordType(param.getType())
                                || PasswordEncryptUtil.isPasswordField(param.getName()))) {
                            if (reencryptValueIfNeeded(param)) {
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        return modified;
    }

    protected boolean checkJobsettintsParameter(Item item, ProcessType processType) throws Exception {
        boolean modified = false;

        ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            for (Object p : parameters.getElementParameter()) {
                if (p instanceof ElementParameterType) {
                    ElementParameterType param = (ElementParameterType) p;
                    // variable name used for Stat&Logs
                    if ("PASS".equals(param.getName()) || EParameterFieldType.PASSWORD.getName().equals(param.getField())) { //$NON-NLS-1$
                        if (reencryptValueIfNeeded(param)) {
                            modified = true;
                        }
                    }

                    // variable name used for implicit context
                    if ("PASS_IMPLICIT_CONTEXT".equals(param.getName())) { //$NON-NLS-1$
                        if (reencryptValueIfNeeded(param)) {
                            modified = true;
                        }
                    }
                }
            }
        }
        return modified;
    }

    protected boolean checkNodesFromEmf(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        for (Object nodeObject : processType.getNode()) {
            NodeType nodeType = (NodeType) nodeObject;
            for (Object paramObjectType : nodeType.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObjectType;
                if (param.getField() != null) {
                    if (EParameterFieldType.PASSWORD.getName().equals(param.getField()) && param.getValue() != null) {
                        if (reencryptValueIfNeeded(param)) {
                            modified = true;
                        }
                    }
                }
            }
        }
        return modified;
    }

    protected boolean checkNodes(Item item, ProcessType processType) throws Exception {
        boolean modified = checkNodesFromEmf(item, processType);

        if (!modified) {
            // some versions of the job doesn't have any field type saved in the job, so we will check from the existing
            // component field type
            ComponentCategory category = ComponentCategory.getComponentCategoryFromItem(item);
            for (Object nodeObjectType : processType.getNode()) {
                NodeType nodeType = (NodeType) nodeObjectType;
                IComponent component = ComponentsFactoryProvider.getInstance().get(nodeType.getComponentName(),
                        category.getName());
                if (component == null) {
                    continue;
                }
                FakeNode fNode = new FakeNode(component);
                for (Object paramObjectType : nodeType.getElementParameter()) {
                    ElementParameterType param = (ElementParameterType) paramObjectType;
                    IElementParameter paramFromEmf = fNode.getElementParameter(param.getName());
                    if (paramFromEmf != null) {
                        if (EParameterFieldType.PASSWORD.equals(paramFromEmf.getFieldType()) && param.getValue() != null) {
                            param.setField(EParameterFieldType.PASSWORD.getName());
                            if (reencryptValueIfNeeded(param)) {
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        return modified;
    }

    protected static boolean reencryptValueIfNeeded(ElementParameterType param) throws Exception {
        String value = param.getValue();
        if (value != null) {
            String decryptValue = PasswordMigrationUtil.decryptPassword(value);
            if (decryptValue != null) {
                param.setRawValue(decryptValue);
            }
            return true;
        }
        return false;
    }

    private boolean reencryptValueIfNeeded(ContextParameterType param) throws Exception {
        String value = param.getValue();
        if (value != null) {
            String decryptValue = PasswordMigrationUtil.decryptPassword(value);
            if (decryptValue != null) {
                param.setRawValue(decryptValue);
            }
            return true;
        }
        return false;
    }

    private List<ERepositoryObjectType> getAllMetaDataType() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>();
        list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        list.add(ERepositoryObjectType.METADATA_FILE_FTP);
        list.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
        list.add(ERepositoryObjectType.METADATA_MDMCONNECTION);
        list.add(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INOSQLService.class)) {
            INOSQLService nosqlService = (INOSQLService) GlobalServiceRegister.getDefault().getService(INOSQLService.class);
            if (nosqlService != null) {
                list.add(nosqlService.getNOSQLRepositoryType());
            }
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 10, 22, 12, 0, 0);
        return gc.getTime();
    }
}
