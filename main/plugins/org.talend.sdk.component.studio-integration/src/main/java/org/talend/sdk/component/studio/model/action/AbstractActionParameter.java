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
package org.talend.sdk.component.studio.model.action;

public abstract class AbstractActionParameter implements IActionParameter {

    /**
     * ElementParameter name (which also denotes its path)
     */
    private final String name;

    /**
     * Action parameter alias, which used to make callback
     */
    private final String parameter;

    protected int rowNumber;

    protected boolean missingRequired;

    /**
     * Creates ActionParameter
     *
     * @param name ElementParameter name
     * @param parameter Action parameter name
     */
    public AbstractActionParameter(final String name, final String parameter) {
        this.name = name;
        this.parameter = parameter;
    }

    /**
     * ElementParameter name (which also denotes its path)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Action parameter alias, which used to make callback
     */
    protected String getParameter() {
        return this.parameter;
    }

    public int getRowNumber() {
        return this.rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public boolean isMissingRequired() {
        return this.missingRequired;
    }

    public void setMissingRequired(boolean missingRequired) {
        this.missingRequired = missingRequired;
    }
}
