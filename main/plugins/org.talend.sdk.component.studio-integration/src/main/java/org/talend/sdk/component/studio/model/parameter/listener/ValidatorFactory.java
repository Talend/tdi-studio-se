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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

/**
 * Creates Validators
 */
public class ValidatorFactory {

    public List<PropertyChangeListener> createValidators(final PropertyValidation validation, final ValidationLabel label) {
        if (validation == null) {
            return Collections.emptyList();
        }
        List<PropertyChangeListener> validators = new ArrayList<>();
        if (validation.getRequired() != null && validation.getRequired()) {
            validators.add(new RequiredValidator(label));
        }
        if (validation.getMax() != null) {
            validators.add(new MaxValidator(label, validation.getMax()));
        }
        if (validation.getMaxLength() != null) {
            validators.add(new MaxLengthValidator(label, validation.getMaxLength()));
        }
        if (validation.getMin() != null) {
            validators.add(new MinValidator(label, validation.getMin()));
        }
        if (validation.getMinLength() != null) {
            validators.add(new MinLengthValidator(label, validation.getMinLength()));
        }
        if (validation.getPattern() != null) {
            validators.add(new PatternValidator(label, validation.getPattern()));
        }
        return validators;
    }

}
