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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Unit-tests for {@link MainDataProcessor} class
 */
public class MainDataProcessorTest {

    /**
     * Checks {@link MainDataProcessor#processData(Object)} returns input Object without any changes
     */
    @Test
    public void testProcessData() {
        Object expectedObject = new Object();
        MainDataProcessor mainDataProcessor = new MainDataProcessor();
        Object actualObject = mainDataProcessor.processData(expectedObject);
        assertEquals(expectedObject, actualObject);
    }

    /**
     * Checks {@link MainDataProcessor#processDataIterable(Object)} returns input {@link Iterable} without any changes
     */
    @Test
    public void testProcessDataIterable() {
        Iterable<Object> expectedIterable = new ArrayList<Object>();
        MainDataProcessor mainDataProcessor = new MainDataProcessor();
        Iterable<Object> actualIterable = mainDataProcessor.processDataIterable(expectedIterable);
        assertEquals(expectedIterable, actualIterable);
    }
}
