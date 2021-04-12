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
package org.talend.sqlbuilder.sqlcontrol;

import org.talend.sqlbuilder.dataset.dataset.DataSet;

/**
 * Store SQL Result. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class SQLResult {

    private DataSet dataSet;

    private long executionTimeMillis;

    private String sqlStatement;

    /**
     * @return Returns the sqlStatement.
     */
    public String getSqlStatement() {
        return sqlStatement;
    }

    /**
     * @return Returns the dataSet.
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * @return Returns the executionTimeMillis.
     */
    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    /**
     * Sets the dataSet.
     *
     * @param dataSet the dataSet to set
     */
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * Sets the executionTimeMillis.
     *
     * @param executionTimeMillis the executionTimeMillis to set
     */
    public void setExecutionTimeMillis(long executionTimeMillis) {
        this.executionTimeMillis = executionTimeMillis;
    }

    /**
     * Sets the sqlStatement.
     *
     * @param sqlStatement the sqlStatement to set
     */
    public void setSqlStatement(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }
}
