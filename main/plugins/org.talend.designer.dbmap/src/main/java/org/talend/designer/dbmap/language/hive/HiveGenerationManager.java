// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.hive;

import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;

/**
 * created by wchen on 2012-8-13 Detailled comment
 * 
 */
public class HiveGenerationManager extends DbGenerationManager {

    /**
     * DOC talend2 HiveGenerationManager constructor comment.
     * 
     * @param language
     */
    public HiveGenerationManager() {
        super(new HiveLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        String query = super.buildSqlSelect(component, outputTableName);
        // tELTHiveMap no need DBName/SchemaName in the SELECT stattement of the HIVE QL generated
        ExternalDbMapData data = (ExternalDbMapData) component.getExternalData();
        for (ExternalDbMapTable input : data.getInputTables()) {
            String inputTableName = input.getTableName();
            if (inputTableName != null && inputTableName.contains(".")) {
                String[] inputTableNames = inputTableName.split("\\.");
                if (inputTableNames.length > 1) {
                    query = query.replaceAll(inputTableName + "\\.", inputTableNames[1] + "."); //$NON-NLS-1$ //$NON-NLS-2$ 
                    for (int i = 0; i < querySegments.size(); i++) {
                        String segment = querySegments.get(i);
                        segment = segment.replaceAll(inputTableName + "\\.", inputTableNames[1] + "."); //$NON-NLS-1$ //$NON-NLS-2$
                        querySegments.set(i, segment);
                    }
                }
            }
        }
        return query;
    }
}
