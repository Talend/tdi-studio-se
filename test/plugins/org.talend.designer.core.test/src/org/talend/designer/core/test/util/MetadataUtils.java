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
package org.talend.designer.core.test.util;

import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.designer.core.utils.SchemaProperties;
import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by ycbai on 2016年4月18日 Detailled comment
 *
 */
public class MetadataUtils {

    /**
     * Create a MetadataTable instance with ComponentProperties
     *
     * @param tableName is the table name
     * @param columnList is a map which key is column attribute key from {@link IMetadataTableConstants} and value is
     * attribute value
     * @param schemaProperties is the ComponentProperties which contained component schema
     * @return
     */
    public static MetadataTable createTable(String tableName, List<Map<String, Object>> columnList,
            SchemaProperties schemaProperties) {
        MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
        table.setName(tableName);
        table.setLabel(table.getName());

        for (Map<String, Object> columnAttrMap : columnList) {
            MetadataColumn column = createColumn(columnAttrMap);
            if (column != null) {
                table.getColumns().add(column);
            }
        }

        Schema avroSchema = MetadataToolAvroHelper.convertToAvro(table);
        schemaProperties.schema.setValue(avroSchema);

        // Set the component properties and schema property name into MetadataTable.
        TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
        serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        serializedPropsTV.setValue(schemaProperties.toSerialized());
        table.getTaggedValue().add(serializedPropsTV);
        TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
        schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
        schemaPropertyTV.setValue(schemaProperties.schema.getName());
        table.getTaggedValue().add(schemaPropertyTV);

        return table;
    }

    /**
     * Create a column instance by <code>columnAttrMap</code>.
     * <p>
     * FIXME: will add more attributes support if needed.
     *
     * @param columnAttrMap
     * @return
     */
    private static MetadataColumn createColumn(Map<String, Object> columnAttrMap) {
        MetadataColumn column = ConnectionFactory.eINSTANCE.createMetadataColumn();
        Object columnNameObj = columnAttrMap.get(IMetadataTableConstants.COLUMN_NAME);
        if (columnNameObj == null) {
            return null; // Column name must not be null.
        }
        column.setName(String.valueOf(columnNameObj));
        column.setLabel(column.getName());
        String columnType = "id_String"; //$NON-NLS-1$
        Object columnTypeObj = columnAttrMap.get(IMetadataTableConstants.COLUMN_TYPE);
        if (columnTypeObj != null) {
            columnType = String.valueOf(columnTypeObj);
        }
        column.setTalendType(columnType);

        return column;
    }

}
