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
package org.talend.sdk.component.studio.model.action;

import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.lang.Strings;

import java.util.Collection;
import java.util.Collections;

public class ActionParameter extends AbstractActionParameter {

    /**
     * Denotes whether associated ElementParameter is set and usable. Once set it can't be
     * unset
     */
    private boolean hasDirectValue = false;

    /**
     * Parameter value
     */
    private String value;

    /**
     * Creates ActionParameter
     * 
     * @param name ElementParameter name
     * @param parameter Action parameter name
     * @param value initial value, can be null. If it's not null, then it switches ActionParameter to set state
     */
    public ActionParameter(final String name, final String parameter, final String value) {
        super(name, parameter);
        setValue(value);
    }

    public void setValue(final String newValue) {
        if (newValue != null) {
            this.value = Strings.removeQuotes(newValue);
            // todo: if context -> evaluate
            this.hasDirectValue = !this.value.equals(newValue) || !ContextParameterUtils.containContextVariables(newValue);
        } else {
            this.value = null;
            this.hasDirectValue = false;
        }
    }

    /**
     * Denotes whether associated ElementParameter is set and usable. Once set it can't be unset
     */
    public boolean isHasDirectValue() {
        return this.hasDirectValue;
    }

    /**
     * Parameter value
     */
    String getValue() {
        return this.value;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final Pair<String, String> parameter = new Pair<>(getParameter(), getValue());
        return Collections.singletonList(parameter);
    }
}
