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

import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

/**
 * Checks whether new value's length is greater than specified {@code minLength} value
 */
public class MinLengthValidator extends PropertyValidator {

    private final Integer minLength;

    public MinLengthValidator(final ValidationLabel label, final Integer minLength) {
        super(label, Messages.getString("validation.minlength.error", minLength));
        this.minLength = minLength;
    }

    @Override
    boolean validate(final Object newValue) {
        if (newValue == null) {
            return false;
        }
        String value = (String) newValue;
        return minLength.compareTo(value.length()) <= 0;
    }

}
