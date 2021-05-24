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

import static org.junit.Assert.*;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.objectmodel.core.TaggedValue;
import orgomg.cwm.objectmodel.core.impl.PackageImpl;

/**
 * created by ycbai on 2016年3月3日 Detailled comment
 *
 */
public class SchemaUtilsTest {

    @Test
    public void testGetMetadataTables() {
        // Create some MetadataTables for test.
        MetadataTable table1 = createMetadataTable("table1"); //$NON-NLS-1$
        MetadataTable table2 = createMetadataTable("table2"); //$NON-NLS-1$
        MetadataTable table3 = createMetadataTable("table3"); //$NON-NLS-1$
        MetadataTable table4 = createMetadataTable("table4"); //$NON-NLS-1$

        // ///////////////////////////////////
        // Overall structure:
        //
        // . parentPackage
        // .. table1
        // .. container1
        // ... table2
        // ... container1_1
        // .... table4
        // .. container2
        // ... table3
        //
        // ///////////////////////////////////
        Package parentPackage = CoreFactory.eINSTANCE.createPackage();
        parentPackage.getOwnedElement().add(table1);

        Package container1 = new TestContainer();
        container1.getOwnedElement().add(table2);
        Package container1_1 = new TestContainer();
        container1_1.getOwnedElement().add(table4);
        container1.getOwnedElement().add(container1_1);
        parentPackage.getOwnedElement().add(container1);

        Package container2 = new OtherTestContainer();
        container2.getOwnedElement().add(table3);
        parentPackage.getOwnedElement().add(container2);

        // When there is not any sub-container specified, will return all direct MetadataTables of the parent package.
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(parentPackage, null);
        assertNotNull(getTheTable(metadataTables, table1.getName()));

        // When there is one sub-container specified, will return all direct MetadataTables of the parent package and
        // sub-container recursively.
        metadataTables = SchemaUtils.getMetadataTables(parentPackage, TestContainer.class);
        assertNotNull(getTheTable(metadataTables, table1.getName()));
        assertNotNull(getTheTable(metadataTables, table2.getName()));
        assertNotNull(getTheTable(metadataTables, table4.getName()));
        assertNull(getTheTable(metadataTables, table3.getName()));
    }

    private MetadataTable getTheTable(List<MetadataTable> tableList, String tableName) {
        for (MetadataTable metadataTable : tableList) {
            if (tableName != null && tableName.equals(metadataTable.getName())) {
                return metadataTable;
            }
        }
        return null;
    }

    private MetadataTable createMetadataTable(String name) {
        MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
        table.setName(name);
        table.setLabel(table.getName());
        return table;
    }

    @Test
    public void testUpdateComponentSchema() {
        String TEST_TABLE_NAME = "testTable"; //$NON-NLS-1$
        String TEST_COL_NAME = "userId"; //$NON-NLS-1$
        String ADDED_COL_NAME = "added"; //$NON-NLS-1$
        String SCHEMA_PROP_NAME = "schema.schema"; //$NON-NLS-1$

        // Create the test MetadataTable.
        MetadataTable table = createMetadataTable(TEST_TABLE_NAME);
        MetadataColumn testColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        testColumn.setName(TEST_COL_NAME);
        testColumn.setLabel(testColumn.getName());
        testColumn.setDefaultValue("1"); //$NON-NLS-1$
        testColumn.setTalendType(JavaTypesManager.STRING.getId());
        table.getColumns().add(testColumn);

        // Create one component properties which has one schema property.
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Schema oldSchema = SchemaBuilder.record(TEST_TABLE_NAME).fields().name(TEST_COL_NAME).type().stringType().noDefault()
                .endRecord();
        props.schema.schema.setValue(oldSchema);

        // Set the component properties and schema property name into MetadataTable.
        TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
        serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        serializedPropsTV.setValue(props.toSerialized());
        table.getTaggedValue().add(serializedPropsTV);
        TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
        schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
        schemaPropertyTV.setValue(SCHEMA_PROP_NAME);
        table.getTaggedValue().add(schemaPropertyTV);

        // Add another MetadataColumn into MetadataTable.
        MetadataColumn addedColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        addedColumn.setName(ADDED_COL_NAME);
        addedColumn.setLabel(addedColumn.getName());
        addedColumn.setDefaultValue("x"); //$NON-NLS-1$
        addedColumn.setTalendType(JavaTypesManager.STRING.getId());
        table.getColumns().add(addedColumn);

        // Invoke updateComponentSchema() method.
        SchemaUtils.updateComponentSchema(table, null);

        // Check if the schema object is updated correctly.
        String componentPropertiesStr = null;
        String schemaPropertyName = null;
        EList<TaggedValue> taggedValues = table.getTaggedValue();
        for (TaggedValue taggedValue : taggedValues) {
            String tag = taggedValue.getTag();
            String tagValue = taggedValue.getValue();
            if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(tag)) {
                componentPropertiesStr = tagValue;
            } else if (IComponentConstants.COMPONENT_SCHEMA_TAG.equals(tag)) {
                schemaPropertyName = tagValue;
            }
        }

        ComponentProperties componentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(componentPropertiesStr, null);
        Object schemaValue = componentProperties.getValuedProperty(schemaPropertyName).getValue();
        Schema avroSchema = getAvroSchema(schemaValue);
        props.schema.schema.setValue(avroSchema);
        assertNotNull(avroSchema.getField(TEST_COL_NAME));
        assertNotNull(avroSchema.getField(ADDED_COL_NAME));
        assertEquals(2, avroSchema.getFields().size());

        // Test method updateComponentSchema(ComponentProperties componentProperties, String schemaPropertyName,
        // IMetadataTable metadataTable)
        IMetadataTable iMetadataTable = MetadataToolHelper.convert(table);
        iMetadataTable.getListColumns().remove(1);
        SchemaUtils.updateComponentSchema(props, SCHEMA_PROP_NAME, iMetadataTable);
        schemaValue = props.getValuedProperty(schemaPropertyName).getValue();
        avroSchema = getAvroSchema(schemaValue);
        assertNotNull(avroSchema.getField(TEST_COL_NAME));
        assertNull(avroSchema.getField(ADDED_COL_NAME));
        assertEquals(1, avroSchema.getFields().size());
    }

    private Schema getAvroSchema(Object schemaValue) {
        Schema avroSchema = null;
        if (schemaValue instanceof Schema) {
            avroSchema = (Schema) schemaValue;
        } else if (schemaValue instanceof String) {
            avroSchema = new Schema.Parser().parse((String) schemaValue);
        }
        return avroSchema;
    }

    /**
     * A container class only for test.
     */
    class TestContainer extends PackageImpl {
        // only for test.
    }

    /**
     * A container class only for test.
     */
    class OtherTestContainer extends PackageImpl {
        // only for test.
    }

}
