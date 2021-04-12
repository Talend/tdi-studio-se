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
package org.talend.designer.core.generic.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.SchemaHelper;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericNodeConnector;
import org.talend.repository.model.IProxyRepositoryFactory;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.objectmodel.core.TaggedValue;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.impl.SchemaImpl;

public class SchemaUtils {

    private static String CATALOG = "TABLE_CAT";

    private static String SCHEMA = "TABLE_SCHEMA";

    private static String TABLE_TYPE = "TABLE_TYPE";

    public static MetadataTable createCatalog(String tableName, String tableType, ComponentProperties properties, Connection connection,
            String schemaPropertyName, String catalogName, String schemaName) {
        if (tableName == null || properties == null || schemaPropertyName == null) {
            return null;
        }

        boolean hasSchemaInCatalog = false;
        boolean exist = false;
        List<Catalog> catalogList = new ArrayList<Catalog>();
        Catalog catalog = (Catalog) ConnectionHelper.getPackage(catalogName, connection, Catalog.class);
        orgomg.cwm.resource.relational.Schema schema = (orgomg.cwm.resource.relational.Schema)
                ConnectionHelper.getPackage(schemaName, connection, orgomg.cwm.resource.relational.Schema.class);
        List<orgomg.cwm.resource.relational.Schema> subschemas = new ArrayList<orgomg.cwm.resource.relational.Schema>();

        if(catalog != null){
            subschemas = CatalogHelper.getSchemas(catalog);
            hasSchemaInCatalog = subschemas.size() > 0;
            exist = true;
        }else{
            catalog = CatalogHelper.createCatalog(catalogName);
            catalogList.add(catalog);
        }

        MetadataTable metadataTable = null;

        if (catalog != null && !hasSchemaInCatalog) { // only catalog
            metadataTable = getExistMetadataTable(catalog, tableName, tableType);
            if(metadataTable == null){
                metadataTable = createTable(tableName, tableType, properties, connection, schemaPropertyName);
                PackageHelper.addMetadataTable(metadataTable, catalog);
            }
        } else if (catalog != null && hasSchemaInCatalog) { // both schema and catalog

            for (orgomg.cwm.resource.relational.Schema current : subschemas) {
                if (current.getName().equals(schemaName)) {
                    schema = current;
                    break;
                }
            }

            if (schema != null) {
                if (schema instanceof SchemaImpl) {
                    SchemaImpl schemaElement = (SchemaImpl) schema;
                    EList<ModelElement> ownedElement = schemaElement.getOwnedElement();
                    metadataTable = getExistMetadataTable(schemaElement, tableName, tableType);
                    if(metadataTable == null){
                        metadataTable = createTable(tableName, tableType, properties, connection, schemaPropertyName);
                        ownedElement.add(metadataTable);
                    }
                }
            } else{
                for (int i = 0; i < subschemas.size(); i++) {
                    SchemaImpl schemaElement = (SchemaImpl) subschemas.get(i);
                    EList<ModelElement> ownedElement = schemaElement.getOwnedElement();
                    metadataTable = getExistMetadataTable(schemaElement, tableName, tableType);
                    if(metadataTable == null){
                        metadataTable = createTable(tableName, tableType, properties, connection, schemaPropertyName);
                        ownedElement.add(metadataTable);
                    }
                }
            }
        }
        if(!exist){
            ConnectionHelper.addCatalogs(catalogList, connection);
        }
        return metadataTable;
    }

    private static MetadataTable getExistMetadataTable(Package pack, String tableName, String tableType){
        ArrayList<MetadataTable> result = new ArrayList<MetadataTable>();
        PackageHelper.getAllTablesWithOrders(pack, result);
        for(MetadataTable table : result){
            if(table.getName().equals(tableName) && table.getTableType().equals(tableType)){
                return table;
            }
        }
        return null;
    }

