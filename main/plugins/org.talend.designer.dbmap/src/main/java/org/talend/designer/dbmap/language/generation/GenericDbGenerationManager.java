// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.generation;

import java.util.Optional;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.language.GenericDbLanguage;
import org.talend.designer.dbmap.language.mssql.MssqlGenerationManager;

/**
 * wzhang class global comment. Detailled comment
 */
public class GenericDbGenerationManager extends DbGenerationManager {

    public GenericDbGenerationManager() {
        super(new GenericDbLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        try {
            if (Optional.ofNullable(component).map(c -> c.getComponent()).map(c -> "tELTMSSqlMap".equals(c.getName()))
                    .orElse(false)) {
                MssqlGenerationManager genManager = new MssqlGenerationManager();
                String result = genManager.buildSqlSelect(component, outputTableName);
                this.queryColumnsName = genManager.getQueryColumnsName();
                return result;
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
        return super.buildSqlSelect(component, outputTableName);
    }
}
