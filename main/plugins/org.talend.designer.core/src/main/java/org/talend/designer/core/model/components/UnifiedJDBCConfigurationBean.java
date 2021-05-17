// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.components;

import java.util.HashMap;
import java.util.Map;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class UnifiedJDBCConfigurationBean {

    private String componentName;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * Getter for componentName.
     * 
     * @return the componentName
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Sets the componentName.
     * 
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Getter for parameters.
     * 
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void putParameter(String paramName, Object paramValue) {
        this.parameters.put(paramName, paramValue);
    }

}
