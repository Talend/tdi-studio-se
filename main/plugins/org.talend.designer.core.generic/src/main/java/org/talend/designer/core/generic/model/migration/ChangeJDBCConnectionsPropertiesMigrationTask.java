package org.talend.designer.core.generic.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.utils.security.CryptoMigrationUtil;

public class ChangeJDBCConnectionsPropertiesMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 3, 11, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.JDBC);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project,
     * org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            connection.setEncryptAndDecryptFuncPair(CryptoMigrationUtil.encryptFunc(), CryptoMigrationUtil.decryptFunc());
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = connection;
                String dbType = dbConnection.getDatabaseType();
                if (dbType == null || !dbType.equals("JDBC")) {
                    return ExecutionResult.NOTHING_TO_DO;
                }
                String compProperties = connection.getCompProperties();
                if (StringUtils.isEmpty(compProperties)) {
                    dbConnection.setDatabaseType(dbType);
                    connectionItem.setTypeName(dbType);
                    IGenericDBService dbService = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                        dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
                    }
                    if (dbService == null) {
                        return ExecutionResult.NOTHING_TO_DO;
                    }
                    boolean isContextMode = connection.isContextMode();
                    Properties properties = dbService.getComponentProperties(dbType, dbConnection.getId());
                    Property url = (Property) properties.getProperty("connection.jdbcUrl");
                    Property driClass = (Property) properties.getProperty("connection.driverClass");
                    Property user = (Property) properties.getProperty("connection.userPassword.userId");
                    Property pass = (Property) properties.getProperty("connection.userPassword.password");
                    Property dirJar = (Property) properties.getProperty("connection.driverTable.drivers");
                    Property mappingFile = (Property) properties.getProperty("mappingFile");
                    if (url != null) {
                        url.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                        url.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, url.getName());
                        url.setValue(connection.getURL());
                    }
                    if (driClass != null) {
                        driClass.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                        driClass.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, driClass.getName());
                        driClass.setValue(connection.getDriverClass());
                    }
                    if (user != null) {
                        user.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                        user.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, user.getName());
                        user.setValue(connection.getUsername());
                    }
                    if (pass != null) {
                        pass.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                        pass.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, pass.getName());
                        pass.setValue(connection.getRawPassword());
                    }
                    if (mappingFile != null) {
                        mappingFile.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                        mappingFile.setValue(connection.getDbmsId());
                    }
                    setDrivers(dirJar, connection.getDriverJarPath(), isContextMode);
                    connection.setCompProperties(properties.toSerialized());
                    try {
                        if (isContextMode) {
                            String contextId = dbConnection.getContextId();
                            ContextItem contextItem = ContextUtils.getContextItemById2(contextId);
                            if (contextItem != null) {
                                setContextDriversValue(contextItem, connection.getDriverJarPath());
                                factory.save(contextItem, true);
                            }
                        }
                        factory.save(item, true);
                        return ExecutionResult.SUCCESS_WITH_ALERT;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                        return ExecutionResult.FAILURE;
                    }
                }

            }
        }
        return ExecutionResult.NOTHING_TO_DO;

    }

    private void setDrivers(Property dirJar, String jars, boolean isContextMode) {
        if (dirJar == null) {
            return;
        }
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService == null) {
            return;
        }
        List<String> jarList = new ArrayList<>();
        for (String jar : jars.split(";")) {
            jarList.add(dbService.getMVNPath(jar));
        }
        dirJar.setValue(jarList);
        dirJar.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, dirJar.getName());
        dirJar.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
    }

    private void setContextDriversValue(ContextItem contextItem, String paramName) {
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        boolean containContextParam = ContextParameterUtils.isContainContextParam(paramName);
        if (!containContextParam || dbService == null) {
            return;
        }
        paramName = ContextParameterUtils.getContextString(paramName);
        EList contexts = contextItem.getContext();
        for (Object context : contexts) {
            if (context instanceof ContextType) {
                ContextParameterType contextParam = ContextUtils.getContextParameterTypeByName((ContextType) context, paramName);
                if (contextParam == null || StringUtils.isBlank(contextParam.getValue())) {
                    continue;
                }
                StringBuffer jarBuffer = new StringBuffer();
                String[] jars = contextParam.getValue().split(";");
                for (int i = 0; i < jars.length; i++) {
                    if (i != 0) {
                        jarBuffer.append(";");
                    }
                    jarBuffer.append(dbService.getMVNPath(jars[i]));
                }
                contextParam.setValue(jarBuffer.toString());
            }
        }

    }
}
