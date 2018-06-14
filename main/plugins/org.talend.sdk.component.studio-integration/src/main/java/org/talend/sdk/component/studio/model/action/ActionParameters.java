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
import java.util.stream.Collectors;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;

/**
 * Manages {@link ActionParameter}. It is used as a part of
 * 
 * {@link ValidationListener}
 */
public class ActionParameters {

    private final Map<String, ActionParameter> parameters = new HashMap<>();

    public void add(final ActionParameter parameter) {
        Objects.requireNonNull(parameter, "parameter should not be null");
        parameters.put(parameter.getName(), parameter);
    }

    public void setValue(final String parameterName, final String parameterValue) {
        if (!parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Non-existent parameter: " + parameterName);
        }
        parameters.get(parameterName).setValue(parameterValue);
    }

    public boolean areSet() {
        return parameters.values().stream().allMatch(ActionParameter::isHasDirectValue);
    }

    public Map<String, String> payload() {
        return parameters.values().stream().collect(Collectors.toMap(ActionParameter::getParameter, ActionParameter::getValue));
    }
}
