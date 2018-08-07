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

import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.listener.ActionParametersUpdater;

import java.util.Collection;
import java.util.List;

public class SuggestionsResolver extends AbstractParameterResolver {

    /**
     * Constructor
     *
     * @param actionOwner
     * @param actions
     * @param updater ActionParameterUpdater which updates action parameters whenever corresponding ElementParameters are changed
     */
    public SuggestionsResolver(final PropertyNode actionOwner, final Collection<ActionReference> actions, final ActionParametersUpdater updater) {
        super(updater.getAction(), actionOwner, getActionRef(actionOwner, actions), updater);
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

    @Override
    protected final List<String> getRelativePaths() {
        return actionOwner.getProperty().getSuggestions().getParameters();
    }

}
