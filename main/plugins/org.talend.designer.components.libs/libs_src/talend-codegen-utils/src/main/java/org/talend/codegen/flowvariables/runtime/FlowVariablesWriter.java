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
import java.io.Writer;
import java.util.Iterator;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.component.runtime.RootRecordUtils;

/**
 * This is Decorator for {@link WriterWithFeedback}. Simple {@link Writer} has no means to output data (both primitive and
 * {@link IndexedRecord}).
 * So, components which produce flow variables should implement {@link WriterWithFeedback} interface. This class is supposed to be
 * used in
 * job generated code. It adds flow variables handling functionality for component {@link WriterWithFeedback}
 *
 * <p>
 * It adds additional functionality for {@link WriterWithFeedback#getRejectedWrites()} and
 * {@link WriterWithFeedback#getSuccessfulWrites()}
 * methods. All other methods are remain unchanged.
 */
public class FlowVariablesWriter<WriteT> implements WriterWithFeedback<WriteT, Object, Object> {

    /**
     * Component {@link WriterWithFeedback}
     */
    private WriterWithFeedback<WriteT, ?, ?> wrappedWriter;

    /**
     * Job runtime container
     */
    private final RuntimeContainer runtimeContainer;

    /**
     * Denotes whether current success data is first or not
     */
    private boolean firstSuccessData = true;

    /**
     * Strategy for success data processing
     */
    private DataProcessor successDataProcessor;

    /**
     * Denotes whether current reject data is first or not
     */
    private boolean firstRejectData = true;

    /**
     * Strategy for reject data processing
     */
    private DataProcessor rejectDataProcessor;

    /**
     * Constructor sets wrapped {@link WriterWithFeedback} and job {@link RuntimeContainer}
     *
     * @param writerWithFeedback {@link WriterWithFeedback} to be wrapped inside this Decorator
     * @param runtimeContainer {@link RuntimeContainer} which is used to set flow variables
     */
    public FlowVariablesWriter(WriterWithFeedback<WriteT, ?, ?> writerWithFeedback, RuntimeContainer runtimeContainer) {
        this.wrappedWriter = writerWithFeedback;
        this.runtimeContainer = runtimeContainer;
    }

    @Override
    public void open(String uId) throws IOException {
        wrappedWriter.open(uId);
    }

    @Override
    public void write(Object object) throws IOException {
        wrappedWriter.write(object);
    }

    @Override
    public WriteT close() throws IOException {
        return wrappedWriter.close();
    }

    @Override
    public WriteOperation<WriteT> getWriteOperation() {
        return wrappedWriter.getWriteOperation();
    }

    /**
     * Retrieves successful writes from wrapped {@link WriterWithFeedback}. Cleans writes from flow variables (aka Out of band)
     * data,
     * stores flow variables in {@link RuntimeContainer} in case they are present
     *
     * <p>
     * It checks whether {@link Iterable} content is Root records or not and chooses appropriate processing strategy
     * during first call to this method.
     *
     * <p>
     * Flow variables data is retrieved and stored only from first element of incoming {@link Iterable}
     * It is assumed that {@link Iterable} contains only 1 element in most cases. More elements have no sense
     * for flow variables mechanism, so they are ignored.
     *
     * <p>
     * Also it is assumed that every element in {@link Iterable} is instance of the same class (has same schema if it is
     * {@link IndexedRecord}). Otherwise errors could appeared during Runtime
     *
     * @return {@link Iterable} with main data which is released from flow variables data
     */
    @Override
    public Iterable<Object> getSuccessfulWrites() {
        Iterable successfulWrites = wrappedWriter.getSuccessfulWrites();

        if (successfulWrites == null) {
            throw new NullPointerException("Null successful writes is not allowed");
        }

        if (firstSuccessData) {
            Iterator<?> writesIterator = successfulWrites.iterator();
            if (writesIterator.hasNext()) {
                Object firstData = writesIterator.next();
                if (RootRecordUtils.isRootRecord(firstData)) {
                    IndexedRecord rootRecord = (IndexedRecord) firstData;
                    successDataProcessor = new FlowVariablesProcessor(runtimeContainer);
                    ((FlowVariablesProcessor) successDataProcessor).initSchema(rootRecord);
                } else {
                    successDataProcessor = new MainDataProcessor();
                }
                firstSuccessData = false;
            } else {
                return successfulWrites;
            }
        }
        return successDataProcessor.processDataIterable(successfulWrites);
    }

    /**
     * Retrieves rejected writes from wrapped {@link WriterWithFeedback}. Cleans writes from flow variables (aka Out of band)
     * data,
     * stores flow variables in {@link RuntimeContainer} in case they are present
     *
     * <p>
     * It checks whether {@link Iterable} content is Root records or not and chooses appropriate processing strategy
     * during first call to this method.
     *
     * <p>
     * Flow variables data is retrieved and stored only from first element of incoming {@link Iterable}
     * It is assumed that {@link Iterable} contains only 1 element in most cases. More elements have no sense
     * for flow variables mechanism, so they are ignored.
     *
     * <p>
     * Also it is assumed that every element in {@link Iterable} is instance of the same class (has same schema if it is
     * {@link IndexedRecord}). Otherwise errors could appeared during Runtime
     *
     * @return {@link Iterable} with rejected data which is released from flow variables data
     */
    @Override
    public Iterable<Object> getRejectedWrites() {
        Iterable rejectedWrites = wrappedWriter.getRejectedWrites();

        if (rejectedWrites == null) {
            throw new NullPointerException("Null rejected writes is not allowed");
        }

        if (firstRejectData) {
            Iterator<?> writesIterator = rejectedWrites.iterator();
            if (writesIterator.hasNext()) {
                Object firstData = writesIterator.next();
                if (RootRecordUtils.isRootRecord(firstData)) {
                    IndexedRecord rootRecord = (IndexedRecord) firstData;
                    rejectDataProcessor = new FlowVariablesProcessor(runtimeContainer);
                    ((FlowVariablesProcessor) rejectDataProcessor).initSchema(rootRecord);
                } else {
                    rejectDataProcessor = new MainDataProcessor();
                }
                firstSuccessData = false;
            } else {
                return rejectedWrites;
            }
        }
        return rejectDataProcessor.processDataIterable(rejectedWrites);
    }

    @Override
    public void cleanWrites() {
        wrappedWriter.cleanWrites();
    }

}
