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

import static org.talend.core.model.process.EConnectionType.FLOW_MERGE;
import static org.talend.core.model.process.EConnectionType.FLOW_REF;
import static org.talend.core.model.process.EConnectionType.ITERATE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.sdk.component.server.front.model.ComponentDetail;

/**
 * Creates connectors for StandAlone component (Processor with no inputs or
 * output)
 */
class StandAloneConnectorCreator extends AbstractConnectorCreator {

    protected StandAloneConnectorCreator(final ComponentDetail detail, final INode node) {
        super(detail, node);
    }

    /**
     * Creates Main connector with 0 incoming and outgoing connections
     */
    @Override
    protected List<INodeConnector> createMainConnectors() {
        final TaCoKitNodeConnector main = TaCoKitNodeConnector.newFlow(node, TaCoKitNodeConnector.DEFAULT);
        main.setMaxLinkInput(0);
        main.setMaxLinkOutput(0);
        main.setHasInput(false);
        main.addConnectionProperty(FLOW_REF, FLOW_REF.getRGB(), FLOW_REF.getDefaultLineStyle());
        main.addConnectionProperty(FLOW_MERGE, FLOW_MERGE.getRGB(), FLOW_MERGE.getDefaultLineStyle());
        return Collections.singletonList(main);
    }

    /**
     * StandAlone component can't have Reject connector, thus returns
     * {@link Optional#empty()}
     */
    @Override
    protected Optional<INodeConnector> createRejectConnector() {
        return Optional.empty();
    }

    /**
     * StandAlone component has 1 incoming and infinite outgoing Iterate connections
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
