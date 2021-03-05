/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

public class ValidationListener extends Action<String> implements PropertyChangeListener {

    private final ValidationLabel label;

    public ValidationListener(final ValidationLabel label, final String family, final String actionName) {
        super(actionName, family, Action.Type.VALIDATION);
        this.label = label;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if(!"value".equals(event.getPropertyName())){
            return;
        }
        if(event.getSource() instanceof TaCoKitElementParameter 
        		&& !((TaCoKitElementParameter)event.getSource()).isShow(Collections.emptyList())) {
            label.hideValidation();
            return;
        }
        CompletableFuture.supplyAsync(this::validate, Lookups.uiActionsThreadPool().getExecutor()).thenAccept(
                this::notify);
    }

    private Map<String, String> validate() {
        return callback();
    }

    private void notify(final Map<String, String> validation) {
        if (OK.equals(validation.get(STATUS))) {
            label.hideValidation();
        } else {
            label.showValidation(validation.get(MESSAGE));
        }

    }

}
