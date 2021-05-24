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
package org.talend.sdk.component.studio.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.api.configuration.action.meta.ActionRef;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestComponent {

    public static final Logger LOG = LoggerFactory.getLogger(TestComponent.class);

    private List<ActionReference> actions;

    private String name;

    private List<PropertyNode> nodes;

    private List<SimplePropertyDefinition> properties;

    private List<TaCoKitElementParameter> settings;

    public static TestComponent load(final String resource) throws Exception {
        try (final Jsonb jsonb = JsonbBuilder.create();
             final InputStream stream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            return jsonb.fromJson(stream, TestComponent.class);
        }
    }

    @JsonbCreator
    public TestComponent(@JsonbProperty("actions") final List<ActionReference> actions,
                         @JsonbProperty("name") final String name,
                         @JsonbProperty("properties") final List<SimplePropertyDefinition> properties,
                         @JsonbProperty("nodes") final List<PropertyNode> nodes,
                         @JsonbProperty("settings") final List<TaCoKitElementParameter> settings) {
        this.actions = actions;
        this.name = name;
        this.properties = properties;
        this.nodes = nodes;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public ActionReference getAction(final String name) {
        return actions.stream()
                .filter(a -> name.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no action with name " + name));
    }

    public List<ActionReference> getActions() {
        return new ArrayList<>(this.actions);
    }

    public PropertyNode getNode(final String name) {
        return nodes.stream()
                .filter(p -> name.equals(p.getProperty().getPath()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no property with name " + name));
    }

    public Map<String, IElementParameter> getSettings() {
        final Map<String, IElementParameter> result = new HashMap<>();
        settings.forEach(p -> {
            result.put(p.getName(), p);
        });
        return result;
    }
}
