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
package org.talend.sdk.component.studio.model.connector;

import java.util.List;

import org.talend.core.model.process.INodeConnector;

/**
 * Strategy for component connectors creating. Component should have connectors
 * for all types listed in EConnectionType When some connector type is not
 * applicable for the component, then this connector has 0 max incoming and
 * outgoing links
 *
 */
public interface ConnectorCreator {

    /**
     * Creates component connectors
     *
     * @return component connectors
     */
    List<INodeConnector> createConnectors();

}
