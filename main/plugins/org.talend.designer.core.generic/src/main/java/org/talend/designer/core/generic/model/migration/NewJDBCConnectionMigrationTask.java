// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.migration.IMigrationTask.ExecutionResult;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class NewJDBCConnectionMigrationTask extends AbstractJobMigrationTask{

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 12, 19, 15, 0, 0).getTime();
    }
    
    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }
    
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = connection;
                String dbType = dbConnection.getDatabaseType();
                if(dbType == null || !dbType.equals("General JDBC")){
                    return null;
                }
                String jdbcType = "JDBC";
                dbConnection.setDatabaseType(jdbcType);
                connectionItem.setTypeName(jdbcType);
                IGenericDBService dbService = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                    dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                            IGenericDBService.class);
                }
                if(dbService == null){
                    return null; 
                }
                boolean isContextMode = connection.isContextMode();
                Properties properties = dbService.getComponentProperties(jdbcType, dbConnection.getId());
                Property url = (Property) properties.getProperty("connection.jdbcUrl");
                Property driClass = (Property) properties.getProperty("connection.driverClass");
                Property user = (Property) properties.getProperty("connection.userPassword.userId");
                Property pass = (Property) properties.getProperty("connection.userPassword.password");
                Property dirJar = (Property) properties.getProperty("connection.driverTable.drivers");
                Property mappingFile = (Property) properties.getProperty("mappingFile");
                if(url != null){
                    url.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                    url.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, url.getName());
                    url.setValue(connection.getURL());
                }
                if(driClass != null){
                    driClass.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                    driClass.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, driClass.getName());
                    driClass.setValue(connection.getDriverClass());
                }
                if(user != null){
                    user.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                    user.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, user.getName());
                    user.setValue(connection.getUsername());
                }
                if(pass != null){
                    pass.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                    pass.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, pass.getName());
                    String password = connection.getValue(connection.getRawPassword(), false);
                    pass.setValue(password);
                }
                if(mappingFile != null){
                    mappingFile.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
                    mappingFile.setValue(connection.getDbmsId());
                }
                setDrivers(dirJar, connection.getDriverJarPath(), isContextMode);
                connection.setCompProperties(properties.toSerialized());
                try {
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
    
    private void setDrivers(Property dirJar, String jars, boolean isContextMode){
        if(dirJar == null){
            return;
        }
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                    IGenericDBService.class);
        }
        if(dbService == null){
            return;
        }
        List<String> jarList = new ArrayList<>();
        for(String jar : jars.split(";")){
            jarList.add(dbService.getMVNPath(jar));
        }
        dirJar.setValue(jarList);
        dirJar.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, dirJar.getName());
        dirJar.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
    }

}
