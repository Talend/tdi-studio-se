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

import java.util.Objects;
import java.util.regex.Pattern;

public class ActionParameter {

    private static final Pattern QUOTES_PATTERN = Pattern.compile("^\"|\"$");

    /**
     * Action parameter name/path. It is used as a key in a Map, which used as action method payload
     */
    private final String parameter;

    /**
     * Parameter value
     */
    private String value;
    
    /**
     * Creates ActionParameter
     * 
     * @param parameter action method parameter name/path
     */
    public ActionParameter(final String parameter) {
        this(parameter, null);
    }
    
    /**
     * Creates ActionParameter
     * 
     * @param parameter action method parameter name/path
     * @param value action method parameter initial value
     */
    public ActionParameter(final String parameter, final String value) {
        Objects.requireNonNull(parameter, "parameter should not be null");
        if (parameter.isEmpty()) {
            throw new IllegalArgumentException("parameter should not be empty");
        }
        this.parameter = parameter;
        setValue0(value);
    }
    
    /**
     * Parameter value
     */
    protected String getValue() {
        return this.value;
    }

    /**
     * Sets ActionParameter new value.
     * 
     * @param newValue new value of ActionParameter to be set
     */
    protected void setValue(final String newValue) {
        setValue0(newValue);
    }
    
    /*
     * a part of Constructor
     * setValue() method is designed for inheritance, thus it can't be called in Constructor
     */
    private void setValue0(final String newValue) {
        if (newValue != null) {
            this.value = removeQuotes(newValue);
        } else {
            this.value = null;
        }
    }

    protected final String removeQuotes(final String quotedString) {
        return QUOTES_PATTERN.matcher(quotedString).replaceAll("");
    }

    /**
     * Action parameter alias, which used to make callback
     */
    String getParameter() {
        return this.parameter;
    }

}
