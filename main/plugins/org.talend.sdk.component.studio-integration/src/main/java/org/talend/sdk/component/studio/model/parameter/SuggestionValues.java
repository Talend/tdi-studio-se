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
package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Response from Suggestions action server request.
 * This class is used to store deserialized response value from json
 */
public class SuggestionValues implements Cloneable {

    /**
     * Does the client can cache the values after the first request or should
     * call the action each time.
     */
    private boolean cacheable;

    /**
     * List of pairs to show in the suggestions in the UI.
     */
    private Collection<Item> items;

    public SuggestionValues() {
        // no-op
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public void setItems(Collection<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "SuggestionValues(cacheable=" + this.isCacheable() + ", items=" + this.getItems() + ")";
    }

    @Override
    public SuggestionValues clone() {
        try {
            final SuggestionValues result = (SuggestionValues) super.clone();
            final Collection<Item> originalItems = result.items;
            result.items = new ArrayList<>(originalItems.size());
            originalItems.forEach(item -> result.items.add(item.clone()));
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class Item implements Cloneable {

        private String id;

        private String label;

        public Item() {
            // no-op
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "Item(id=" + this.getId() + ", label=" + this.getLabel() + ")";
        }

        @Override
        public Item clone() {
            try {
                return (Item) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }
}
