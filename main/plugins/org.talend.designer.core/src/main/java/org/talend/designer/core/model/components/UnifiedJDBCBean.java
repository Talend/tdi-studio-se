// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class UnifiedJDBCBean {

    private String databaseId;

    private String displayName;

    private String driverClass;

    private String url;

    private List<String> paths = new ArrayList<String>();

    private List<String> excludeDefinitions = new ArrayList<String>();

    private List<UnifiedJDBCConfigurationBean> parameterConfigurations = new ArrayList<UnifiedJDBCConfigurationBean>();

    /**
     * Getter for databaseId.
     * 
     * @return the databaseId
     */
    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * Sets the databaseId.
     * 
     * @param databaseId the databaseId to set
     */
    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * Getter for componentKey.
     * 
     * @return the componentKey
     */
    public String getComponentKey() {
        return StringUtils.deleteWhitespace(displayName);
    }

    /**
     * Getter for displayName.
     * 
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the displayName.
     * 
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for driverClass.
     * 
     * @return the driverClass
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * Sets the driverClass.
     * 
     * @param driverClass the driverClass to set
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * Getter for url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     * 
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter for paths.
     * 
     * @return the paths
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Sets the paths.
     * 
     * @param paths the paths to set
     */
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    /**
     * Getter for excludeDefinitions.
     * 
     * @return the excludeDefinitions
     */
    public List<String> getExcludeDefinitions() {
        return excludeDefinitions;
    }

    /**
     * Sets the excludeDefinitions.
     * 
     * @param excludeDefinitions the excludeDefinitions to set
     */
    public void setExcludeDefinitions(List<String> excludeDefinitions) {
        this.excludeDefinitions = excludeDefinitions;
    }

    /**
     * Getter for parameterConfigurations.
     * 
     * @return the parameterConfigurations
     */
    public List<UnifiedJDBCConfigurationBean> getParameterConfigurations() {
        return parameterConfigurations;
    }

    /**
     * Sets the parameterConfigurations.
     * 
     * @param parameterConfigurations the parameterConfigurations to set
     */
    public void setParameterConfigurations(List<UnifiedJDBCConfigurationBean> parameterConfigurations) {
        this.parameterConfigurations = parameterConfigurations;
    }

}
