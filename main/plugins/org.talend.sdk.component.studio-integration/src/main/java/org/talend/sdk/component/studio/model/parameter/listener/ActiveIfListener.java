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
package org.talend.sdk.component.studio.model.parameter.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

/**
 * {@link PropertyChangeListener}, which activates/deactivates {@link IElementParameter} according target
 * {@link IElementParameter} new value
 * If new value is the same as one of specified <code>values</code>, then {@link ActiveIfListener} activates (shows)
 * {@link IElementParameter}.
 * Else it deactivates {@link IElementParameter}
 */
public class ActiveIfListener implements PropertyChangeListener {

    private final Map<Integer, List<PropertyDefinitionDecorator.Condition>> conditions;

    private final Map<String, TaCoKitElementParameter> targetParams;

    private final TaCoKitElementParameter sourceParameter;

    public ActiveIfListener(
            final Map<Integer, List<PropertyDefinitionDecorator.Condition>> conditions,
            final TaCoKitElementParameter parameter,
            final Map<String, TaCoKitElementParameter> targetParams) {
        this.conditions = conditions;
        this.sourceParameter = parameter;
        this.targetParams = targetParams;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final boolean show = conditions.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .allMatch(condition -> Arrays.stream(condition.getValues())
                        .anyMatch(conditionValue -> condition.getTargetPath().equals(event.getPropertyName()) ?
                                conditionValue.equals(String.valueOf(event.getNewValue())) :
                                conditionValue.equals(targetParams.get(condition.getTargetPath()).getValue())));
        sourceParameter.setShow(show);
        sourceParameter.redraw();//request source parameter redraw
    }
}
