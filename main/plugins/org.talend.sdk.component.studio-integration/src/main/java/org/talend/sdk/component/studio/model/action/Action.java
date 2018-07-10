/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

public class Action {

    public static final String STATUS = "status";

    public static final String OK = "OK";

    public static final String KO = "KO";

    public static final String MESSAGE = "comment";

    private final String actionName;

    private final String family;

    private final String type;
    
    private final Map<String, ActionParameter> parameters = new HashMap<>();
    
    public Action(final String actionName, final String family, final Type type) {
        this.actionName = actionName;
        this.family = family;
        this.type = type.toString();
    }

    public void addParameter(final ActionParameter parameter) {
        Objects.requireNonNull(parameter, "parameter should not be null");
        parameters.put(parameter.getName(), parameter);
    }
    
    public void setParameterValue(final String parameterName, final String parameterValue) {
        if (!parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Non-existent parameter: " + parameterName);
        }
        parameters.get(parameterName).setValue(parameterValue);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> callback() {
        final V1Action action = Lookups.client().v1().action();
        return action.execute(Map.class, family, type, actionName, payload());
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
    
    protected final boolean areParametersSet() {
        return parameters.values().stream().allMatch(ActionParameter::isHasDirectValue);
    }
    
    protected final Map<String, String> payload() {
        final Map<String, String> payload = new HashMap<>();
        parameters.values().forEach(actionParameter -> {
            payload.put(actionParameter.getParameter(), actionParameter.getValue());
        });
        return payload;
    }
    
    public enum Type {
        HEALTHCHECK,
        SUGGESTIONS,
        VALIDATION;
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

}
