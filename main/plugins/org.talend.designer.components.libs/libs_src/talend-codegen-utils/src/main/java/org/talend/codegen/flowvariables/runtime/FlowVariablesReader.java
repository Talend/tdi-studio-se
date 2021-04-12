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

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.component.runtime.RootRecordUtils;

/**
 * This is Decorator for {@link Reader}. It adds additional functionality for {@link Reader#getCurrent()} method of wrapped
 * Reader.
 * All other methods remain unchanged.
 *
 * <p>
 * This class's {@link Reader#getCurrent()} method does following:
 * 1. Checks whether outgoing {@link IndexedRecord} is Root record
 * 2. Retrieves Flow variables (Out of band data) from Root record
 * 3. Fill runtime container with flow variables
 * 4. Unwrap and return Data Record
 */
public class FlowVariablesReader implements Reader<Object> {

    /**
     * Component {@link Reader}
     */
    private final Reader<?> wrappedReader;

    /**
     * Job runtime container
     */
    private final RuntimeContainer runtimeContainer;

    /**
     * Strategy for output data processing
     */
    private DataProcessor dataProcessor;

    /**
     * Denotes whether current data is first or not
     */
    private boolean firstData = true;

    /**
     * Constructor sets wrapped {@link Reader} and global map
     *
     * @param reader component {@link Reader}
     * @param runtimeContainer runtime container
     */
    public FlowVariablesReader(Reader<?> reader, RuntimeContainer runtimeContainer) {
        this.wrappedReader = reader;
        this.runtimeContainer = runtimeContainer;
    }

    @Override
    public boolean start() throws IOException {
        return wrappedReader.start();
    }

    @Override
    public boolean advance() throws IOException {
        return wrappedReader.advance();
    }

    /**
     * Checks whether outgoing {@link IndexedRecord} is Root record and chooses appropriate processing strategy
     * Retrieves Main and Flow variables (aka Out of band) data from Root record,
     * stores Flow variables in {@link RuntimeContainer} in case of Root record
     *
     * @return Main data
     */
    @Override
    public Object getCurrent() throws NoSuchElementException {
        Object data = wrappedReader.getCurrent();
        if (firstData) {
            if (RootRecordUtils.isRootRecord(data)) {
                dataProcessor = new FlowVariablesProcessor(runtimeContainer);
                ((FlowVariablesProcessor) dataProcessor).initSchema((IndexedRecord) data);
            } else {
                dataProcessor = new MainDataProcessor();
            }
            firstData = false;
        }
        return dataProcessor.processData(data);
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        return wrappedReader.getCurrentTimestamp();
    }

    @Override
    public void close() throws IOException {
        wrappedReader.close();
    }

    @Override
    public Source getCurrentSource() {
        return wrappedReader.getCurrentSource();
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return wrappedReader.getReturnValues();
    }

}
