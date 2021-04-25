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
import static org.talend.core.model.process.EConnectionType.REJECT;

import java.util.List;
import java.util.Optional;

import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.sdk.component.server.front.model.ComponentDetail;

/**
 * Creates connectors for Output component
 */
public class OutputConnectorCreator extends AbstractConnectorCreator {

    protected OutputConnectorCreator(final ComponentDetail detail, final INode node) {
        super(detail, node);
    }

    /**
     * Output component has no outgoing connectors
     */
    @Override
    protected List<INodeConnector> createMainConnectors() {
        return detail
                .getInputFlows()
                .stream()
                .filter(input -> FLOW_MAIN.equals(getType(input)))
                .map(input -> {
                    final TaCoKitNodeConnector main = TaCoKitNodeConnector.newFlow(node, input);
                    main.setMaxLinkInput(1);
                    main.setMaxLinkOutput(0);
                    main.setHasInput(true);
                    main.addConnectionProperty(FLOW_REF, FLOW_REF.getRGB(), FLOW_REF.getDefaultLineStyle());
                    main.addConnectionProperty(FLOW_MERGE, FLOW_MERGE.getRGB(), FLOW_MERGE.getDefaultLineStyle());
                    existingTypes.add(getType(input));
                    return main;
                })
                .collect(toList());
    }

    /**
     * Output component may have reject connector
     */
    @Override
    protected Optional<INodeConnector> createRejectConnector() {
        return detail
                .getOutputFlows()
                .stream() //
                .filter(output -> REJECT.equals(getType(output))) //
                .findFirst() //
                .map(output -> { //
                    final TaCoKitNodeConnector reject = TaCoKitNodeConnector.newReject(node, output);
                    reject.setMaxLinkInput(0);
                    reject.setMaxLinkOutput(1);
                    reject.setHasInput(false);
                    reject.addConnectionProperty(EConnectionType.FLOW_MAIN, new RGB(255, 0, 0), 2);
                    reject.getConnectionProperty(EConnectionType.FLOW_MAIN).setRGB(new RGB(255, 0, 0));
                    existingTypes.add(getType(output));
                    return reject;
                }); //
    }

    /**
     * Output component has no incoming iterate connection and has infinite outgoing
     * iterate connections
     */
    @Override
    protected INodeConnector createIterateConnector() {
        final TaCoKitNodeConnector iterate = new TaCoKitNodeConnector(node, ITERATE);
        iterate.setMinLinkInput(0);
        iterate.setMaxLinkInput(0);
        iterate.setMinLinkOutput(0);
        iterate.setMaxLinkOutput(-1);
        existingTypes.add(ITERATE);
        return iterate;
    }

}
