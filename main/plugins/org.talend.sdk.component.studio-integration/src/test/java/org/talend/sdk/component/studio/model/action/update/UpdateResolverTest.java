// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.model.action.update;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.test.TestComponent;

public class UpdateResolverTest {

    private static TestComponent component;

    @BeforeAll
    public static void init() throws Exception {
        component = TestComponent.load("update.json");
    }

    /**
     * Checks {@link UpdateResolver#resolveParameters(Map)} resolves action parameters correctly
     * Checks {@link UpdateResolver#resolveParameters(Map)} resolves parameters for update (parameters which are
     * updated according value from action result) correctly. This check is done via calling {@link UpdateCommand#onResult(Map)}
     */
    @Test
    public void testResolveParametersWithNestedConfig() throws Exception {
        final Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("c", "LESS");
        expectedPayload.put("s", "Hello world!");

        final PropertyNode actionOwner = component.getNode("conf.updatableConfig");
        final Collection<ActionReference> actions = component.getActions();
        final ActionReference actionRef = component.getAction("updatableConfig");
        final ActionMock action = new ActionMock(actionRef.getName(), actionRef.getFamily());
        final Map<String, IElementParameter> settings = component.getSettings();
        final UpdateResolver resolver = new UpdateResolver(null, EComponentCategory.ADVANCED, 0,
                action, actionOwner,actions, null, settings, () -> true);

        resolver.resolveParameters(settings);

        Assertions.assertEquals(expectedPayload, action.checkPayload());

        final ButtonParameter button = (ButtonParameter) settings.get("conf.updatableConfig.update");
        final UpdateCommand command = (UpdateCommand) button.getCommand();
        final Map<String, Object> actionResult = loadActionResult("updateActionResult.json");
        command.onResult(actionResult);

        Assertions.assertEquals("1st level str value", settings.get("conf.updatableConfig.str").getValue());
        Assertions.assertEquals("2nd level str1 value", settings.get("conf.updatableConfig.nestedConfig.str1").getValue());
        Assertions.assertEquals("2nd level str2 value", settings.get("conf.updatableConfig.nestedConfig.str2").getValue());
    }

    private Map<String, Object> loadActionResult(final String resource)  throws Exception {
        try (final Jsonb jsonb = JsonbBuilder.create();
             final InputStream stream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            return jsonb.fromJson(stream, Map.class);
        }
    }

    private static class ActionMock extends UpdateAction {

        public ActionMock(final String actionName, final String family) {
            super(actionName, family);
        }

        public Map<String, String> checkPayload() {
            return super.payload();
        }

    }

}
