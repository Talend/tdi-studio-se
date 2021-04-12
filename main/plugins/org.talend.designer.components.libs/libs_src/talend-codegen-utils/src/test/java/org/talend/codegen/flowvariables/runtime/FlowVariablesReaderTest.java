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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.RootSchemaUtils;

/**
 * Unit-tests for {@link FlowVariablesReader} class
 */
public class FlowVariablesReaderTest {

    private Reader<?> wrappedReader = mock(Reader.class);

    private RuntimeContainer runtimeContainer = new DefaultComponentRuntimeContainerImpl() {

        @Override
        public String getCurrentComponentId() {
            return "tComponent_1";
        }
    };

    /**
     * Checks {@link FlowVariablesReader#start()} calls {@link Reader#start()} method of underlying {@link Reader}
     */
    @Test
    public void testStart() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.start();

        verify(wrappedReader).start();
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#advance())} calls {@link Reader#advance()} method of underlying {@link Reader}
     */
    @Test
    public void testAdvance() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.advance();

        verify(wrappedReader).advance();
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#getCurrentSource()} calls {@link Reader#getCurrentSource()} method of underlying
     * {@link Reader}
     */
    @Test
    public void testGetCurrentSource() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.getCurrentSource();

        verify(wrappedReader).getCurrentSource();
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#getCurrentTimestamp()} calls {@link Reader#getCurrentTimestamp()} method of underlying
     * {@link Reader}
     */
    @Test
    public void testGetCurrentTimestamp() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.getCurrentTimestamp();

        verify(wrappedReader).getCurrentTimestamp();
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#getReturnValues()} calls {@link Reader#getReturnValues()} method of underlying
     * {@link Reader}
     */
    @Test
    public void testGetReturnValues() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.getReturnValues();

        verify(wrappedReader).getReturnValues();
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#close()} calls {@link Reader#close()} method of underlying {@link Reader}
     */
    @Test
    public void testClose() throws IOException {
        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        reader.close();

        verify(wrappedReader).close();
    }

    /**
     * Checks {@link FlowVariablesReader#getCurrent()} returns Main data without any changes when
     * wrapped {@link Reader} returns Main data
     */
    @Test
    public void testGetCurrentMain() throws IOException {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault().endRecord(); //

        IndexedRecord mainRecord = new GenericData.Record(mainSchema);
        mainRecord.put(0, "Abraham Lincoln");
        mainRecord.put(1, 208);

        Reader<IndexedRecord> wrappedReader = mock(Reader.class);
        when(wrappedReader.getCurrent()).thenReturn(mainRecord);

        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        Object actualMainData = reader.getCurrent();

        assertEquals(mainRecord, actualMainData);
        reader.close();
    }

    /**
     * Checks {@link FlowVariablesReader#getCurrent()} returns Main data when
     * wrapped {@link Reader} returns Root {@link IndexedRecord} and sets Flow variables data to {@link RuntimeContainer}
     */
    @Test
    public void testGetCurrentRoot() throws IOException {
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

        Reader<IndexedRecord> wrappedReader = mock(Reader.class);
        when(wrappedReader.getCurrent()).thenReturn(rootRecord);

        FlowVariablesReader reader = new FlowVariablesReader(wrappedReader, runtimeContainer);
        Object actualData = reader.getCurrent();

        assertEquals(mainRecord, actualData);

        Object flowVariable = runtimeContainer.getComponentData("tComponent_1", "id");
        assertNotNull(flowVariable);
        assertThat(flowVariable, instanceOf(Integer.class));

        assertEquals(123, flowVariable);
        reader.close();
    }
}
