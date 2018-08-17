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

import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

/**
 * Listens to {@link TaCoKitElementParameter} value change and updates {@link Action} parameters with new value
 */
public class ActionParametersUpdater implements PropertyChangeListener {
    
    private final Action action;
    
    public ActionParametersUpdater(final Action action) {
        this.action = action;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        action.setParameterValue(TaCoKitElementParameter.class.cast(event.getSource()).getName(), (String) event.getNewValue());
    }
    
    public Action getAction() {
        return this.action;
    }

}
