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
package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.ActionParameter;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;

class ValidationResolver extends AbstractParameterResolver {

    private final ActionReference action;

    private final ValidationListener listener;

    private final ElementParameter redrawParameter;

    ValidationResolver(final PropertyNode actionOwner, final Collection<ActionReference> actions,
            final ValidationListener listener, final ElementParameter redrawParameter) {
        super(actionOwner);
        if (!actionOwner.getProperty().hasValidation()) {
            throw new IllegalArgumentException("property has no validation");
        }
        this.listener = listener;
        this.redrawParameter = redrawParameter;
        final String actionName = actionOwner.getProperty().getValidationName();
        this.action = actions
                .stream()
                .filter(a -> Action.Type.VALIDATION.toString().equals(a.getType()))
                .filter(a -> a.getName().equals(actionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action with name " + actionName + " wasn't found"));
    }

    public void resolveParameters(final Map<String, IElementParameter> settings) {
        final List<SimplePropertyDefinition> callbackParameters = new ArrayList<>(action.getProperties());
        final List<String> relativePaths = actionOwner.getProperty().getValidationParameters();

        for (int i = 0; i < relativePaths.size(); i++) {
            final TaCoKitElementParameter parameter = resolveParameter(relativePaths.get(i), settings);
            parameter.registerListener(parameter.getName(), listener);
            parameter.setRedrawParameter(redrawParameter);
            final String callbackParameter = callbackParameters.get(i).getName();
            final String initialValue = callbackParameters.get(i).getDefaultValue();
            listener.addParameter(new ActionParameter(parameter.getName(), callbackParameter, initialValue));
        }

    }
}
