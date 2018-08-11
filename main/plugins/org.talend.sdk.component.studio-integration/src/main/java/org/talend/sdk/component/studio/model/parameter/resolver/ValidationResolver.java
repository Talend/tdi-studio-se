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
package org.talend.sdk.component.studio.model.parameter.resolver;

import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;

import java.util.Collection;
import java.util.List;

public class ValidationResolver extends AbstractParameterResolver {

    public ValidationResolver(final PropertyNode actionOwner, final Collection<ActionReference> actions,
            final ValidationListener listener, final ElementParameter redrawParameter) {
        super(listener, actionOwner, getActionRef(actionOwner, actions), redrawParameter);
    }
    
    private static ActionReference getActionRef(final PropertyNode actionOwner, final Collection<ActionReference> actions) {
        if (!actionOwner.getProperty().hasValidation()) {
            throw new IllegalArgumentException("property has no validation");
        }
        final String actionName = actionOwner.getProperty().getValidationName();
        return actions
                .stream()
                .filter(a -> Action.Type.VALIDATION.toString().equals(a.getType()))
                .filter(a -> a.getName().equals(actionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action with name " + actionName + " wasn't found"));
    }

    protected final List<String> getRelativePaths() {
        return actionOwner.getProperty().getValidationParameters();
    }
}