    public static MetadataTable createTable(String tableName, String tableType, ComponentProperties properties, Connection connection, String schemaPropertyName){
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        MetadataTable metadataTable = null;
        if(tableType.equals(MetadataManager.TYPE_TABLE)){
            metadataTable = RelationalFactory.eINSTANCE.createTdTable();
        }else if(tableType.equals(MetadataManager.TYPE_VIEW)){
            metadataTable = RelationalFactory.eINSTANCE.createTdView();
        }
        metadataTable.setId(factory.getNextId());
        metadataTable.setName(tableName);
        metadataTable.setSourceName(tableName);
        metadataTable.setLabel(tableName);
        metadataTable.setTableType(tableType);

        Object schemaObj = ComponentsUtils.getGenericPropertyValue(properties, schemaPropertyName);
        if (schemaObj instanceof String) {
            Schema avroSchema = new Schema.Parser().parse((String) schemaObj);
            convertComponentSchemaIntoTalendSchema(avroSchema, metadataTable);
        } else if (schemaObj instanceof Schema) {
            convertComponentSchemaIntoTalendSchema((Schema) schemaObj, metadataTable);
        }

        IMetadataTable iMetadataTable = MetadataToolHelper.convert(metadataTable);
        updateComponentSchema(properties, schemaPropertyName, iMetadataTable);
        metadataTable = ConvertionHelper.convert(iMetadataTable, tableType);
        properties.setValue(schemaPropertyName, convertTalendSchemaIntoComponentSchema(metadataTable));
        TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
        serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        serializedPropsTV.setValue(properties.toSerialized());
        metadataTable.getTaggedValue().add(serializedPropsTV);
        TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
        schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
        schemaPropertyTV.setValue(schemaPropertyName);
        metadataTable.getTaggedValue().add(schemaPropertyTV);

        return metadataTable;
    }

    public static MetadataTable createSchema(String tableName, String tableType, ComponentProperties properties, Connection connection,
            String schemaPropertyName, String schemaName) {
        if (tableName == null || properties == null || schemaPropertyName == null) {
            return null;
        }
        boolean exist = false;
        List<orgomg.cwm.resource.relational.Schema> schemaList = new ArrayList<orgomg.cwm.resource.relational.Schema>();
        orgomg.cwm.resource.relational.Schema schema = (orgomg.cwm.resource.relational.Schema)
                ConnectionHelper.getPackage(schemaName, connection, orgomg.cwm.resource.relational.Schema.class);
        if(schema == null){
            schema = SchemaHelper.createSchema(schemaName);
            schemaList.add(schema);
        }else{
            exist = true;
        }

        MetadataTable metadataTable = getExistMetadataTable(schema, tableName, tableType);
        if(metadataTable == null){
            metadataTable = createTable(tableName, tableType, properties, connection, schemaPropertyName);
            PackageHelper.addMetadataTable(metadataTable, schema);
        }
        if(!exist){
            ConnectionHelper.addSchemas(schemaList, connection);
        }
        return metadataTable;
    }

    public static MetadataTable createSchema(String name, ComponentProperties properties, String schemaPropertyName) {
        if (name == null || properties == null || schemaPropertyName == null) {
            return null;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        MetadataTable metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
        metadataTable.setId(factory.getNextId());
        metadataTable.setName(name);
        metadataTable.setLabel(name);
        metadataTable.setSourceName(name);
        Object schemaObj = ComponentsUtils.getGenericPropertyValue(properties, schemaPropertyName);
        if (schemaObj instanceof String) {
            Schema avroSchema = new Schema.Parser().parse((String) schemaObj);
            convertComponentSchemaIntoTalendSchema(avroSchema, metadataTable);
        } else if (schemaObj instanceof Schema) {
            convertComponentSchemaIntoTalendSchema((Schema) schemaObj, metadataTable);
        }
        IMetadataTable iMetadataTable = MetadataToolHelper.convert(metadataTable);
        updateComponentSchema(properties, schemaPropertyName, iMetadataTable);
        metadataTable = ConvertionHelper.convert(iMetadataTable);
        properties.setValue(schemaPropertyName, convertTalendSchemaIntoComponentSchema(metadataTable));
        TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
        serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        serializedPropsTV.setValue(properties.toSerialized());
        metadataTable.getTaggedValue().add(serializedPropsTV);
        TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
        schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
        schemaPropertyTV.setValue(schemaPropertyName);
        metadataTable.getTaggedValue().add(schemaPropertyTV);
        return metadataTable;
    }

    private static void convertComponentSchemaIntoTalendSchema(Schema schema, MetadataTable metadataTable) {
        if (schema == null || metadataTable == null) {
            return;
        }
        for (Schema.Field field : schema.getFields()) {
            MetadataColumn metadataColumn = MetadataToolAvroHelper.convertFromAvro(field, metadataTable);
            metadataTable.getColumns().add(metadataColumn);
        }
    }

    public static Schema convertTalendSchemaIntoComponentSchema(MetadataTable metadataTable) {
        if (metadataTable == null) {
            return null;
        }
        return MetadataToolAvroHelper.convertToAvro(metadataTable);
    }

    /**
     * DOC ycbai Comment method "updateComponentSchema".
     * <p>
     * Recreate a component schema by <code>metadataTable<code> and save it back into the <code>metadataTable<code>.
     *
     * @param metadataTable
     * @param connection
     */
    public static void updateComponentSchema(MetadataTable metadataTable, Connection connection) {
        if (metadataTable == null) {
            return;
        }
        String componentPropertiesStr = null;
        String schemaPropertyName = null;
        TaggedValue componentPropertiesTaggedValue = null;
        EList<TaggedValue> taggedValues = metadataTable.getTaggedValue();
        for (TaggedValue taggedValue : taggedValues) {
            String tag = taggedValue.getTag();
            String tagValue = taggedValue.getValue();
            if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(tag)) {
                componentPropertiesStr = tagValue;
                componentPropertiesTaggedValue = taggedValue;
            } else if (IComponentConstants.COMPONENT_SCHEMA_TAG.equals(tag)) {
                schemaPropertyName = tagValue;
            }
        }
        if (componentPropertiesStr != null && componentPropertiesTaggedValue != null && schemaPropertyName != null) {
            ComponentProperties componentProperties = ComponentsUtils
                    .getComponentPropertiesFromSerialized(componentPropertiesStr, connection);
            componentProperties.setValue(schemaPropertyName, convertTalendSchemaIntoComponentSchema(metadataTable));
            componentPropertiesTaggedValue.setValue(componentProperties.toSerialized());
        }
    }

