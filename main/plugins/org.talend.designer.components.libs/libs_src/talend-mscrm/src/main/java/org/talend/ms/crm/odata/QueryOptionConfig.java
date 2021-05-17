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
package org.talend.ms.crm.odata;

public class QueryOptionConfig {

    private int top = -1;

    private String orderBy;

    private String skipToken;

    private String[] returnEntityProperties;

    private String filter;

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSkipToken() {
        return skipToken;
    }

    public void setSkipToken(String skipToken) {
        this.skipToken = skipToken;
    }

    public String[] getReturnEntityProperties() {
        return returnEntityProperties;
    }

    public void setReturnEntityProperties(String[] returnEntityProperties) {
        this.returnEntityProperties = returnEntityProperties;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}
