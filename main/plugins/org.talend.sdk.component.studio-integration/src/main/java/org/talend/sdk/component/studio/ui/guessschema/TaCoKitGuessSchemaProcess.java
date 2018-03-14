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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.talend.core.CorePlugin;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
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

    private final ExecutorService executorService = ExecutorService.class.cast(Lookups.uiActionsThreadPool()
            .getExecutor());

    public TaCoKitGuessSchemaProcess(final Property property, final INode node, final IContext context,
            final String discoverSchemaAction) {
        this.node = node;
        this.context = context;
        this.discoverSchemaAction = discoverSchemaAction;
        this.property = property;
    }

    public Future<String> run() {
        return executorService.submit(new Task(property, context, node, discoverSchemaAction));
    }

    public static class Task implements Callable<String> {

        private final Process process;

        private final IContext context;

        private INode node;

        private final String actionName;

        public Task(final Property property, final IContext context, final INode node, final String actionName) {
            this.process = new Process(property);
            this.context = context;
            this.node = node;
            this.actionName = actionName;
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
                        throw new IllegalStateException(reader.lines().collect(joining("\n")));
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
            process.getContextManager().getListContext().addAll(node.getProcess().getContextManager().getListContext());
            process.getContextManager().setDefaultContext(this.context);
            List<? extends IConnection> outgoingConnections = new ArrayList<>(node.getOutgoingConnections());
            try {
                node.setOutgoingConnections(new ArrayList<>());
                DataProcess dataProcess = new DataProcess(process);
                INode newNode = dataProcess.buildNodeFromNode(node, process);

                IComponent component = newNode.getComponent();
                ComponentModelSpy componentSpy = createComponnetModelSpy(component);
                newNode.setComponent(componentSpy);
                this.node = newNode;
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
                }

            } finally {
                node.setOutgoingConnections(outgoingConnections);
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