    public static void updateComponentSchema(ComponentProperties componentProperties, String schemaPropertyName,
            IMetadataTable metadataTable) {
        updateComponentSchema(componentProperties, schemaPropertyName, metadataTable, null);
    }

    public static void updateComponentSchema(ComponentProperties componentProperties, String schemaPropertyName,
            IMetadataTable metadataTable, IElementParameter param) {
        if (componentProperties == null || schemaPropertyName == null || metadataTable == null) {
            return;
        }
        Schema schema = convertTalendSchemaIntoComponentSchema(ConvertionHelper.convert(metadataTable));
        componentProperties.setValue(schemaPropertyName, schema);
        if (param != null) {
            param.setValue(schema.toString());
        }
    }

    public static void updateComponentSchema(INode node, IMetadataTable metadataTable, Boolean askPropagate) {
        if (node == null || metadataTable == null || node.getComponentProperties() == null) {
            return;
        }
        Schema schema = convertTalendSchemaIntoComponentSchema(ConvertionHelper.convert(metadataTable));
        INodeConnector connector = node.getConnectorFromName(metadataTable.getAttachedConnector());
        if (connector != null) {
            if (connector instanceof GenericNodeConnector) {
                node.getComponentProperties().setConnectedSchema(((GenericNodeConnector) connector).getComponentConnector(),
                        schema, true);
            }
            for (IElementParameter param : new ArrayList<IElementParameter>(node.getElementParameters())) {
                if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())
                        && connector.getName().equals(param.getContext())) {
                    if (param instanceof GenericElementParameter) {
                        ((GenericElementParameter)param).setAskPropagate(askPropagate);
                    }
                    param.setValue(schema);
                }
            }
        }
    }

    public static ComponentProperties getCurrentComponentProperties(IMetadataTable table) {
        if (table != null) {
            String serializedProperties = null;
            SerializerDeserializer.Deserialized<ComponentProperties> fromSerializedProperties = null;
            if (table instanceof MetadataTableRepositoryObject) {
                MetadataTableRepositoryObject metaTableRepObj = (MetadataTableRepositoryObject) table;
                MetadataTable metadataTable = metaTableRepObj.getTable();
                if (metadataTable != null && metadataTable.getTaggedValue() != null) {
                    for (TaggedValue serializedProps : metadataTable.getTaggedValue()) {
                        if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(serializedProps.getTag())) {
                            serializedProperties = serializedProps.getValue();
                            break;
                        }
                    }
                }
                if (serializedProperties != null) {
                    Connection connection = ((ConnectionItem)metaTableRepObj.getViewObject().getProperty().getItem()).getConnection();
                    return ComponentsUtils.getComponentPropertiesFromSerialized(serializedProperties, connection);
                }
            } else if (table instanceof org.talend.core.model.metadata.MetadataTable) {
                org.talend.core.model.metadata.MetadataTable metaTable = (org.talend.core.model.metadata.MetadataTable) table;
                Map<String, String> additionalProperties = metaTable.getAdditionalProperties();
                serializedProperties = additionalProperties.get(IComponentConstants.COMPONENT_PROPERTIES_TAG);
                if (serializedProperties != null) {
                    fromSerializedProperties = Properties.Helper.fromSerializedPersistent(serializedProperties, ComponentProperties.class, null);
                }
            }
            if (fromSerializedProperties != null) {
                return fromSerializedProperties.object;
            }
        }
        return null;
    }

    public static List<MetadataTable> getMetadataTables(orgomg.cwm.objectmodel.core.Package parentPackage,
            Class containerTypeClass) {
        List<MetadataTable> metadataTables = new ArrayList<>();
        if(parentPackage instanceof DatabaseConnection){
            metadataTables.addAll(ConnectionHelper.getTables((DatabaseConnection)parentPackage));
            return metadataTables;
        }
        metadataTables.addAll(PackageHelper.getOwnedElements(parentPackage, MetadataTable.class));
        if (containerTypeClass != null) {
            List subContainers = PackageHelper.getOwnedElements(parentPackage, containerTypeClass);
            for (Object subContainer : subContainers) {
                if (subContainer instanceof orgomg.cwm.objectmodel.core.Package) {
                    orgomg.cwm.objectmodel.core.Package subContainerPackage = (orgomg.cwm.objectmodel.core.Package) subContainer;
                    metadataTables.addAll(getMetadataTables(subContainerPackage, containerTypeClass));
                }
            }
        }
        return metadataTables;
    }

    public static MetadataTable getMetadataTable(Connection connection, String tabLabel, Class containerTypeClass) {
        List<MetadataTable> tables = getMetadataTables(connection, containerTypeClass);
        for (MetadataTable table : tables) {
            if (tabLabel != null && tabLabel.equals(table.getLabel())) {
                return table;
            }
        }
        return null;
    }

    public static MetadataTable getMetadataTable(Connection connection, MetadataTable mtable, IRepositoryViewObject object) {
        List<MetadataTable> tables = new ArrayList<MetadataTable>();
        ERepositoryObjectType type = object.getRepositoryObjectType();
        if(object instanceof MetadataTableRepositoryObject){
            IRepositoryViewObject viewObject = ((MetadataTableRepositoryObject)object).getViewObject();
            if(viewObject != null){
                type = viewObject.getRepositoryObjectType();
            }
        }
        if(isExtraDBType(type)){
            tables.addAll(ConnectionHelper.getTables(connection));
        }else{
            tables.addAll(getMetadataTables(connection, mtable.eContainer().getClass()));
        }
        for (MetadataTable table : tables) {
            if (mtable.getLabel() != null && mtable.getLabel().equals(table.getLabel())) {
                return table;
            }
        }
        return null;
    }

    public static boolean isExtraDBType(ERepositoryObjectType type){
        if(type == null){
            return false;
        }
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                    IGenericDBService.class);
        }
        if(dbService != null){
            return dbService.getExtraTypes().contains(type);
        }
        return false;
    }

    public static void createCatalog(String name, ComponentProperties properties, Connection connection, String schemaPropertyName){
        Schema avroSchema = null;
        Object schemaObj = ComponentsUtils.getGenericPropertyValue(properties, schemaPropertyName);
        if (schemaObj instanceof String) {
            avroSchema = new Schema.Parser().parse((String) schemaObj);
        } else if (schemaObj instanceof Schema) {
            avroSchema = (Schema) schemaObj;
        }
        String catalogName = avroSchema.getProp(CATALOG);
        String schemaName = avroSchema.getProp(SCHEMA);
        String tableType = avroSchema.getProp(TABLE_TYPE);

        if(catalogName != null){
            createCatalog(name, tableType, properties, connection,
                    schemaPropertyName, catalogName, schemaName);
        }else if(schemaName != null){
            createSchema(name, tableType, properties, connection,
                    schemaPropertyName, schemaName);
        }
    }

}
