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
package org.talend.sdk.component.studio.ui.guessschema;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataProcess;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.enums.ETaCoKitComponentType;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.ComponentModelSpy;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitGuessSchemaProcess {

    private final Task guessSchemaTask;

    private final ExecutorService executorService = ExecutorService.class.cast(Lookups.uiActionsThreadPool()
            .getExecutor());

    public TaCoKitGuessSchemaProcess(final Property property, final INode node, final IContext context,
            final String discoverSchemaAction, final String connectionName) {
        this.guessSchemaTask = new Task(property, context, node, discoverSchemaAction, connectionName, executorService);
    }

    public Future<GuessSchemaResult> run() {
        return executorService.submit(guessSchemaTask);
    }

    public void kill() {
        guessSchemaTask.kill();
    }

    public static class Task implements Callable<GuessSchemaResult> {

        private final Property property;

        private IProcess process;

        private final IContext context;

        private INode node;

        private final String actionName;

        private final String connectionName;

        private final ExecutorService executorService;

        private java.lang.Process executeProcess;

        public Task(final Property property, final IContext context, final INode node, final String actionName,
                final String connectionName, final ExecutorService executorService) {
            this.property = property;
            this.context = context;
            this.node = node;
            this.actionName = actionName;
            this.connectionName = connectionName;
            this.executorService = executorService;
        }

        @Override
        public GuessSchemaResult call() throws Exception {
            buildProcess();
            IProcessor processor = ProcessorUtilities.getProcessor(process, null);
            processor.setContext(context);
            final String debug = System.getProperty("org.talend.tacokit.guessschema.debug", null);
            executeProcess = processor.run(debug == null || debug.isEmpty() ? null :
                            singletonList(debug).toArray(new String[0]),
                    IProcessor.NO_STATISTICS,
                    IProcessor.NO_TRACES);

            final Future<String> result = executorService.submit(() -> {
                try (
                        final BufferedReader reader = new BufferedReader(
                                new InputStreamReader(executeProcess.getInputStream()))) {
                    return reader.lines()
                            .filter(l -> l.startsWith("[") || l.startsWith("{")) // ignore line with non json data
                            .collect(joining("\n"));
                }
            });

            // read error stream
            final Future<String> error = executorService.submit(() -> {
                try (
                        final BufferedReader reader = new BufferedReader(
                                new InputStreamReader(executeProcess.getErrorStream()))) {
                    return reader.lines().collect(joining("\n"));
                }
            });
            executeProcess.waitFor();
            final String resultStr = result.get();
            if (resultStr != null && !resultStr.trim().isEmpty()) {
                return new GuessSchemaResult(resultStr, error.get());
            }
            final String errMessage = error.get();
            if (errMessage != null && !errMessage.isEmpty()) {
                throw new IllegalStateException(errMessage);
            } else {
                throw new IllegalStateException(Messages.getString("guessSchema.error.empty")); //$NON-NLS-1$
            }
        }

        public synchronized void kill() {
            if (executeProcess != null && executeProcess.isAlive()) {
                final java.lang.Process p = executeProcess.destroyForcibly();
                try {
                    p.waitFor(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void buildProcess() {
            IProcess originalProcess;
            originalProcess = new Process(property);

            List<? extends IConnection> incomingConnections = new ArrayList<>(node.getIncomingConnections());
            List<? extends IConnection> outgoingConnections = new ArrayList<>(node.getOutgoingConnections());
            try {
                node.setOutgoingConnections(new ArrayList<>());

                List<INode> nodes = new ArrayList<>();
                IComponent nodeComp = node.getComponent();

                /**
                 * If it is an input component and guess schema action is provided, calling the action is enough
                 */
                if (ComponentModel.class.isInstance(nodeComp)) {
                    ComponentModel compModel = (ComponentModel) nodeComp;
                    if (ETaCoKitComponentType.input.equals(compModel.getTaCoKitComponentType())) {
                        final List<ActionReference> actions = compModel.getDiscoverSchemaActions();
                        if (actions != null && !actions.isEmpty() && actionName != null
                                && actions.stream().anyMatch(a -> a.getName().equals(actionName))) {
                            node.setIncomingConnections(new ArrayList<>());
                            nodes.add(node);
                        }
                        if (TaCoKitUtil.isUseExistConnection(node)) {
                            updateDatastoreParameterFromConnection(node);
                        }
                    }
                }
                /**
                 * Else, still need to build the sub job
                 */
                if (nodes.isEmpty()) {
                    retrieveNodes(nodes, new HashSet<>(), node);
                }

                DataProcess dataProcess = new DataProcess(originalProcess);
                dataProcess.buildFromGraphicalProcess(nodes);
                process = dataProcess.getDuplicatedProcess();
                process.getContextManager()
                        .getListContext()
                        .addAll(originalProcess.getContextManager().getListContext());
                process.getContextManager().setDefaultContext(this.context);
                List<INode> nodeList = dataProcess.getNodeList();
                INode newNode = null;
                // INode newNode = dataProcess.buildNodeFromNode(node, process);
                for (INode curNode : nodeList) {
                    if (curNode.getUniqueName().equals(node.getUniqueName())) {
                        newNode = curNode;
                        break;
                    }
                }

                IComponent component = newNode.getComponent();
                ComponentModelSpy componentSpy = createComponnetModelSpy(component);
                newNode.setComponent(componentSpy);
                if (ComponentModel.class.isInstance(component)) {
                    List<IElementParameter> elementParameters =
                            (List<IElementParameter>) newNode.getElementParameters();
                    final ComponentModel cm = ComponentModel.class.cast(component);
                    final IElementParameter pluginName = new ElementParameter(newNode);
                    pluginName.setName(TaCoKitConst.GUESS_SCHEMA_PARAMETER_PLUGIN_NAME);
                    pluginName.setValue(cm.getPluginName());
                    elementParameters.add(pluginName);

                    final IElementParameter actionNameParam = new ElementParameter(newNode);
                    actionNameParam.setName(TaCoKitConst.GUESS_SCHEMA_PARAMETER_ACTION_NAME);
                    final List<ActionReference> actions = cm.getDiscoverSchemaActions();
                    if (actionName != null && !actions.isEmpty() && actions.stream()
                            .anyMatch(a -> a.getName().equals(actionName))) {
                        actionNameParam.setValue(actions.stream()
                                .filter(a -> a.getName().equals(actionName)).findFirst().get().getName());

                    }
                    elementParameters.add(actionNameParam);

                    final IElementParameter tacokitComponentType = new ElementParameter(newNode);
                    tacokitComponentType.setName(TaCoKitConst.GUESS_SCHEMA_PARAMETER_TACOKIT_COMPONENT_TYPE);
                    tacokitComponentType.setValue(cm.getTaCoKitComponentType().toString());
                    elementParameters.add(tacokitComponentType);

                    final IElementParameter outputConnectionName = new ElementParameter(newNode);
                    outputConnectionName.setName(TaCoKitConst.GUESS_SCHEMA_PARAMETER_OUTPUT_CONNECTION_NAME);
                    outputConnectionName.setValue(connectionName);
                    elementParameters.add(outputConnectionName);
                }

            } finally {
                node.setIncomingConnections(incomingConnections);
                node.setOutgoingConnections(outgoingConnections);
            }
        }

        private void retrieveNodes(final List<INode> nodeList, final Set<INode> recordedNodes,
                final INode currentNode) {
            if (currentNode == null || recordedNodes.contains(currentNode)) {
                return;
            }
            nodeList.add(currentNode);
            recordedNodes.add(currentNode);
            List<? extends IConnection> incomingConnections = currentNode.getIncomingConnections();
            if (incomingConnections != null && !incomingConnections.isEmpty()) {
                for (IConnection conn : incomingConnections) {
                    if (conn != null) {
                        retrieveNodes(nodeList, recordedNodes, conn.getSource());
                    }
                }
            }
        }

        private ComponentModelSpy createComponnetModelSpy(final IComponent component) {
            ComponentModelSpy componentSpy = new ComponentModelSpy(component);
            IComponent guessComponent = Lookups.taCoKitCache().getTaCoKitGuessSchemaComponent();
            componentSpy.spyName(guessComponent.getName());
            componentSpy.spyOriginalName(guessComponent.getOriginalName());
            componentSpy.spyShortName(guessComponent.getShortName());
            componentSpy.spyTemplateFolder(guessComponent.getTemplateFolder());
            componentSpy.spyTemplateNamePrefix(guessComponent.getTemplateNamePrefix());
            componentSpy.spyAvailableCodeParts(guessComponent.getAvailableCodeParts());
            return componentSpy;
        }

        private void updateDatastoreParameterFromConnection(INode node) {
            for (IElementParameter parameter : node.getElementParameters()) {
                if (TaCoKitUtil.isDataStoreParameter(node, parameter.getName())) {
                    parameter.setValue(TaCoKitUtil.getParameterValueFromConnection(node, parameter.getName()));
                }
            }
        }
    }

    public static class GuessSchemaResult {

        private String result;

        private String error;

        public GuessSchemaResult(String result, String error) {
            super();
            this.result = result;
            this.error = error;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

    }
}
