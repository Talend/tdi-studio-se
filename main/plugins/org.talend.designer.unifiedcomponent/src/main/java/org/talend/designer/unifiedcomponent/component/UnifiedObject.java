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
package org.talend.designer.unifiedcomponent.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * created by wchen on Dec 13, 2017 Detailled comment
 *
 */
public class UnifiedObject {

    private String database;

    private String componentName;

    private String displayComponentName;

    private Set<String> supportedCategories = new HashSet<String>();

    private Map<String, String> parameterMapping = new HashMap<String, String>();

    private Map<String, String> connectorMapping = new HashMap<String, String>();

    private Set<String> paramMappingExclude = new HashSet<String>();

    private Set<String> hideFamilies = new HashSet<String>();

    /**
     * Getter for database.
     *
     * @return the database
     */
    public String getDatabase() {
        return this.database;
    }

    /**
     * Sets the database.
     *
     * @param database the database to set
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Getter for componentName.
     *
     * @return the componentName
     */
    public String getComponentName() {
        return this.componentName;
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
     * Getter for supportedCategories.
     *
     * @return the supportedCategories
     */
    public Set<String> getSupportedCategories() {
        return this.supportedCategories;
    }

    /**
     * Getter for parameterMapping.
     *
     * @return the parameterMapping
     */
    public Map<String, String> getParameterMapping() {
        return this.parameterMapping;
    }

    /**
     * Getter for paramMappingExclude.
     *
     * @return the paramMappingExclude
     */
    public Set<String> getParamMappingExclude() {
        return this.paramMappingExclude;
    }

    /**
     * Getter for connectorMapping.
     *
     * @return the connectorMapping
     */
    public Map<String, String> getConnectorMapping() {
        return this.connectorMapping;
    }

    /**
     * Getter for hideFamilies.
     *
     * @return the hideFamilies
     */
    public Set<String> getHideFamilies() {
        return this.hideFamilies;
    }

    public String getDisplayComponentName() {
        if (displayComponentName != null) {
            return displayComponentName;
        }
        return componentName;
    }

    public void setDisplayComponentName(String jdbcComponent) {
        this.displayComponentName = jdbcComponent;
    }

}
