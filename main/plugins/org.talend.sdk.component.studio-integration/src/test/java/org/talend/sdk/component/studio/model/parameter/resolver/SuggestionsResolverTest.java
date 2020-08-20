/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.model.parameter.resolver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.SuggestionsAction;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TableElementParameter;
import org.talend.sdk.component.studio.test.TestComponent;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuggestionsResolverTest {

    private static TestComponent component;

    @BeforeAll
    public static void init() throws Exception {
        component = TestComponent.load("suggestions.json");
    }

    @Test
    public void testResolveParametersOrder() {
        final Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("p", "primitive value");
        expectedPayload.put("a", "another value");

        final PropertyNode actionOwner = component.getNode("conf.basedOnTwoPrimitives");
        final Collection<ActionReference> actions = component.getActions();
        final ActionReference actionRef = component.getAction("basedOnTwoPrimitives");
        final ActionMock action = new ActionMock(actionRef.getName(), actionRef.getFamily());
        final SuggestionsResolver resolver = new SuggestionsResolver(action, actionOwner, actions);

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        assertEquals(expectedPayload, action.checkPayload());
    }

    @Test
    public void testResolveParametersOrderComplex() {
        final Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("c.complexString", "complex string");
        expectedPayload.put("c.complexInt", "-1");
        expectedPayload.put("ds.url", "http://initial.url");
        expectedPayload.put("ds.port", "8080");

        final PropertyNode actionOwner = component.getNode("conf.basedOnComplex");
        final Collection<ActionReference> actions = component.getActions();
        final ActionReference actionRef = component.getAction("basedOnComplex");
        final ActionMock action = new ActionMock(actionRef.getName(), actionRef.getFamily());
        final SuggestionsResolver resolver = new SuggestionsResolver(action, actionOwner, actions);

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        assertEquals(expectedPayload, action.checkPayload());
    }

    @Test
    public void testResolveParametersTable() {
        final Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("table[0].check", "true");
        expectedPayload.put("table[0].number", "1");
        expectedPayload.put("table[0].operator", "GREATER");
        expectedPayload.put("table[0].strColumn", "Talend");
        expectedPayload.put("table[1].check", "false");
        expectedPayload.put("table[1].number", "2");
        expectedPayload.put("table[1].operator", "LESS");
        expectedPayload.put("table[1].strColumn", "The best");

        final PropertyNode actionOwner = component.getNode("conf.basedOnTable");
        final Collection<ActionReference> actions = component.getActions();
        final ActionReference actionRef = component.getAction("basedOnTable");
        final ActionMock action = new ActionMock(actionRef.getName(), actionRef.getFamily());
        final SuggestionsResolver resolver = new SuggestionsResolver(action, actionOwner, actions);

        final Map<String, IElementParameter> settings = component.getSettings();
        final TableElementParameter tableParam = createTableParameter();
        settings.put("conf.table", tableParam);

        resolver.resolveParameters(settings);

        assertEquals(expectedPayload, action.checkPayload());
    }

    private TableElementParameter createTableParameter() {

        final List<Map<String, String>> tableValue = new ArrayList<>();
        final Map<String, String> row1 = new HashMap<>();
        row1.put("conf.table[].check", "true");
        row1.put("conf.table[].number", "1");
        row1.put("conf.table[].operator", "GREATER");
        row1.put("conf.table[].strColumn", "Talend");
        tableValue.add(row1);
        final Map<String, String> row2 = new HashMap<>();
        row2.put("conf.table[].check", "false");
        row2.put("conf.table[].number", "2");
        row2.put("conf.table[].operator", "LESS");
        row2.put("conf.table[].strColumn", "The best");
        tableValue.add(row2);

        final TableElementParameter tableParam = new TableElementParameter(null, Collections.emptyList());
        tableParam.setName("conf.table");
        tableParam.setValue(tableValue);
        return tableParam;
    }

    private static class ActionMock extends SuggestionsAction {

        public ActionMock(final String actionName, final String family) {
            super(actionName, family);
        }

        public Map<String, String> checkPayload() {
            return super.payload();
        }

    }
}