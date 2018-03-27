/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.talend.core.CorePlugin;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataProcess;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.model.ComponentModelSpy;
import org.talend.sdk.component.studio.util.TaCoKitConst;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitGuessSchemaProcess {

    protected static final String DEFAULT_JOB_NAME = "Mock_job_for_Guess_schema"; //$NON-NLS-1$

    protected static final int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    private INode node;

    private IContext context;

    private final String discoverSchemaAction;

    private final Property property;

    private final String connectionName;

    private final ExecutorService executorService = ExecutorService.class.cast(Lookups.uiActionsThreadPool()
            .getExecutor());

    public TaCoKitGuessSchemaProcess(final Property property, final INode node, final IContext context,
            final String discoverSchemaAction, final String connectionName) {
        this.node = node;
        this.context = context;
        this.discoverSchemaAction = discoverSchemaAction;
        this.property = property;
        this.connectionName = connectionName;
    }

    public Future<String> run() {
        return executorService.submit(new Task(property, context, node, discoverSchemaAction, connectionName));
    }

    public static class Task implements Callable<String> {

        private final Property property;

        private IProcess process;

        private final IContext context;

        private INode node;

        private final String actionName;

        private final String connectionName;

        public Task(final Property property, final IContext context, final INode node, final String actionName,
                final String connectionName) {
            this.property = property;
            this.context = context;
            this.node = node;
            this.actionName = actionName;
            this.connectionName = connectionName;
        }

        @Override
        public String call() throws Exception {
            buildProcess();
            IProcessor processor = ProcessorUtilities.getProcessor(process, null);
            processor.setContext(context);
            java.lang.Process executeProcess = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
            try {
                final int exitCode = executeProcess.waitFor(); //wait check error stream
                if (exitCode != 0) {
                    try (
                            final BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(executeProcess.getErrorStream()));
                    ) {
                        throw new IllegalStateException(reader.lines().collect(joining("\r\n")));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }

            try (
                    final BufferedReader reader = new BufferedReader(
                            new InputStreamReader(executeProcess.getInputStream()));
            ) {
                return reader.lines().collect(joining("\n"));

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private void buildProcess() {
            IProcess originalProcess = null;

            boolean useMokeJob = true;
            if (useMokeJob) {
                /**
                 * use mocked process to run guess schema job in .Java maven project
                 */
                originalProcess = new Process(property);
                // seems mock job has problems with log
            } else {
                /**
                 * if we use node.getProcess(), guess schema job will be run in the maven project of the job, but seems
                 * still have problem for guessing schema of input
                 */
                originalProcess = node.getProcess();
            }

            List<? extends IConnection> outgoingConnections = new ArrayList<>(node.getOutgoingConnections());
            try {
                node.setOutgoingConnections(new ArrayList<>());

                List<INode> nodes = new ArrayList<>();
                retrieveNodes(nodes, new HashSet<>(), node);

                DataProcess dataProcess = new DataProcess(originalProcess);
                dataProcess.buildFromGraphicalProcess(nodes);
                process = dataProcess.getDuplicatedProcess();
                if (useMokeJob) {
                    process.getContextManager().getListContext().addAll(originalProcess.getContextManager().getListContext());
                    process.getContextManager().setDefaultContext(this.context);
                }
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
                node.setOutgoingConnections(outgoingConnections);
            }
        }

        private void retrieveNodes(final List<INode> nodeList, final Set<INode> recordedNodes, final INode currentNode) {
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
    }

}
