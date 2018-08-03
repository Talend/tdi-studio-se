package org.talend.sdk.component.studio.test;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_PARAMETERS;

public class TestComponent {

    private final Map<String, IElementParameter> settings;

    private final Map<String, ActionReference> actions;

    private final Map<String, PropertyNode> nodes;

    public TestComponent() {
        this.settings = createSettings();
        this.actions = createActions();
        this.nodes = createNodes();
    }

    public PropertyNode getNode(final String path) {
        return nodes.get(path);
    }

    public Map<String, IElementParameter> getSettings() {
        return settings;
    }

    public ActionReference getAction(final String actionName) {
        return actions.get(actionName);
    }

    public Collection<ActionReference> getActions() {
        return actions.values();
    }

    /**
     * Creates leaf options
     *
     * @return leaf options
     */
    private Map<String, IElementParameter> createSettings() {
        final Map<String, IElementParameter> settings = new LinkedHashMap<>();

        final TaCoKitElementParameter p1 = mock(TaCoKitElementParameter.class);
        when(p1.getName()).thenReturn("conf.primitive");
        when(p1.getStringValue()).thenReturn("primitive value");
        settings.put("conf.primitive", p1);

        final TaCoKitElementParameter p2 = mock(TaCoKitElementParameter.class);
        when(p2.getName()).thenReturn("conf.another");
        when(p2.getStringValue()).thenReturn("another value");
        settings.put("conf.another", p2);

        final TaCoKitElementParameter p3 = mock(TaCoKitElementParameter.class);
        when(p3.getName()).thenReturn("conf.basedOnTwoPrimitives");
        when(p3.getStringValue()).thenReturn("based on two");
        settings.put("conf.basedOnTwoPrimitives", p3);

        return settings;
    }

    private Map<String, ActionReference> createActions() {
        final Map<String, ActionReference> actions = new LinkedHashMap<>();

        final ActionReference a1 = new ActionReference();
        a1.setName("basedOnTwoPrimitives");
        a1.setFamily("test");
        a1.setType("suggestions");
        a1.setProperties(createBasedOnTwoPrimitivesActionProperties());
        actions.put("basedOnTwoPrimitives", a1);

        return actions;
    }

    private Collection<SimplePropertyDefinition> createBasedOnTwoPrimitivesActionProperties() {
        Collection<SimplePropertyDefinition> properties = new ArrayList<>();
        final SimplePropertyDefinition p1 = new SimplePropertyDefinition();
        p1.setPath("a");
        p1.setName("a");
        final Map<String, String> metadata1 = new HashMap<>();
        metadata1.put(Metadatas.PARAMETER_INDEX, "1");
        p1.setMetadata(metadata1);
        properties.add(p1);

        final SimplePropertyDefinition p2 = new SimplePropertyDefinition();
        p2.setPath("p");
        p2.setName("p");
        final Map<String, String> metadata2 = new HashMap<>();
        metadata2.put(Metadatas.PARAMETER_INDEX, "0");
        p2.setMetadata(metadata2);
        properties.add(p2);

        return properties;
    }

    private Map<String, PropertyNode> createNodes() {
        final Map<String, PropertyNode> nodes = new LinkedHashMap<>();

        final Map<String, String> metadata1 = new HashMap<>();
        metadata1.put(ACTION_SUGGESTIONS_NAME, "basedOnTwoPrimitives");
        metadata1.put(ACTION_SUGGESTIONS_PARAMETERS, "primitive,another");
        final SimplePropertyDefinition def1 = new SimplePropertyDefinition();
        def1.setName("basedOnTwoPrimitives");
        def1.setPath("conf.basedOnTwoPrimitives");
        def1.setMetadata(metadata1);
        final PropertyDefinitionDecorator p1 = new PropertyDefinitionDecorator(def1);
        final PropertyNode n1 = new PropertyNode(p1, null, false);
        nodes.put("conf.basedOnTwoPrimitives", n1);

        return nodes;
    }
}
