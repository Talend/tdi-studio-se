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

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.util.TaCoKitConst;
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
        IElement element = parameter.getElement();
        if (element.getElementParameters() != null) {
            for (int i = 0; i < element.getElementParameters().size(); i++) {
                ElementParameter ele = (ElementParameter) element.getElementParameters().get(i);
                if (TaCoKitConst.PARAMETER_USE_EXISTING_CONNECTION.equals(ele.getName())) {
                    if (ele.getValue() != null && Boolean.parseBoolean(ele.getValue().toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected String getUseExistConnectionName(TaCoKitElementParameter parameter) {
        IElement element = parameter.getElement();
        if (element.getElementParameters() != null) {
            for (int i = 0; i < element.getElementParameters().size(); i++) {
                ElementParameter ele = (ElementParameter) element.getElementParameters().get(i);
                if (TaCoKitConst.PARAMETER_CONNECTION.equals(ele.getName())) {
                    if (ele.getValue() == null || StringUtils.isEmpty(ele.getValue().toString())) {
                        return null;
                    } else {
                        return ele.getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    protected String getParameterValueFromConnection(TaCoKitElementParameter parameter, String parameterName) {
        String connectionName = getUseExistConnectionName(parameter);
        if (connectionName != null && parameter.getElement() instanceof Node) {
            Node node = (Node) parameter.getElement();
            IProcess process = node.getProcess();
            INode connectionNode = process.getNodeByUniqueName(connectionName);
            if (connectionNode != null) {
                String datastoreName = TaCoKitUtil.getDataStorePath((ComponentModel) node.getComponent(), parameter.getName());
                IElementParameter param = connectionNode.getElementParameter(datastoreName);
                if (param != null) {
                    return param.getValue() == null ? "" : String.valueOf(param.getValue());
                } else {
                    throw new IllegalArgumentException("Can't find parameter:" + parameterName);
                }
            } else {
                throw new IllegalArgumentException("Can't find connection node:" + connectionName);
            }
        }
        return "";
    }

    protected boolean isDataStoreParameter(TaCoKitElementParameter parameter) {
        if (parameter.getElement() instanceof Node) {
            Node node = (Node) parameter.getElement();
            if (node.getComponent() instanceof ComponentModel) {
                ComponentModel model = (ComponentModel) node.getComponent();
                if (TaCoKitUtil.isDataStorePath(model, parameter.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
