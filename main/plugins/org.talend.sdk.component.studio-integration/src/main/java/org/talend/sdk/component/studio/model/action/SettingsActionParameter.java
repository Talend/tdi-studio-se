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
package org.talend.sdk.component.studio.model.action;

import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.lang.Strings;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link IActionParameter} which is binded with ElementParameter.
 * It may be used to bind to TaCoKitElementParameter (String), DebouncedParameter (String)
 * and CheckElementParameter (boolean)
 */
public class SettingsActionParameter extends AbstractActionParameter {

    private final TaCoKitElementParameter setting;

    public SettingsActionParameter(final TaCoKitElementParameter setting, final String parameter) {
        super(setting.getName(), parameter);
        this.setting = setting;
    }

    private String getValue() {
        final String value = setting.getStringValue();
        if (value == null) {
            return "";
        } else {
            return Strings.removeQuotes(value);
        }
    }

    /**
     * Converts values stored in TaCoKitElementParameter to String
     * and returns single parameter, which key is action parameter name and
     * values is TaCoKitElementParameter's value
     *
     * @return Collection with single action parameter
     */
    @Override
    public Collection<Pair<String, String>> parameters() {
        final String key = getParameter();
        final String value = getValue();
        final Pair<String, String> parameter = new Pair<>(key, value);
        return Collections.singletonList(parameter);
    }
}
