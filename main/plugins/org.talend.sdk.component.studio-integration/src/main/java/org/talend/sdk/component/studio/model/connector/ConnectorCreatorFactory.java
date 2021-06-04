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
package org.talend.sdk.component.studio.model.connector;

import static org.talend.core.model.process.EConnectionType.FLOW_MAIN;
import static org.talend.sdk.component.studio.model.connector.AbstractConnectorCreator.getType;

import java.util.Objects;

import org.talend.core.model.process.INode;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.VirtualComponentModel;

/**
 * Creates appropriate {@link ConnectorCreator} according component meta
 */
public final class ConnectorCreatorFactory {

    private ConnectorCreatorFactory() {
        new AssertionError();
    }

    public static ConnectorCreator create(final ComponentModel componentModel, final ComponentDetail component, final INode node) {
        if (componentModel instanceof VirtualComponentModel ) {
            return new VirtualComponentConnectorCreator(component, node);
        }
        if (isStandAlone(component)) {
            return new StandAloneConnectorCreator(component, node);
        }
        if (isInput(component)) {
            return new PartitionMapperConnectorCreator(component, node);
        }
        if (isOutput(component)) {
            return new OutputConnectorCreator(component, node);
        }
        if (isProcessor(component)) {
            return new ProcessorConnectorCreator(component, node);
        }
        throw new AssertionError();
    }

    /**
     * Checks whether the component is StandAlone component.
     * StandAlone component is a component, which has no input and output connections.
     *
     * @param detail ComponentDetail - a description of a component
     * @return true, it the component is StandAlone
     */
    public static boolean isStandAlone(final ComponentDetail detail) {
        Objects.requireNonNull(detail);
        return !hasInputs(detail) && !hasOutputs(detail);
    }

    /**
     * Checks whether the component is an Input component.
     * Input component is a component, which has only input connections.
     *
     * @param detail ComponentDetail - a description of a component
     * @return true, it the component is an Input
     */
    public static boolean isInput(final ComponentDetail detail) {
        Objects.requireNonNull(detail);
        return !hasInputs(detail) && hasOutputs(detail);
    }

    /**
     * Checks whether the component is an Output component.
     * Output component is a component, which has only output connections.
     *
     * @param detail ComponentDetail - a description of a component
     * @return true, it the component is an Output
     */
    public static boolean isOutput(final ComponentDetail detail) {
        Objects.requireNonNull(detail);
        return hasInputs(detail) && !hasOutputs(detail);
    }

    /**
     * Checks whether the component is a Processor component.
     * Processor component is a component, which has both input and output connections.
     *
     * @param detail ComponentDetail - a description of a component
     * @return true, it the component is a Processor
     */
    public static boolean isProcessor(final ComponentDetail detail) {
        Objects.requireNonNull(detail);
        return hasInputs(detail) && hasOutputs(detail);
    }

    private static boolean hasInputs(final ComponentDetail component) {
        return //
        component.getInputFlows().stream().anyMatch(input -> FLOW_MAIN.equals(getType(input)));
    }

    private static boolean hasOutputs(final ComponentDetail component) {
        return //
        component.getOutputFlows().stream().anyMatch(output -> FLOW_MAIN.equals(getType(output)));
    }

}
