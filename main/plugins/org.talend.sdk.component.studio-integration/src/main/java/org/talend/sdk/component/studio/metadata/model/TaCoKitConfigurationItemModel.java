/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.metadata.model;

import org.talend.core.model.properties.ConnectionItem;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;

/**
 * Wrapper for ConnectionItem, which provides convenient API
 */
public class TaCoKitConfigurationItemModel {

    private final ConnectionItem connectionItem;

    public TaCoKitConfigurationItemModel(final ConnectionItem item) throws Exception {
        this.connectionItem = item;
    }

    public String getDisplayLabel() {
        return connectionItem.getProperty().getDisplayName();
    }

    public ConfigTypeNode getConfigTypeNode() {
        return Lookups.taCoKitCache().getConfigTypeNodeMap().get(getConfigId());
    }

    private String getConfigId() {
        return connectionItem.getTypeName();
    }
}
