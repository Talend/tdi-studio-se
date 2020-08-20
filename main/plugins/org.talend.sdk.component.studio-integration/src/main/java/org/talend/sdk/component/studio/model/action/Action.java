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
package org.talend.sdk.component.studio.model.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

public class Action<T> {

    public static final String STATUS = "status";

    public static final String OK = "OK";

    public static final String KO = "KO";

    public static final String MESSAGE = "comment";

    private V1Action actionClient;

    private final String actionName;

    private final String family;

    private final String type;

    /**
     * Action parameters map. Key is an ElementParameter path. Value is a list of action parameters associated with the ElementParameter
     */
    private final Map<String, List<IActionParameter>> parameters = new HashMap<>();

    public Action(final String actionName, final String family, final Type type) {
        this.actionName = actionName;
        this.family = family;
        this.type = type.toString();
    }

    /**
     * Adds specified {@code parameter} to this Action.
     * ActionParameter passed should be unique action parameter.
     *
     * @param parameter ActionParameter to be added
     */
    public void addParameter(final IActionParameter parameter) {
        Objects.requireNonNull(parameter, "parameter should not be null");
        final String elementParameter = parameter.getName();
        List<IActionParameter> list = parameters.computeIfAbsent(elementParameter, k -> new ArrayList<>());

        if (list.contains(parameter)) {
            throw new IllegalArgumentException("action already contains parameter " + parameter);
        }
        list.add(parameter);
    }

    @SuppressWarnings("unchecked")
    public Map<String, T> callback() {
        return actionClient().execute(Map.class, family, type, actionName, payload());
    }

    protected final String getActionName() {
        return this.actionName;
    }

    protected final String getFamily() {
        return this.family;
    }

    protected final String getType() {
        return this.type;
    }

    protected final Map<String, String> payload() {
        final Map<String, String> payload = new HashMap<>();
        parameters.values().stream()
                .flatMap(List::stream)
                .flatMap(actionParam -> actionParam.parameters().stream())
                .forEach(param -> {
                    payload.put(param.getFirst(), param.getSecond());
                 });
        return payload;
    }

    public enum Type {
        HEALTHCHECK,
        SUGGESTIONS,
        VALIDATION,
        UPDATE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    protected V1Action actionClient() {
        if (actionClient == null) {
            actionClient = Lookups.client().v1().action();
        }
        return actionClient;
    }

}
