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

import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

/**
 * Checks whether new value's length is less than specified {@code maxLength} value
 */
public class MaxLengthValidator extends PropertyValidator {

    private final Integer maxLength;

    public MaxLengthValidator(final ValidationLabel label, final Integer maxLength) {
        super(label, Messages.getString("validation.maxlength.error", maxLength));
        this.maxLength = maxLength;
    }

    @Override
    boolean validate(final Object newValue) {
        if (newValue == null) {
            return false;
        }
        String value = (String) newValue;
        return maxLength.compareTo(value.length()) >= 0;
    }

}
