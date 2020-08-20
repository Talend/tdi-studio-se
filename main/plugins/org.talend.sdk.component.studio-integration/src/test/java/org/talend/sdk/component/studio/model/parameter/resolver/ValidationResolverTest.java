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
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;
import org.talend.sdk.component.studio.test.TestComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidationResolverTest {

    private static TestComponent component;

    @BeforeAll
    public static void init() throws Exception {
        component = TestComponent.load("suggestions.json");
    }

    @Test
    public void testResolveParametersOrderPrimitives() {
        Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("xPort", "8080");
        expectedPayload.put("yUrl", "http://initial.url");
        expectedPayload.put("z", "based on 2 primitives");

        final PropertyNode actionOwner = component.getNode("conf.basedOnTwoPrimitives");
        final Collection<ActionReference> actions = component.getActions();
        final ValidationListenerMock listener = createValidationListener(component.getAction("checkSeveral"));
        final ValidationResolver resolver = new ValidationResolver(actionOwner, component.getActions(), listener, new ElementParameter(null));

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        assertEquals(expectedPayload, listener.checkPayload());

    }

    @Test
    public void testResolveParametersOrderComplex() {
        Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("s", "based on complex");
        expectedPayload.put("d.url", "http://initial.url");
        expectedPayload.put("d.port", "8080");

        final PropertyNode actionOwner = component.getNode("conf.basedOnComplex");
        final Collection<ActionReference> actions = component.getActions();
        final ValidationListenerMock listener = createValidationListener(component.getAction("checkComplex"));
        final ValidationResolver resolver = new ValidationResolver(actionOwner, component.getActions(), listener, new ElementParameter(null));

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        assertEquals(expectedPayload, listener.checkPayload());
    }

    private ValidationListenerMock createValidationListener(final ActionReference action) {
        return new ValidationListenerMock(action.getFamily(), action.getName());
    }

    private static class ValidationListenerMock extends ValidationListener {

        public ValidationListenerMock(final String family, final String actionName) {
            super(null, family, actionName);
        }

        public Map<String, String> checkPayload() {
            return super.payload();
        }
    }

}
