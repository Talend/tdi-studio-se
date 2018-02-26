// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.Schema;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * 
 * created by ycbai on 2016年9月22日 Detailled comment
 *
 */
public class NewDelimitedFileWizardMigrationTask extends NewGenericWizardMigrationTask {

    public static final String TYPE_NAME = "fileDelimited"; //$NON-NLS-1$

    public static final String SCHEMA = "main.schema"; //$NON-NLS-1$

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 9, 22, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<>();
        toReturn.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
        return toReturn;
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewDelimitedFileWizardMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace(); // Only for debug.
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    ex.printStackTrace(); // Only for debug.
                }
            }
        }
        return props;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ComponentService service = ComponentsUtils.getComponentService();
        Properties props = getPropertiesFromFile();
        if (item instanceof ConnectionItem) {
            boolean modify = false;
            GenericConnectionItem genericConnectionItem = null;
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection connection = connectionItem.getConnection();
            // Init
            genericConnectionItem = initGenericConnectionItem(connectionItem);
            genericConnectionItem.setTypeName(TYPE_NAME);
            GenericConnection genericConnection = initGenericConnection(connection);
            initProperty(connectionItem, genericConnectionItem);

            ComponentWizard componentWizard = service.getComponentWizard(TYPE_NAME, genericConnectionItem.getProperty().getId());
            ComponentProperties componentProperties = (ComponentProperties) componentWizard.getForms().get(0).getProperties();
            componentProperties.init();

            // Update
            modify = updateComponentProperties(connection, componentProperties, props);
            genericConnection.setCompProperties(componentProperties.toSerialized());
            genericConnectionItem.setConnection(genericConnection);
            updateMetadataTable(connection, genericConnection, componentProperties);
            if (modify) {
                try {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    IRepositoryViewObject object = factory.getLastVersion(item.getProperty().getId(),
                            ERepositoryObjectType.METADATA_FILE_DELIMITED.getFolder(),
                            ERepositoryObjectType.METADATA_FILE_DELIMITED);
                    if (object != null) {
                        factory.deleteObjectPhysical(object);
                    }
                    if (genericConnectionItem != null && connectionItem != null) {
                        factory.create(genericConnectionItem, new Path(connectionItem.getState().getPath()), true);
                    }
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    protected boolean updateComponentProperties(Connection oldConnection, ComponentProperties componentProperties,
            Properties props) {
        boolean updated = super.updateComponentProperties(oldConnection, componentProperties, props);
        boolean rowSeparatorPropertyUpdated = removeQuotesOfStringProperty(componentProperties, "rowSeparator"); //$NON-NLS-1$
        boolean fieldSeparatorPropertyUpdated = removeQuotesOfStringProperty(componentProperties, "fieldSeparator"); //$NON-NLS-1$
        return updated || rowSeparatorPropertyUpdated || fieldSeparatorPropertyUpdated;
    }

    /**
     * Remove quotes from string property value unless it is a space which is invisible.
     * 
     * @param componentProperties
     * @param propertyName
     * @return true if the property is modified, otherwise return false.
     */
    private boolean removeQuotesOfStringProperty(ComponentProperties componentProperties, String propertyName) {
        Property property = componentProperties.getValuedProperty(propertyName);
        if (property != null && GenericTypeUtils.isStringType(property)) {
            String value = (String) property.getValue();
            if (value != null && !" ".equals(value)) { //$NON-NLS-1$
                property.setValue(TalendQuoteUtils.removeQuotesIfExist(value));
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean updateMetadataTable(Connection oldConnection, Connection genericConnection,
            ComponentProperties componentProperties) {
        boolean modified = false;
        if (oldConnection == null || genericConnection == null || componentProperties == null) {
            return modified;
        }
        Set<MetadataTable> tables = ConnectionHelper.getTables(oldConnection);
        Set<MetadataTable> newTables = new HashSet<>(tables);
        for (MetadataTable metaTable : newTables) {
            TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
            serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
            serializedPropsTV.setValue(componentProperties.toSerialized());
            metaTable.getTaggedValue().add(serializedPropsTV);
            TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
            schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
            schemaPropertyTV.setValue(SCHEMA);
            metaTable.getTaggedValue().add(schemaPropertyTV);
            ((orgomg.cwm.objectmodel.core.Package) genericConnection).getOwnedElement().add(metaTable);
            Schema schema = SchemaUtils.convertTalendSchemaIntoComponentSchema(metaTable);
            componentProperties.setValue(SCHEMA, schema);
            modified = true;
        }
        return modified;
    }

}
