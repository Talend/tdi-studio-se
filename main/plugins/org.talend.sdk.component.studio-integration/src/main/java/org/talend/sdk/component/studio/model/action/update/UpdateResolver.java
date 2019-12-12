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
package org.talend.sdk.component.studio.model.action.update;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;
import org.talend.sdk.component.studio.model.parameter.PathCollector;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.resolver.AbstractParameterResolver;

public class UpdateResolver extends AbstractParameterResolver {

    private final ButtonParameter button;

    public UpdateResolver(final IElement element, final EComponentCategory category, final int rowNumber,
                          final UpdateAction action, final PropertyNode actionOwner, final Collection<ActionReference> actions,
                          final ElementParameter redrawParameter, final Map<String, IElementParameter> settings,
                          final BooleanSupplier isShown) {
        super(action, actionOwner,
                getActionRef(actions,
                        actionOwner.getProperty().getUpdatable().orElseThrow(IllegalStateException::new).getActionName(),
                        Action.Type.UPDATE));

        this.button = new ButtonParameter(element) {
            @Override
            public boolean isShow(final List<? extends IElementParameter> listParam) {
                return isShown.getAsBoolean();
            }

            @Override
            public boolean isShow(final String conditionShowIf, final String conditionNotShowIf,
                                  final List<? extends IElementParameter> listParam) {
                return isShown.getAsBoolean();
            }
        };
        button.setCategory(category);
        button.setDisplayName(actionRef.getDisplayName());
        button.setName(actionOwner.getProperty().getPath() + PropertyNode.UPDATE_BUTTON);
        button.setNumRow(rowNumber);
        button.setRedrawParameter(redrawParameter);
        settings.put(button.getName(), button);
    }

    public ButtonParameter getButton() {
        return button;
    }

    @Override
    public void resolveParameters(final Map<String, IElementParameter> settings) {
        super.resolveParameters(settings);

        final UpdateAction action = (UpdateAction) getAction();
        final Map<String, TaCoKitElementParameter> parameters = actionOwner.accept(new PathCollector()).getPaths().stream()
                .map(settings::get)
                .filter(Objects::nonNull)
                .map(TaCoKitElementParameter.class::cast)
                .collect(Collectors.toMap(TaCoKitElementParameter::getName, Function.identity()));
        button.setCommand(new UpdateCommand(action, actionOwner.getId(), parameters, button));
   }
}
