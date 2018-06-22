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

import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.parameter.SuggestionValues;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

public class SuggestionsAction extends Action {

    public SuggestionsAction(String actionName, String family) {
        super(actionName, family, Action.Type.SUGGESTIONS);
    }
    
    public SuggestionValues callSuggestions() {
        final V1Action action = Lookups.client().v1().action();
        return action.execute(SuggestionValues.class, getFamily(), getType(), getActionName(), parameters.payload());
    }
    
    @Override
    public Map<String, String> callback() {
        final SuggestionValues response = callSuggestions();
        final Map<String, String> result = new HashMap<>();
        result.put("cacheble", String.valueOf(response.isCacheable()));
        response.getItems().forEach(item -> {
            result.put(item.getId(), item.getLabel());
        });
        return result;
    }

}
