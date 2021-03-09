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
package org.talend.designer.webservice.data;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class ArrayIndexList {

    private String parameterName;

    private String indexNum;

    public ArrayIndexList(String parameterName) {
        super();
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getIndexNum() {
        return this.indexNum;
    }

    public void setIndexNum(String indexNum) {
        this.indexNum = indexNum;
    }

}
