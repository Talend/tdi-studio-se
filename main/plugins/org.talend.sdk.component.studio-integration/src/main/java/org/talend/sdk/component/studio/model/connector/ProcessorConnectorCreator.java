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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.sdk.component.server.front.model.ComponentDetail;

/**
 * Creates connectors for Processor component
 */
class ProcessorConnectorCreator extends AbstractConnectorCreator {

    protected ProcessorConnectorCreator(final ComponentDetail detail, final INode node) {
        super(detail, node);
    }

    /**
     * Processor component may have both incoming and outgoing connections
     */
    @Override
    protected List<INodeConnector> createMainConnectors() {
        AtomicInteger rowCount = new AtomicInteger(0);
        AtomicBoolean isDefaultConnectionPresent = new AtomicBoolean(false);
        detail.getInputFlows().forEach(input -> {
            if (FLOW_MAIN.equals(getType(input))) {
                rowCount.incrementAndGet();
                if (FLOW_MAIN.getName().equals(getName(input))) {
                    isDefaultConnectionPresent.set(true);
                }
            }
        });
        final String mainConnectorName = MAIN_CONNECTOR_NAME;
        final TaCoKitNodeConnector main = TaCoKitNodeConnector.newFlow(node, mainConnectorName);
        main.setMaxLinkInput(rowCount.get());
        main.setMaxLinkOutput(0);
        main.setHasInput(isDefaultConnectionPresent.get());
        main.addConnectionProperty(FLOW_MAIN, FLOW_MAIN.getRGB(), FLOW_MAIN.getDefaultLineStyle());
        main.addConnectionProperty(FLOW_REF, FLOW_MAIN.getRGB(), FLOW_MAIN.getDefaultLineStyle());
        main.addConnectionProperty(FLOW_MERGE, FLOW_MAIN.getRGB(), FLOW_MAIN.getDefaultLineStyle());
        existingTypes.add(EConnectionType.FLOW_MAIN);

        List<INodeConnector> connectors = new ArrayList<>();
        connectors.add(main);
        final Map<String, TaCoKitNodeConnector> connectorMap = new HashMap<>();
        connectorMap.put(main.getName(), main);

        List<INodeConnector> generatedConnectors = Stream
                .concat(detail
                        .getInputFlows()
                        .stream() //
                        .filter(input -> {
                            if (!FLOW_MAIN.equals(getType(input))) {
                                return false;
                            }
                            return !mainConnectorName.equalsIgnoreCase(getName(input));
                        }) //
                        .map(input -> { //
                            final TaCoKitNodeConnector inputConnector = TaCoKitNodeConnector.newFlow(node, input);
                            inputConnector.setMaxLinkInput(1);
                            inputConnector.setMaxLinkOutput(0);
                            inputConnector.setHasInput(true);
                            inputConnector.addConnectionProperty(FLOW_MAIN, FLOW_MAIN.getRGB(),
                                    FLOW_MAIN.getDefaultLineStyle());
                            inputConnector.addConnectionProperty(FLOW_REF, FLOW_MAIN.getRGB(),
                                    FLOW_MAIN.getDefaultLineStyle());
                            inputConnector.addConnectionProperty(FLOW_MERGE, FLOW_MAIN.getRGB(),
                                    FLOW_MAIN.getDefaultLineStyle());
                            existingTypes.add(getType(input));
                            connectorMap.put(inputConnector.getName(), inputConnector);
                            return inputConnector;
                        }), detail
                        .getOutputFlows()
                        .stream() //
                        .filter(output -> FLOW_MAIN.equals(getType(output))) //
                        .map(output -> { //
                            TaCoKitNodeConnector outputConnector = null;
                            try {
                                outputConnector = TaCoKitNodeConnector.newFlow(node, output);
                                TaCoKitNodeConnector existingConnector = connectorMap.get(outputConnector.getName());
                                if (existingConnector != null) {
                                    outputConnector = existingConnector;
                                    int maxOutput = outputConnector.getMaxLinkOutput();
                                    if (maxOutput < 0) {
                                        maxOutput = 0;
                                    }
                                    outputConnector.setMaxLinkOutput(maxOutput + 1);
                                    return null;
                                } else {
                                    outputConnector.setMaxLinkInput(0);
                                    outputConnector.setMaxLinkOutput(1);
                                    outputConnector.addConnectionProperty(FLOW_REF, FLOW_REF.getRGB(),
                                            FLOW_REF.getDefaultLineStyle());
                                    outputConnector.addConnectionProperty(FLOW_MERGE, FLOW_MERGE.getRGB(),
                                            FLOW_MERGE.getDefaultLineStyle());
                                    existingTypes.add(getType(output));
                                    connectorMap.put(outputConnector.getName(), outputConnector);
                                }
                                return outputConnector;
                            } finally {
                                if (outputConnector != null) {
                                    outputConnector.setHasOutput(true);
                                }
                            }
                        }))
                .filter(Objects::nonNull)
                .collect(toList());

        connectors.addAll(generatedConnectors);
        return connectors;
    }

    /**
     * Processor component may have outgoing Reject connections
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
                    reject.setHasOutput(true);
                    reject.addConnectionProperty(EConnectionType.FLOW_MAIN, new RGB(255, 0, 0), 2);
                    reject.getConnectionProperty(EConnectionType.FLOW_MAIN).setRGB(new RGB(255, 0, 0));
                    existingTypes.add(getType(output));
                    return reject;
                }); //
    }

    /**
     * Processor component has 0 incoming and infinite outgoing iterate connections
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
