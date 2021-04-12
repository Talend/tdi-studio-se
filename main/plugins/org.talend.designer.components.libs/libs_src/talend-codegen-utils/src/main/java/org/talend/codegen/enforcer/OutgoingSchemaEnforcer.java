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
package org.talend.codegen.enforcer;

import static org.talend.codegen.DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE;
import static org.talend.codegen.DiSchemaConstants.LOGICAL_TIME_TYPE_AS;
import static org.talend.codegen.DiSchemaConstants.AS_TALEND_DATE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter.UnmodifiableAdapterException;

/**
 * This class acts as a wrapper around an arbitrary Avro {@link IndexedRecord} to coerce the output type to the exact
 * Java objects expected by the Talend 6 Studio (which will copy the fields into a POJO in generated code).
 * <p>
 * A wrapper like this should be attached to an input component, for example, to ensure that its outgoing data meets the
 * Schema constraints imposed by the Studio, including:
 * <ul>
 * <li>Coercing the types of the returned objects to *exactly* the type required by the Talend POJO.</li>
 * <li>Placing all of the unresolved columns between the wrapped schema and the output schema in the Dynamic column.</li>
 * </ul>
 * <p>
 * One instance of this object can be created per outgoing schema and reused via the {@link #setWrapped(IndexedRecord)}
 * method.
 * <p>
 * This class accepts so-called design schema as an argument for constructor. Design schema is specified by user in Schema Editor.
 * It contains data fields, which user wants to retrieve from data source. Schema Editor creates schema in old manner. It creates
 * instance
 * of MetadataTable. For new components this instance is converted to avro {@link Schema} instance by MetadataToolAvroHelper.
 * <p>
 * There could be a situation when user doesn't know all fields of data, but he wants to retrieve them all. In this case user
 * should specify
 * some field as dynamic. Dynamic means it is not known at design time how much actual fields will be retrieved. Dynamic field
 * aggregates
 * all unknown fields. Note, design avro {@link Schema} doesn't contain dynamic field. It contain special properties, which
 * describe
 * dynamic field (its name, position in schema etc).
 * <p>
 * Consider following example:
 * User specified schema with following fields (field name, type):
 * <ul>
 * <li>(name, String)</li>
 * <li>(dynamic, Dynamic)</li>
 * <li>(address, String)</li>
 * </ul>
 * After conversion with MetadataToolAvroHelper avro {@link Schema} will contain only:
 * <ul>
 * <li>(name, String)</li>
 * <li>(address, String)</li>
 * </ul>
 * and properties, which describes dynamic field
 * <p>
 * There is one more thing, which should be mentioned. Both old and TCOMP components could be used in one Job in Di (Studio). To
 * make them
 * compatible there is special handling in codegen plugin. TCOMP component output (IndexedRecord and its Schema) is converted to
 * old Di objects.
 * Row2Struct (also known as POJO) corresponds to IndexedRecord. Its fields correspond to data fields. Important note is that
 * Row2Struct contains
 * also a field for dynamic field. {@link DiOutgoingSchemaEnforcer} goal is to convert avro-styled data to Talend-styled.
 * {@link DiOutgoingSchemaEnforcer#get(int)} is the main functionality of this class. This class is used in codegen plugin.
 * See, component_util_indexedrecord_to_rowstruct.javajet. Note, get() is called for each field in Row2Struct. When user specified
 * dynamic column,
 * Row2Struct will contain one more field than desigh avro schema.
 */
public class OutgoingSchemaEnforcer implements IndexedRecord {

    /**
     * {@link Schema} which was specified by user during setting component properties (at design time)
     * This schema may contain di-specific properties
     */
    protected final Schema designSchema;

    /**
     * A {@link List} of design schema {@link Field}s
     * It is stored as separate field to accelerate access to them
     */
    protected final List<Field> designFields;

    /**
     * Number of fields in design schema
     */
    protected final int designSchemaSize;

    /**
     * {@link IndexedRecord} currently wrapped by this enforcer. This can be swapped out for new data as long as
     * they keep the same schema. This {@link IndexedRecord} contains another {@link Schema} which is called actual or runtime
     * schema.
     */
    protected IndexedRecord wrappedRecord;

    /**
     * Tool, which computes correspondence between design and runtime fields
     */
    protected final IndexMapper indexMapper;

    /**
     * Maps design field indexes to runtime field indexes.
     * Design indexes are indexed of this array and runtime indexed are values
     * This map is computed once for the first incoming record and then used for all subsequent records
     * -1 value is used to to denote that design field corresponds to dynamic field
     */
    protected int[] indexMap;

