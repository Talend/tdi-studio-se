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

import static java.util.stream.Collectors.toList;
import static org.talend.core.model.process.EConnectionType.FLOW_MAIN;
import static org.talend.core.model.process.EConnectionType.FLOW_MERGE;
import static org.talend.core.model.process.EConnectionType.FLOW_REF;
import static org.talend.core.model.process.EConnectionType.ITERATE;

import java.util.List;
import java.util.Optional;

import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.sdk.component.server.front.model.ComponentDetail;

/**
 * Creates connectors for PartitionMapper (aka Input) component
 */
class PartitionMapperConnectorCreator extends AbstractConnectorCreator {

    protected PartitionMapperConnectorCreator(final ComponentDetail detail, final INode node) {
        super(detail, node);
    }

    /**
     * PartitionMapper (Input) has no incoming MAIN connection, but may have
     * outgoing
     */
    @Override
    protected List<INodeConnector> createMainConnectors() {
        return detail
                .getOutputFlows()
                .stream() //
                .filter(output -> FLOW_MAIN.equals(getType(output))) //
                .map(output -> { //
                    final TaCoKitNodeConnector main = TaCoKitNodeConnector.newFlow(node, output);
                    main.setMaxLinkInput(0);
                    main.setMaxLinkOutput(1);
                    main.setHasInput(false);
                    main.addConnectionProperty(FLOW_REF, FLOW_REF.getRGB(), FLOW_REF.getDefaultLineStyle());
                    main.addConnectionProperty(FLOW_MERGE, FLOW_MERGE.getRGB(), FLOW_MERGE.getDefaultLineStyle());
                    existingTypes.add(getType(output));
                    return main;
                })
                .collect(toList());
    }

    /**
     * Partition Mapper (Input) can't have Reject connection
     */
    @Override
    protected Optional<INodeConnector> createRejectConnector() {
        return Optional.empty();
    }

    /**
     * PartitionMapper (Input) component has 1 incoming and infinite outgoing
     * connections
     */
    @Override
    protected INodeConnector createIterateConnector() {
        final TaCoKitNodeConnector iterate = new TaCoKitNodeConnector(node, ITERATE);
        iterate.setMinLinkInput(0);
        iterate.setMaxLinkInput(1);
        iterate.setMinLinkOutput(0);
        iterate.setMaxLinkOutput(-1);
        existingTypes.add(ITERATE);
        return iterate;
    }

}
