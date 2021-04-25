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
package org.talend.designer.core.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class NotReallyNeedSchemaDBS {

    private List<String> needSchemaDBNames = new ArrayList<String>();

    // these dbs are not really use Schema when create a database connection in repository
    public void init() {
        try {
            needSchemaDBNames.add(EDatabaseTypeName.IBMDB2.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.SYBASEASE.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.MSSQL.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.ORACLEFORSID.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.ORACLESN.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.ORACLE_OCI.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.PSQL.getProduct());
            needSchemaDBNames.add(EDatabaseTypeName.PLUSPSQL.getProduct());
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    public List<String> getNeedSchemaDBNames() {
        return this.needSchemaDBNames;
    }

}
