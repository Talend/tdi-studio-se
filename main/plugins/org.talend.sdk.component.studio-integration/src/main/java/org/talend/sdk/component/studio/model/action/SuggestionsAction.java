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
package org.talend.sdk.component.studio.model.action;

import org.talend.sdk.component.studio.model.parameter.SuggestionValues;
import org.talend.sdk.component.studio.ui.composite.controller.TaCoKitValueSelectionController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * It should be Thread-safe as it is used in a Job launched by {@link TaCoKitValueSelectionController}
 */
public class SuggestionsAction extends Action<String> {

    /**
     * Cached previously used payload and corresponding response
     */
    private Cache cached;

    public SuggestionsAction(String actionName, String family) {
        super(actionName, family, Action.Type.SUGGESTIONS);
    }

    private synchronized SuggestionValues callSuggestions() {
        final Map<String, String> newPayload = payload();
        if (responseNotCached() || payloadChanged(newPayload)) {
            return doCallback(newPayload);
        } else {
            return cached.response();
        }
    }

    private boolean responseNotCached() {
        return cached == null;
    }

    private boolean payloadChanged(final Map<String, String> newPayload) {
        return !cached.payload().equals(newPayload);
    }

    private SuggestionValues doCallback(Map<String, String> payload) {
        final SuggestionValues response = actionClient().execute(SuggestionValues.class, getFamily(), getType(), getActionName(), payload);
        if (response.isCacheable()) {
            cached = new Cache(payload, response);
        } else {
            cached = null;
        }
        return response;
    }

    /**
     * Retrieves suggestion values and return a map of them, where label is a key and id is value
     */
    @Override
    public Map<String, String> callback() {
        final SuggestionValues response = callSuggestions();
        final Map<String, String> result = new LinkedHashMap<>();
        response.getItems().forEach(item -> result.put(item.getLabel(), item.getId()));
        return result;
    }

    private static class Cache {

        private final Map<String, String> cachedPayload;

        private final SuggestionValues cachedResponse;

        Cache(final Map<String, String> payload, SuggestionValues response) {
            this.cachedPayload = payload;
            this.cachedResponse = response;
        }

        Map<String, String> payload() {
            return cachedPayload;
        }

        SuggestionValues response() {
            return cachedResponse;
        }
    }

}
