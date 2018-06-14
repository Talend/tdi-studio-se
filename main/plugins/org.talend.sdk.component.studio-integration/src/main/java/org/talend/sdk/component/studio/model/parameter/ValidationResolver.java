/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.model.parameter;

import static org.talend.sdk.component.studio.model.action.Action.VALIDATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.action.ActionParameter;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;

class ValidationResolver {

    private final ActionReference action;

    private final PropertyNode actionOwner;

    private final ValidationListener listener;

    private final ElementParameter redrawParameter;

    ValidationResolver(final PropertyNode actionOwner, final Collection<ActionReference> actions,
            final ValidationListener listener, final ElementParameter redrawParameter) {
        // TODO remove these checks as it is package visible
        Objects.requireNonNull(actionOwner, "actionOwner should not be null");
        Objects.requireNonNull(actions, "actions should not be null");
        Objects.requireNonNull(listener, "listener should not be null");
        Objects.requireNonNull(redrawParameter, "redrawParameter should not be null");
        if (!actionOwner.getProperty().hasValidation()) {
            throw new IllegalArgumentException("property has no validation");
        }
        this.actionOwner = actionOwner;
        this.listener = listener;
        this.redrawParameter = redrawParameter;
        final String actionName = actionOwner.getProperty().getValidationName();
        action = actions
                .stream()
                .filter(a -> VALIDATION.equals(a.getType()))
                .filter(a -> a.getName().equals(actionName))
                .findFirst()
                .get();
    }

    void resolveParameters(final Map<String, IElementParameter> settings) {
        final List<SimplePropertyDefinition> callbackParameters = new ArrayList<>(action.getProperties());
        final List<String> relativePaths = actionOwner.getProperty().getValidationParameters();

        for (int i = 0; i < relativePaths.size(); i++) {
            final TaCoKitElementParameter parameter = resolveParameter(relativePaths.get(i), settings);
            parameter.registerListener(parameter.getName(), listener);
            parameter.setRedrawParameter(redrawParameter);
            final String callbackParameter = callbackParameters.get(i).getName();
            final String initialValue = callbackParameters.get(i).getDefaultValue();
            listener.addParameter(new ActionParameter(parameter.getName(), callbackParameter, initialValue));
        }

    }

    TaCoKitElementParameter resolveParameter(final String relativePath, final Map<String, IElementParameter> settings) {
        String path = null;
        // workaround for "." relative path. "./prop" will be handled in else branch
        if (PathResolver.CURRENT.equals(relativePath)) {
            path = actionOwner.getProperty().getPath();
        } else {
            path = PathResolver.resolve(actionOwner, relativePath);
        }
        return (TaCoKitElementParameter) settings.get(path);
    }
}

// TODO find place for this class
class PathResolver {

    /**
     * Path character which denotes parent element
     */
    public static final String PARENT = "..";

    /**
     * Path character which denotes current element
     */
    public static final String CURRENT = ".";

    /**
     * Path separator which is used in Property tree
     */
    public static final String DOT_PATH_SEPARATOR = ".";

    /**
     * Path separator
     */
    public static final String PATH_SEPARATOR = "/";

    static String resolve(final PropertyNode baseNode, final String relativePath) {
        final String basePath = baseNode.getParentId();
        final LinkedList<String> path = new LinkedList<>();
        path.addAll(Arrays.asList(basePath.split("\\" + DOT_PATH_SEPARATOR)));
        final List<String> parts = Arrays.asList(relativePath.split(PATH_SEPARATOR));
        for (final String p : parts) {
            if (CURRENT.equals(p)) {
                continue;
            }
            if (PARENT.equals(p)) {
                path.removeLast();
            } else {
                path.addLast(p);
            }
        }
        return path.stream().collect(Collectors.joining(DOT_PATH_SEPARATOR));
    }

}