    /**
     * State field, which denotes whether first incoming {@link IndexedRecord} was processed
     * (i.e. <code>indexMap</code> was created)
     */
    private boolean firstRecordProcessed = false;

    /**
     * Constructor sets design schema and {@link IndexMapper} instance
     *
     * @param designSchema design schema specified by user
     * @param indexMapper  tool, which computes correspondence between design and runtime fields
     */
    public OutgoingSchemaEnforcer(Schema designSchema, IndexMapper indexMapper) {
        this.designSchema = designSchema;
        this.designFields = designSchema.getFields();
        this.designSchemaSize = designFields.size();
        this.indexMapper = indexMapper;
    }

    /**
     * Wraps {@link IndexedRecord},
     * creates map of correspondence between design and runtime fields, when first record is wrapped
     *
     * @param record {@link IndexedRecord} to be wrapped
     */
    public void setWrapped(IndexedRecord record) {
        wrappedRecord = record;
        if (!firstRecordProcessed) {
            indexMap = indexMapper.computeIndexMap(record.getSchema());
            firstRecordProcessed = true;
        }
    }

    /**
     * Returns schema of this {@link IndexedRecord}
     * Note, this schema doesn't contain dynamic field.
     * However, {@link DiOutgoingDynamicSchemaEnforcer} returns dynamic values
     * in map, when dynamic field index is passed
     */
    @Override
    public Schema getSchema() {
        return designSchema;
    }

    /**
     * Throws {@link UnmodifiableAdapterException}. This operation is not supported
     */
    @Override
    public void put(int i, Object v) {
        throw new UnmodifiableAdapterException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Could be called only after first record was wrapped.
     * Here design schema and runtime schema have the same fields
     * (but fields could be in different order)
     *
     * @param pojoIndex index of required value. Could be from 0 to designSchemaSize - 1
     */
    @Override
    public Object get(int pojoIndex) {
        Field outField = designFields.get(pojoIndex);
        Object value = wrappedRecord.get(indexMap[pojoIndex]);
        return transformValue(value, outField);
    }

    /**
     * Transforms record column value from Avro type to Talend type
     *
     * @param value      record column value, which should be transformed into Talend compatible value.
     *                   It can be null when null
     *                   corresponding wrapped field.
     * @param valueField field, which contain information about value's Talend type. It mustn't be null
     */
    protected Object transformValue(Object value, Field valueField) {
        if (null == value) {
            return null;
        }

        Schema nonnull = AvroUtils.unwrapIfNullable(valueField.schema());
        LogicalType logicalType = nonnull.getLogicalType();
        if (logicalType != null) {
            if (logicalType == LogicalTypes.date()) {
                // (dchmyga): we need to set date as number of days at the end of the day in current timezone
                LocalDate ld = LocalDate.ofEpochDay(((Integer) value).longValue());
                ZonedDateTime zonedDate = ld.atStartOfDay(ZoneId.systemDefault());
                return Date.from(zonedDate.toInstant());
            } else if (logicalType == LogicalTypes.timeMillis()) {
                if (AS_TALEND_DATE.equals(valueField.getProp(LOGICAL_TIME_TYPE_AS))) {
                    return new Date((Integer) value);
                } else {
                    return value;
                }
            } else if (logicalType == LogicalTypes.timestampMillis()) {
                //TODO use java.sql.Timestamp for precision to nano second? not sure as logicalType is not only for database maybe
                return new Date((Long) value);
            }
        }

        // This might not always have been specified.
        String talendType = valueField.getProp(TALEND6_COLUMN_TALEND_TYPE);
        String javaClass = nonnull.getProp(SchemaConstants.JAVA_CLASS_FLAG);

        // TODO(rskraba): A full list of type conversion to coerce to Talend-compatible types.
        if ("id_Short".equals(talendType)) { //$NON-NLS-1$
            return value instanceof Number ? ((Number) value).shortValue() : Short.parseShort(String.valueOf(value));
        } else if ("id_Date".equals(talendType) || "java.util.Date".equals(javaClass)) { //$NON-NLS-1$
            // FIXME - remove this mapping in favor of using Avro logical types
            return value instanceof Date ? value : new Date((Long) value);
        } else if ("id_Byte".equals(talendType)) { //$NON-NLS-1$
            return value instanceof Number ? ((Number) value).byteValue() : Byte.parseByte(String.valueOf(value));
        } else if ("id_Character".equals(talendType) || "java.lang.Character".equals(javaClass)) {
            return value instanceof Character ? value : ((String) value).charAt(0);
        } else if ("id_BigDecimal".equals(talendType) || "java.math.BigDecimal".equals(javaClass)) {
            return value instanceof BigDecimal ? value : new BigDecimal(String.valueOf(value));
        }
        return value;
    }

}