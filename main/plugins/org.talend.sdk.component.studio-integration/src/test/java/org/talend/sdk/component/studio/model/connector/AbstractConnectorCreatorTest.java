/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.EConnectionType;

/**
 * Unit-tests for {@link AbstractConnectorCreator}
 */
class AbstractConnectorCreatorTest {

    private static final String DEFAULT = "__default__";

    @Test
    void testGetTypeDefault() {
        EConnectionType expectedType = EConnectionType.FLOW_MAIN;
        EConnectionType actualType = AbstractConnectorCreator.getType(DEFAULT);
        assertEquals(expectedType, actualType);
    }

    @Test
    void testGetTypeMain() {
        EConnectionType expectedType = EConnectionType.FLOW_MAIN;
        EConnectionType actualType = AbstractConnectorCreator.getType("Main");
        assertEquals(expectedType, actualType);
    }

    @Test
    void testGetTypeReject() {
        EConnectionType expectedType = EConnectionType.REJECT;
        EConnectionType actualType = AbstractConnectorCreator.getType("reject");
        assertEquals(expectedType, actualType);
    }

    @Test
    void testGetTypeRejectUpperCase() {
        EConnectionType expectedType = EConnectionType.REJECT;
        EConnectionType actualType = AbstractConnectorCreator.getType("REJECT");
        assertEquals(expectedType, actualType);
    }

    @Test
    void testGetNameDefault() {
        String expectedName = EConnectionType.FLOW_MAIN.getName();
        String actualName = AbstractConnectorCreator.getName(DEFAULT);
        assertEquals(expectedName, actualName);
    }

    @Test
    void testGetNameAny() {
        String expectedName = "Any";
        String actualName = AbstractConnectorCreator.getName("Any");
        assertEquals(expectedName, actualName);
    }
}
