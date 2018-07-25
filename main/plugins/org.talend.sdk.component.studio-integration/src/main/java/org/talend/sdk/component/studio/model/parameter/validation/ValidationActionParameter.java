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
package org.talend.sdk.component.studio.model.parameter.validation;

import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.sdk.component.studio.model.action.ActionParameter;

/**
 * ActionParameter with isSet state, which is used to understand whether ActionParameters are ready (are set) for the call
 */
class ValidationActionParameter extends ActionParameter {

    /**
     * Denotes whether associated ElementParameter's value is set for the first time. Once it was set it can't be
     * unset
     */
    private boolean isSet = false;
    
    /**
     * Creates ValidationActionParameter
     * 
     * @param parameter action method parameter name/path
     */
    ValidationActionParameter(final String parameter) {
        this(parameter, null);
    }
    
    /**
     * Creates ValidationActionParameter
     * 
     * @param parameter action method parameter name/path
     * @param value action method parameter initial value
     */
    ValidationActionParameter(final String parameter, final String value) {
        super(parameter, value);
    } 
    
    /**
     * Checks whether associated ElementParameter's value was set and Action is ready for use
     * 
     * @return true, if value was set
     */
    boolean isSet() {
        return isSet;
    }
    
    /**
     * Sets ActionParameter new value. If value is set for the first time, method
     * changes {@link #isSet} state to {@code true}
     * 
     * @param newValue new value of ActionParameter to be set
     */
    @Override
    protected void setValue(final String newValue) {
        if (newValue == null) {
            isSet = false;
        } else {
            // TODO: if context -> evaluate
            isSet = !isDefaultValue(newValue) && !ContextParameterUtils.containContextVariables(newValue);
        }
        super.setValue(newValue);
    }
    
    private boolean isDefaultValue(final String newValue) {
        return newValue.equals(getValue());
    }
}
