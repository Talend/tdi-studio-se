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

import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.parameter.SuggestionValues;
import org.talend.sdk.component.studio.ui.composite.controller.TaCoKitValueSelectionController;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

/**
 * It should be Thread-safe as it is used in a Job launched by {@link TaCoKitValueSelectionController}
 */
public final class SuggestionsAction extends Action {
    
    private final V1Action actionService = Lookups.client().v1().action();
    
    /**
     * Denotes whetere parameters were changed since last callback
     */
    private boolean parametersChanged = true;
    
    /**
     * Cached server response
     */
    private SuggestionValues cachedValue;

    public SuggestionsAction(String actionName, String family) {
        super(actionName, family, Action.Type.SUGGESTIONS);
    }
    
    private synchronized SuggestionValues callSuggestions() {
        SuggestionValues result;
        if (cachedValue == null || parametersChanged) {
            final SuggestionValues values = actionService.execute(SuggestionValues.class, getFamily(), getType(), getActionName(), payload());
            if (values.isCacheable()) {
                cachedValue = values.clone();
            }
            result = values;
        } else {
            result = cachedValue.clone();
        }
        parametersChanged = false;
        return result;
    }
    
    /**
     * Retrieves suggestion values and return a map of them, where label is a key and id is value
     */
    @Override
    public Map<String, String> callback() {
        final SuggestionValues response = callSuggestions();
        final Map<String, String> result = new LinkedHashMap<>();
        response.getItems().forEach(item -> {
            result.put(item.getLabel(), item.getId());
        });
        return result;
    }
    
    /**
     * Extends {@link Action#setParameterValue(String, String)} to set {@link #parametersChanged} flag to true
     */
    @Override
    public synchronized void setParameterValue(final String parameterName, final String parameterValue) {
        super.setParameterValue(parameterName, parameterValue);
        parametersChanged = true;
    }

}
