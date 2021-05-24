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
package org.talend.designer.dbmap.language.hive;

import java.util.List;
import java.util.Set;

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

    @Override
    protected String replaceVariablesForExpression(DbMapComponent component, String expression) {
        if (expression == null) {
            return null;
        }

            List<String> contextList = getContextList(component);
            boolean haveReplace = false;
            for (String context : contextList) {
                if (expression.contains(context)) {
                    expression = expression.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    haveReplace = true;
                }
            }
            if (!haveReplace) {
                List<String> connContextList = getConnectionContextList(component);
                for (String context : connContextList) {
                    if (expression.contains(context)) {
                        expression = expression.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                }
            }
            Set<String> globalMapList = getGlobalMapList(component, expression);
            for (String globalMapStr : globalMapList) {
                String regex = parser.getGlobalMapExpressionRegex(globalMapStr);
                String replacement = parser.getGlobalMapReplacement(globalMapStr);
                expression = expression.replaceAll(regex, "\" +" + replacement + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$
            }

        return expression;
    }
}
