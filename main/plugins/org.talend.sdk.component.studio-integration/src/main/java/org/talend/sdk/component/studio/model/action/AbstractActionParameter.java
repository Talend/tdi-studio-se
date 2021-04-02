/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.model.action;

import org.talend.core.model.process.INode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public abstract class AbstractActionParameter implements IActionParameter {

    /**
     * ElementParameter name (which also denotes its path)
     */
    private final String name;

    /**
     * Action parameter alias, which used to make callback
     */
    private final String parameter;

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

    protected boolean isUseExistConnection(TaCoKitElementParameter parameter) {
        if (parameter.getElement() instanceof INode) {
            return TaCoKitUtil.isUseExistConnection((INode) parameter.getElement());
        }
        return false;
    }

    protected Object getParameterValueFromConnection(TaCoKitElementParameter parameter, String parameterName) {
        if (parameter.getElement() instanceof INode) {
            return TaCoKitUtil.getParameterValueFromConnection((INode) parameter.getElement(), parameterName);
        }
        return null;
    }

    protected boolean isDataStoreParameter(TaCoKitElementParameter parameter) {
        if (parameter.getElement() instanceof INode) {
            return TaCoKitUtil.isDataStoreParameter((INode) parameter.getElement(), parameter.getName());
        }
        return false;
    }
}
