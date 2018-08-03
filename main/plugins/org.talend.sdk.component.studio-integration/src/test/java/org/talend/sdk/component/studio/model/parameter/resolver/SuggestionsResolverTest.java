package org.talend.sdk.component.studio.model.parameter.resolver;

import org.junit.jupiter.api.Test;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.listener.ActionParametersUpdater;
import org.talend.sdk.component.studio.test.TestComponent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuggestionsResolverTest {

    private final TestComponent testComponent = new TestComponent();

    @Test
    public void testResolveParametersOrder() {
        Map<String, String> expectedPayload = new HashMap<>();
        expectedPayload.put("p", "primitive value");
        expectedPayload.put("a", "another value");

        final PropertyNode actionOwner = testComponent.getNode("conf.basedOnTwoPrimitives");
        final ActionParametersUpdater listener = createActionParametersUpdater();
        final SuggestionsResolver resolver = createResolver(actionOwner, listener);

        final Map<String, IElementParameter> settings = testComponent.getSettings();
        resolver.resolveParameters(settings);

        final ActionMock action = (ActionMock) listener.getAction();

        assertEquals(expectedPayload, action.checkPayload());
    }

    private SuggestionsResolver createResolver(final PropertyNode actionOwner, final ActionParametersUpdater listener) {
        return new SuggestionsResolver(actionOwner, testComponent.getActions(), listener);
    }

    private ActionParametersUpdater createActionParametersUpdater() {
        final ActionReference action = testComponent.getAction("basedOnTwoPrimitives");
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