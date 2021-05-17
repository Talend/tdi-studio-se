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
package org.talend.designer.core.generic.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class SchemaBean {

    List<TableBean> tables = new ArrayList<TableBean>();

    String type;

    String name;


    public String getName() {
        return name;
    }



    public void setName(String name) {
        this.name = name;
    }


    public SchemaBean(){

    }


    public List<TableBean> getTables() {
        return tables;
    }


    public void setTables(List<TableBean> tables) {
        this.tables = tables;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }

}
