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

import org.talend.core.model.process.IElement;

/**
 * Represents Check parameter. Table parameter is ElementParameter, which EParameterFieldType is CHECK.
 * Value of Table parameter should have Boolean type.
 * This class provides correct conversion from/to String of parameter value
 */
public class CheckElementParameter extends TaCoKitElementParameter {

    public CheckElementParameter(final IElement element) {
        super(element);
    }

    /**
     * Retrieves stored value and converts it to String using {@link Boolean#toString()} method
     *
     * @return string representation of stored value
     */
    @Override
    public String getStringValue() {
        return super.getValue().toString();
    }

    /**
     * Sets new parameter value. If new value is of type String, converts it to Boolean.
     * If new value is of type Boolean, then sets it without conversion.
     * Else it throws exception.
     *
     * @param newValue value to be set
     */
    @Override
    public void setValue(final Object newValue) {
        if (newValue == null) {
            super.setValue(false);
        } else if (newValue instanceof String) {
            super.setValue(Boolean.parseBoolean((String) newValue));
        } else if (newValue instanceof Boolean) {
            super.setValue(newValue);
        } else {
            throw new IllegalArgumentException("wrong type on new value: " + newValue.getClass().getName());
        }
    }

}
