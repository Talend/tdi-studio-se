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
package org.talend.sdk.component.studio.model.parameter.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;

import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

/**
 * Base class for Property Validators. It validates new value. If validation is not passed, then
 * it activates {@code label} ElementParameter and sets its validation message
 */
public abstract class PropertyValidator implements PropertyChangeListener {

    private final static Pattern CONTEXT_PATTERN = Pattern.compile("^(context\\.).*");

    private final ValidationLabel label;

    private final String validationMessage;

    PropertyValidator(final ValidationLabel label, final String validationMessage) {
        this.label = label;
        this.validationMessage = validationMessage;
    }

    /**
     * Validates new value, if it is raw value. Skips validation for contextual value
     *
     * @param event property change event, which provides new value to validate
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if(!"value".equals(event.getPropertyName())){
            return;
        }

        if (isContextualValue(event.getNewValue())) {
            label.hideConstraint(validationMessage);
        } else if (!validate(event.getNewValue())) {
            label.showConstraint(validationMessage);
        } else {
            label.hideConstraint(validationMessage);
        }
    }

    /**
     * Checks whether {@code value} is raw data or contains {@code context} variable.
     * It is assumed that any not String value is raw data.
     * The {@code value} contains {@code context} if some of words in it starts with "context."
     *
     * @param value value to check
     * @return true, if value contains {@code context} variables
     */
    protected boolean isContextualValue(final Object value) {
        if (!String.class.isInstance(value)) {
            return false;
        }
        String strValue = (String) value;
        String[] tokens = strValue.split(" ");
        for (String token : tokens) {
            if (isContextVariable(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether incoming token is context variable.
     *
     * @param token token to check
     * @return true, it the token is context variable
     */
    private boolean isContextVariable(final String token) {
        return CONTEXT_PATTERN.matcher(token).matches();
    }

    abstract boolean validate(final Object newValue);

}
