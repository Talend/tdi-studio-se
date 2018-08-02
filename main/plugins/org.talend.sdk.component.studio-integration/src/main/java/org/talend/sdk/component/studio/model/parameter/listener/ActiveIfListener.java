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
import org.talend.sdk.component.studio.model.parameter.TableElementParameter;
import org.talend.sdk.component.studio.model.parameter.TextElementParameter;

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
            final TaCoKitElementParameter sourceParam,
            final Map<String, TaCoKitElementParameter> targetParams) {
        this.conditions = conditions;
        this.sourceParameter = sourceParam;
        this.targetParams = targetParams;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if(!"value".equals(event.getPropertyName())){
            return;
        }
        final boolean show = conditions.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .allMatch(condition -> {
                    final boolean negate = condition.isNegation();
                    return negate != Arrays.stream(condition.getValues())
                            .anyMatch(conditionValue -> evaluate(condition, conditionValue));

                });
        sourceParameter.setShow(show);
        sourceParameter.redraw();//request source parameter redraw
        sourceParameter.firePropertyChange("show", null, show);
    }

    private boolean evaluate(final PropertyDefinitionDecorator.Condition condition, final String value) {
        final String evaluationStrategy = condition.getEvaluationStrategy();
        String targetValue = null;
        switch (evaluationStrategy) {
        case "DEFAULT":
            targetValue = String.valueOf(targetParams.get(condition.getTargetPath()).getValue());
        case "LENGTH":
            final TaCoKitElementParameter targetParam = targetParams.get(condition.getTargetPath());
            if (targetParam.getValue() == null) {
                return "0".equals(value);
            }
            if (TextElementParameter.class.isInstance(targetParam)) {
                targetValue = String.valueOf(String.valueOf(targetParam.getValue()).length());
            } else if (TableElementParameter.class.isInstance(targetParam)) {
                targetValue = String.valueOf(((List) (targetParam.getValue())).size());
            }
            break;
        default:
            throw new IllegalArgumentException("Not supported operation '" + evaluationStrategy + "'");
        }
        return value.equals(targetValue);
    }
}
