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

import static org.talend.sdk.component.studio.model.action.Action.HEALTH_CHECK;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.SettingsActionParameter;
import org.talend.sdk.component.studio.model.parameter.command.AsyncAction;

/**
 * Binds together things required for HealthCheck callback
 */
public class HealthCheckResolver {

    private final IElement element;

    private final String family;

    private final PropertyNode node;

    private final ActionReference action;

    private final EComponentCategory category;

    private final int rowNumber;

    public HealthCheckResolver(final IElement element, final String family, final PropertyNode node, final ActionReference action,
            final EComponentCategory category, final int rowNumber) {
        this.element = element;
        this.family = family;
        this.node = node;
        this.action = action;
        this.category = category;
        this.rowNumber = rowNumber;
    }

    public void resolveParameters(final Map<String, IElementParameter> settings) {
        final ButtonParameter button = new ButtonParameter(element);
        button.setCategory(category);
        button.setDisplayName(Messages.getString("healthCheck.button"));
        button.setName(node.getProperty().getPath() + ".testConnection");
        button.setNumRow(rowNumber);
        button.setShow(true);
        final String basePath = node.getProperty().getPath();
        final String alias = getParameterAlias();
        final PathCollector collector = new PathCollector();
        node.accept(collector);
        final AsyncAction command = new AsyncAction(new Action(node.getProperty().getHealthCheckName(), family, HEALTH_CHECK));
        collector.getPaths().stream().map(settings::get).filter(Objects::nonNull).map(p -> (TaCoKitElementParameter) p)
                .forEach(p -> {
                    final String parameter = p.getName().replace(basePath, alias);
                    final SettingsActionParameter actionParameter = new SettingsActionParameter(p, parameter);
                    command.addParameter(actionParameter);
                });
        button.setCommand(command);
        settings.put(button.getName(), button);
    }

    /**
     * Finds parameter alias (which is value of Option annotation in HealthCheck method)
     * 
     * This method builds property tree and assumes that root node path is a required alias
     *
     * 
     * 
     * @return parameter alias
     */
    private String getParameterAlias() {
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator.wrap(action.getProperties());
        final PropertyNode root = new PropertyTreeCreator(new WidgetTypeMapper()).createPropertyTree(properties);
        return root.getProperty().getPath();
    }

}
