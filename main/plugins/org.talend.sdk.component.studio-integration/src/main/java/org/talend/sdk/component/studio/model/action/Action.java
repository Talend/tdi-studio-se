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
package org.talend.sdk.component.studio.model.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.commons.CommonsPlugin;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.designer.core.ui.editor.properties.controllers.uidialog.OpenContextChooseComboDialog;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

public class Action<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Action.class.getName());

    public static final String STATUS = "status";

    public static final String OK = "OK";

    public static final String KO = "KO";

    public static final String MESSAGE = "comment";

    private V1Action actionClient;

    private final String actionName;

    private final String family;

    private final String type;

    /**
     * Action parameters map. Key is an ElementParameter path. Value is a list of action parameters associated with the ElementParameter
     */
    private final Map<String, List<IActionParameter>> parameters = new HashMap<>();

    private IContextManager contextManager;

    public Action(final String actionName, final String family, final Type type) {
        this.actionName = actionName;
        this.family = family;
        this.type = type.toString();
    }

    /**
     * Adds specified {@code parameter} to this Action.
     * ActionParameter passed should be unique action parameter.
     *
     * @param parameter ActionParameter to be added
     */
    public void addParameter(final IActionParameter parameter) {
        Objects.requireNonNull(parameter, "parameter should not be null");
        final String elementParameter = parameter.getName();
        List<IActionParameter> list = parameters.computeIfAbsent(elementParameter, k -> new ArrayList<>());

        if (list.contains(parameter)) {
            throw new IllegalArgumentException("action already contains parameter " + parameter);
        }
        list.add(parameter);
    }

    @SuppressWarnings("unchecked")
    public Map<String, T> callback() {
        return actionClient().execute(Map.class, family, type, actionName, payload());
    }

    protected final String getActionName() {
        return this.actionName;
    }

    protected final String getFamily() {
        return this.family;
    }

    protected final String getType() {
        return this.type;
    }

    protected final Map<String, String> payload() {
        final Map<String, String> payload = new HashMap<>();
        IContext context = selectContext();
        parameters.values().stream()
                .flatMap(List::stream)
                .flatMap(actionParam -> actionParam.parameters().stream())
                .forEach(param -> {
                    final String initialValue = param.getSecond();
                    String value = Optional.ofNullable(context)
                            .map(cx -> Optional.ofNullable(
                                    cx.getContextParameter(initialValue.replace("context.", "")))
                                    .map(IContextParameter::getValue)
                                    .orElse(initialValue)
                            ).orElse(initialValue);
                    payload.put(param.getFirst(), value);
                });
        return payload;
    }

    public IContextManager getContextManager() {
        return contextManager;
    }

    public void setContextManager(final IContextManager contextManager) {
        this.contextManager = contextManager;
    }

    private IContext selectContext() {
        if (getContextManager() == null) {
            return null;
        }
        final List<IContext> allContexts = getContextManager().getListContext();
        if (allContexts.size() == 1) {
            return getContextManager().getDefaultContext();
        }
        final IContext[] selectedContext = { getContextManager().getDefaultContext() };
        try {
            if (!CommonsPlugin.isHeadless() && PlatformUI.isWorkbenchRunning()) {
                final Display display = Display.getDefault();
                if (display != null) {
                    display.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            Shell shell = null;
                            final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            if (activeWindow != null) {
                                shell = activeWindow.getShell();
                            } else {
                                shell = display.getActiveShell();
                            }
                            OpenContextChooseComboDialog dialog = new OpenContextChooseComboDialog(shell, allContexts);
                            dialog.create();
                            dialog.getShell().setMinimumSize(200, 200);
                            if (Window.OK == dialog.open()) {
                                selectedContext[0] = dialog.getSelectedContext();
                            }
                        }
                    });
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error while selecting context: " + e.getMessage());
        }

        return selectedContext[0];
    }

    public enum Type {
        HEALTHCHECK,
        SUGGESTIONS,
        VALIDATION,
        UPDATE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    protected V1Action actionClient() {
        if (actionClient == null) {
            actionClient = Lookups.client().v1().action();
        }
        return actionClient;
    }

}
