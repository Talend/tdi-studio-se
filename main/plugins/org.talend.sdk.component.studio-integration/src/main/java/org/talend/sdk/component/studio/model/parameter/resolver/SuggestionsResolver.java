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

import static java.util.Comparator.comparing;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.ActionParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.listener.ActionParametersUpdater;

public class SuggestionsResolver extends AbstractParameterResolver {
    
    /**
     * Updates action parameters whenever corresponding ElementParameters are changed
     */
    private final ActionParametersUpdater updater;

    public SuggestionsResolver(final PropertyNode actionOwner, final Collection<ActionReference> actions, final ActionParametersUpdater updater) {
        super(actionOwner, getActionRef(actionOwner, actions));
        this.updater = updater;
    }
    
    private static ActionReference getActionRef(final PropertyNode actionOwner, final Collection<ActionReference> actions) {
        final String actionName = actionOwner.getProperty().getSuggestions().getName();
        return actions
                .stream()
                .filter(a -> Action.Type.SUGGESTIONS.toString().equals(a.getType()))
                .filter(a -> a.getName().equals(actionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action with name " + actionName + " wasn't found"));
    }
    
    /**
     * Finds ElementParameters needed for action call by their relative path.
     * Registers ActionParameterUpdater to each ElementParameter needed for action call
     * Creates ActionParameter for each ElementParameter
     * 
     * @param settings all "leaf" Component options
     */
    public void resolveParameters(final Map<String, IElementParameter> settings) {
        final Iterator<PropertyDefinitionDecorator> expectedParameters = PropertyDefinitionDecorator.wrap(actionRef.getProperties())
                .stream()
                .filter(p -> p.getParameter().isRoot())
                .sorted(comparing(p -> p.getParameter().getIndex()))
                .iterator();
        final List<String> relativePaths = actionOwner.getProperty().getSuggestions().getParameters();
        
        relativePaths.forEach(relativePath -> {
            if (expectedParameters.hasNext()) {
                final String absolutePath = pathResolver.resolvePath(getOwnerPath(), relativePath);
                final List<TaCoKitElementParameter> parameters = resolveParameters(absolutePath, settings);
                final SimplePropertyDefinition parameterRoot = expectedParameters.next();
                parameters.forEach(parameter -> {
                    parameter.registerListener("value", updater);
                    final String callbackProperty = parameter.getName().replaceFirst(absolutePath, parameterRoot.getPath());
                    final String defaultValue = parameter.getStringValue();
                    final ActionParameter actionParameter = new ActionParameter(parameter.getName(), callbackProperty, defaultValue);
                    updater.getAction().addParameter(actionParameter);
                });
            }
        });

    }

}
