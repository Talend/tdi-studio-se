package org.talend.sdk.component.studio.model.parameter.resolver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.listener.ActionParametersUpdater;
import org.talend.sdk.component.studio.test.TestComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuggestionsResolverTest {

    private static TestComponent component;

    @BeforeAll
    public static void init() throws Exception {
        component = TestComponent.load("suggestions.json");
    }

    @Test
    public void testResolveParametersOrder() {
        Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("p", "primitive value");
        expectedPayload.put("a", "another value");

        final PropertyNode actionOwner = component.getNode("conf.basedOnTwoPrimitives");
        final Collection<ActionReference> actions = component.getActions();
        final ActionParametersUpdater listener = createActionParametersUpdater();
        final SuggestionsResolver resolver = new SuggestionsResolver(actionOwner, actions, listener);

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        final ActionMock action = (ActionMock) listener.getAction();

        assertEquals(expectedPayload, action.checkPayload());
    }

    @Test
    public void testResolveParametersOrderComplex() {
        Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("c.complexString", "complex string");
        expectedPayload.put("c.complexInt", "-1");
        expectedPayload.put("ds.url", "http://initial.url");

        final PropertyNode actionOwner = component.getNode("conf.basedOnComplex");
        final Collection<ActionReference> actions = component.getActions();
        final ActionParametersUpdater listener = createActionParametersUpdater();
        final SuggestionsResolver resolver = new SuggestionsResolver(actionOwner, actions, listener);

        final Map<String, IElementParameter> settings = component.getSettings();
        resolver.resolveParameters(settings);

        final ActionMock action = (ActionMock) listener.getAction();

        assertEquals(expectedPayload, action.checkPayload());
    }

    private SuggestionsResolver createResolver(final PropertyNode actionOwner, final ActionParametersUpdater listener) {
        return new SuggestionsResolver(actionOwner, component.getActions(), listener);
    }

    private ActionParametersUpdater createActionParametersUpdater() {
        final ActionReference action = component.getAction("basedOnTwoPrimitives");
        final Action actionMock = new ActionMock(action.getName(), action.getFamily(), Action.Type.SUGGESTIONS);
        return new ActionParametersUpdater(actionMock);
    }

    private static class ActionMock extends Action {

        public ActionMock(final String actionName, final String family, final Type type) {
            super(actionName, family, type);
        }

        public Map<String, String> checkPayload() {
            return super.payload();
        }

    }
}