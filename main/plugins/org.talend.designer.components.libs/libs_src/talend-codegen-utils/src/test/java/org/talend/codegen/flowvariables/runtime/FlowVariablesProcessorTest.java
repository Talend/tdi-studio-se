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
package org.talend.codegen.flowvariables.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.RootSchemaUtils;

/**
 * Unit-tests for {@link FlowVariablesProcessor} class
 */
public class FlowVariablesProcessorTest {

    private RuntimeContainer runtimeContainer = new DefaultComponentRuntimeContainerImpl() {

        @Override
        public String getCurrentComponentId() {
            return "tComponent_1";
        }
    };

    /**
     * Checks {@link FlowVariablesProcessor#processData(Object)} throws {@link NullPointerException} if
     * {@link FlowVariablesProcessor#initSchema()}
     * wasn't called before
     */
    @Test(expected = NullPointerException.class)
    public void testProcessDataSchemaNotInitialized() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields().name("id").type().intType().noDefault().endRecord();

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord record = new GenericData.Record(rootSchema);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.processData(record);
    }

    /**
     * Checks {@link FlowVariablesProcessor#processData(Object)} returns instance of Main {@link IndexedRecord} unwrapped from
     * Root record
     * and {@link RuntimeContainer} contains flow variable value after this method call
     */
    @Test
    public void testProcessData() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord mainRecord = new GenericData.Record(mainSchema);
        mainRecord.put(0, "Abraham Lincoln");
        mainRecord.put(1, 208);

        IndexedRecord outOfBandRecord = new GenericData.Record(outOfBandSchema);
        outOfBandRecord.put(0, 123);

        IndexedRecord rootRecord = new GenericData.Record(rootSchema);
        rootRecord.put(0, mainRecord);
        rootRecord.put(1, outOfBandRecord);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.initSchema(rootRecord);
        Object actualData = flowVariablesProcessor.processData(rootRecord);

        assertThat(actualData, instanceOf(IndexedRecord.class));

        IndexedRecord actualMainRecord = (IndexedRecord) actualData;
        assertEquals(mainRecord, actualMainRecord);

        Object flowVariable = runtimeContainer.getComponentData("tComponent_1", "id");
        assertNotNull(flowVariable);
        assertThat(flowVariable, instanceOf(Integer.class));

        assertEquals(123, flowVariable);
    }

    /**
     * Checks {@link FlowVariablesProcessor#processData(Object)} returns instance of Main data even unwrapped from Root record
     * if it's not {@link IndexedRecord}
     */
    @Test
    public void testProcessDataMainNotRecord() {
        Schema mainSchema = SchemaBuilder.builder().stringType();

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        String mainData = "mainData";

        IndexedRecord outOfBandRecord = new GenericData.Record(outOfBandSchema);
        outOfBandRecord.put(0, 123);

        IndexedRecord rootRecord = new GenericData.Record(rootSchema);
        rootRecord.put(0, mainData);
        rootRecord.put(1, outOfBandRecord);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.initSchema(rootRecord);
        Object actualData = flowVariablesProcessor.processData(rootRecord);

        assertThat(actualData, instanceOf(String.class));

        assertEquals("mainData", actualData);
    }

    /**
     * Checks {@link FlowVariablesProcessor#processDataIterable(Iterable)} throws {@link NullPointerException}
     * if {@link FlowVariablesProcessor#initSchema()} wasn't called before
     */
    @Test(expected = NullPointerException.class)
    public void testProcessDataIterableSchemaNotInitialized() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields().name("id").type().intType().noDefault().endRecord();

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord record = new GenericData.Record(rootSchema);
        ArrayList<Object> records = new ArrayList<>();
        records.add(record);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.processDataIterable(records);
    }

    /**
     * Checks {@link FlowVariablesProcessor#processDataIterable(Iterable)} returns instance of {@link Iterable} with the only one
     * Main {@link IndexedRecord} unwrapped from Root record
     * and {@link RuntimeContainer} contains flow variable value after this method call
     */
    @Test
    public void testProcessDataIterable() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord mainRecord = new GenericData.Record(mainSchema);
        mainRecord.put(0, "Abraham Lincoln");
        mainRecord.put(1, 208);

        IndexedRecord outOfBandRecord = new GenericData.Record(outOfBandSchema);
        outOfBandRecord.put(0, 123);

        IndexedRecord rootRecord = new GenericData.Record(rootSchema);
        rootRecord.put(0, mainRecord);
        rootRecord.put(1, outOfBandRecord);

        ArrayList<Object> records = new ArrayList<>();
        records.add(rootRecord);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.initSchema(rootRecord);
        Iterable<Object> actualDataIterable = flowVariablesProcessor.processDataIterable(records);
        Iterator<Object> actualDataIterator = actualDataIterable.iterator();

        assertTrue(actualDataIterator.hasNext());
        Object actualData = actualDataIterator.next();
        assertFalse(actualDataIterator.hasNext());

        assertThat(actualData, instanceOf(IndexedRecord.class));
        IndexedRecord actualMainRecord = (IndexedRecord) actualData;
        assertEquals(mainRecord, actualMainRecord);

        Object flowVariable = runtimeContainer.getComponentData("tComponent_1", "id");
        assertNotNull(flowVariable);
        assertThat(flowVariable, instanceOf(Integer.class));

        assertEquals(123, flowVariable);
    }

    /**
     * Checks {@link FlowVariablesProcessor#processDataIterable(Iterable)} returns instance of {@link Iterable} with the same
     * number
     * of data as input {@link Iterable} has. All elements in output {@link Iterable} should be instances of Main data.
     * {@link RuntimeContainer} contains value of first flow variable value after this method call
     */
    @Test
    public void testProcessDataIterableSeveralData() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord mainRecord1 = new GenericData.Record(mainSchema);
        mainRecord1.put(0, "Abraham Lincoln");
        mainRecord1.put(1, 208);

        IndexedRecord outOfBandRecord1 = new GenericData.Record(outOfBandSchema);
        outOfBandRecord1.put(0, 123);

        IndexedRecord mainRecord2 = new GenericData.Record(mainSchema);
        mainRecord2.put(0, "George Washington");
        mainRecord2.put(1, 284);

        IndexedRecord outOfBandRecord2 = new GenericData.Record(outOfBandSchema);
        outOfBandRecord2.put(0, 321);

        IndexedRecord rootRecord1 = new GenericData.Record(rootSchema);
        rootRecord1.put(0, mainRecord1);
        rootRecord1.put(1, outOfBandRecord1);

        IndexedRecord rootRecord2 = new GenericData.Record(rootSchema);
        rootRecord2.put(0, mainRecord2);
        rootRecord2.put(1, outOfBandRecord2);

        ArrayList<Object> records = new ArrayList<>();
        records.add(rootRecord1);
        records.add(rootRecord2);

        FlowVariablesProcessor flowVariablesProcessor = new FlowVariablesProcessor(runtimeContainer);
        flowVariablesProcessor.initSchema(rootRecord1);
        Iterable<Object> actualDataIterable = flowVariablesProcessor.processDataIterable(records);
        Iterator<Object> actualDataIterator = actualDataIterable.iterator();

        assertTrue(actualDataIterator.hasNext());
        Object actualData1 = actualDataIterator.next();
        assertEquals(mainRecord1, actualData1);

        assertTrue(actualDataIterator.hasNext());
        Object actualData2 = actualDataIterator.next();
        assertEquals(mainRecord2, actualData2);

        assertFalse(actualDataIterator.hasNext());

        Object flowVariable = runtimeContainer.getComponentData("tComponent_1", "id");
        assertNotNull(flowVariable);
        assertThat(flowVariable, instanceOf(Integer.class));

        assertEquals(123, flowVariable);
    }
}
