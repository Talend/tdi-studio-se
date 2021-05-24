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
package org.talend.designer.core.generic.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class RevertJDBCContextModeMappingFileValueMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 11, 16, 15, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        toReturn.add(ERepositoryObjectType.JDBC);
        return toReturn;
    }


    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = connection;
                String dbType = dbConnection.getDatabaseType();
                String mappingId = connection.getDbmsId();
                if (dbType == null || !dbType.equals("JDBC") || !connection.isContextMode()
                        || StringUtils.isBlank(connection.getContextId())
                        || !ContextParameterUtils.containContextVariables(mappingId)) {
                    return ExecutionResult.NOTHING_TO_DO;
                }

                String compPropertiesStr = connection.getCompProperties();
                Properties compProperties = ComponentsUtils.getComponentPropertiesFromSerialized(compPropertiesStr, connection);

                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                try {
                    IRepositoryViewObject contextObject = factory.getLastVersion(connection.getContextId());
                    if (contextObject == null) {
                        return ExecutionResult.NOTHING_TO_DO;
                    }
                    String parameterValue = null;
                    Item contextItem = contextObject.getProperty().getItem();
                    if (contextItem instanceof ContextItem) {
                        ContextItem citem = (ContextItem) contextItem;
                        ContextType context = ContextUtils.getContextTypeByName(citem, citem.getDefaultContext(), true);
                        List<ContextParameterType> contextParameterList = context.getContextParameter();
                        String paramName = ContextParameterUtils.getContextString(mappingId);
                        Optional<ContextParameterType> parameterOption = contextParameterList.stream()
                                .filter(parameter -> paramName.equals(parameter.getName())).findFirst();
                        if (parameterOption.isPresent()) {
                            ContextParameterType paramType = parameterOption.get();
                            parameterValue = TalendQuoteUtils.removeQuotesIfExist(paramType.getValue());
                        }
                    }
                    if (StringUtils.isBlank(parameterValue)) {
                        return ExecutionResult.NOTHING_TO_DO;
                    }

                    connection.setDbmsId(parameterValue);
                    NamedThing namedThing = ComponentsUtils.getNameThingFromComponentPropertiesByName(compProperties,
                            "mappingFile");
                    if (namedThing instanceof Property) {
                        Property mappingFile = (Property) namedThing;
                        mappingFile.setValue(parameterValue);
                        mappingFile.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, false);
                    }
                    connection.setCompProperties(compProperties.toSerialized());

                    factory.save(item, true);
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
