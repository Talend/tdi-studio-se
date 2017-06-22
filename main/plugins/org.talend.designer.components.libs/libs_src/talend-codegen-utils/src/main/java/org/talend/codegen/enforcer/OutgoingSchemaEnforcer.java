// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.di.converter.DiConverters;

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
 * also a field for dynamic field. {@link OutgoingSchemaEnforcer} goal is to convert avro-styled data to Talend-styled.
 * {@link OutgoingSchemaEnforcer#get(int)} is the main functionality of this class. This class is used in codegen plugin.
 * See, component_util_indexedrecord_to_rowstruct.javajet. Note, get() is called for each field in Row2Struct. When user specified
 * dynamic column,
 * Row2Struct will contain one more field than desigh avro schema.
 */
public class OutgoingSchemaEnforcer {

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
     * Avro to DI converters list. Converter index corresponds to specific field in {@link Schema}, which this converter will
     * convert
     */
    @SuppressWarnings("rawtypes")
    private List<AvroConverter> converters;

    /**
     * Constructor sets {@link IndexMapper} instance
     *
     * @param indexMapper tool, which computes correspondence between design and runtime fields
     */
    public OutgoingSchemaEnforcer(IndexMapper indexMapper) {
        this.indexMapper = indexMapper;
    }

    /**
     * Wraps {@link IndexedRecord},
     * creates map of correspondence between design and runtime fields, when first record is wrapped
     * It may be useful when runtime schema has different field order from design schema (and "by Name" index mapping is used)
     * Also initializes converters for each field
     *
     * @param record {@link IndexedRecord} to be wrapped
     */
    public void setWrapped(IndexedRecord record) {
        wrappedRecord = record;
        if (!firstRecordProcessed) {
            processFirstRecord(record);
            firstRecordProcessed = true;
        }
    }

    /**
     * Computes {@link this#indexMap} and initializes {@link this#converters}
     * This method could be extended
     * 
     * @param record first incoming {@link IndexedRecord}
     */
    protected void processFirstRecord(IndexedRecord record) {
        indexMap = indexMapper.computeIndexMap(record.getSchema());
        converters = DiConverters.initConverters(record.getSchema());
    }

    /**
     * Could be called only after first record was wrapped.
     * Here design schema and runtime schema have the same fields
     * (but fields could be in different order)
     * 
     * <code>pojoIndex</code> is a field index in design schema. This field may have different index in runtime schema
     * (when runtime {@link Schema} has different field order).
     * {@link this#indexMap} is used to compute corresponding field index in runtime schema
     *
     * @param pojoIndex index of required value. Could be from 0 to designSchemaSize - 1
     */
    public Object get(int pojoIndex) {
        Object value = wrappedRecord.get(indexMap[pojoIndex]);
        return convertValue(value, indexMap[pojoIndex]);
    }

    /**
     * Converts value from Avro to DI format
     * 
     * @param avroValue value retrieved from {@link IndexedRecord}
     * @param recordIndex value index in {@link IndexedRecord}
     * @return DI value to be set to DI row
     */
    @SuppressWarnings("unchecked")
    protected Object convertValue(Object avroValue, int recordIndex) {
        Object diValue = null;
        if (avroValue != null) {
            diValue = converters.get(recordIndex).convertToDatum(avroValue);
        }
        return diValue;
    }

}