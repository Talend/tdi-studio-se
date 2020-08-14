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

/**
 * DOC jding  class global comment. Detailled comment
 */
public class UnifiedJDBCBean {

    private String databaseId;

    private String driverClass;

    private String url;

    private List<String> paths = new ArrayList<String>();

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

}
