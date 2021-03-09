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
package org.talend.designer.dbmap.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DBMapHelper {

    public static void saveDataToEmf(DBMapData emfMapperData, ExternalDbMapData externalData) {
        // input
        if (externalData.getInputTables() != null && !externalData.getInputTables().isEmpty()) {
            for (ExternalDbMapTable table : externalData.getInputTables()) {
                final InputTable persistentTable = DbmapFactory.eINSTANCE.createInputTable();
                persistentTable.setMinimized(table.isMinimized());
                persistentTable.setName(table.getName());
                persistentTable.setAlias(table.getAlias());
                persistentTable.setJoinType(table.getJoinType());
                persistentTable.setTableName(table.getTableName());
                if (table.getMetadataTableEntries() != null && !table.getMetadataTableEntries().isEmpty()) {
                    List<DBMapperTableEntry> persistentEntities = new ArrayList<DBMapperTableEntry>();
                    for (ExternalDbMapEntry entity : table.getMetadataTableEntries()) {
                        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
                        emfMapperTableEntry.setExpression(entity.getExpression());
                        emfMapperTableEntry.setName(entity.getName());
                        emfMapperTableEntry.setJoin(entity.isJoin());
                        emfMapperTableEntry.setOperator(entity.getOperator());
                        persistentEntities.add(emfMapperTableEntry);
                    }
                    persistentTable.getDBMapperTableEntries().addAll(persistentEntities);

                }
                emfMapperData.getInputTables().add(persistentTable);
            }
        }
        // output
        if (externalData.getOutputTables() != null && !externalData.getOutputTables().isEmpty()) {
            for (ExternalDbMapTable table : externalData.getOutputTables()) {
                final OutputTable persistentTable = DbmapFactory.eINSTANCE.createOutputTable();
                persistentTable.setMinimized(table.isMinimized());
                persistentTable.setName(table.getName());
                persistentTable.setTableName(table.getTableName());
                if (table.getMetadataTableEntries() != null && !table.getMetadataTableEntries().isEmpty()) {
                    for (ExternalDbMapEntry entity : table.getMetadataTableEntries()) {
                        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
                        emfMapperTableEntry.setExpression(entity.getExpression());
                        emfMapperTableEntry.setName(entity.getName());
                        persistentTable.getDBMapperTableEntries().add(emfMapperTableEntry);
                    }

                    // filters
                    if (table.getCustomWhereConditionsEntries() != null && !table.getCustomWhereConditionsEntries().isEmpty()) {
                        for (ExternalDbMapEntry entity : table.getCustomWhereConditionsEntries()) {
                            final FilterEntry persistentEntry = DbmapFactory.eINSTANCE.createFilterEntry();
                            persistentEntry.setName(entity.getName());
                            persistentEntry.setExpression(entity.getExpression());
                            persistentEntry.setFilterKind(FilterTableEntry.WHERE_FILTER);
                            persistentTable.getFilterEntries().add(persistentEntry);
                        }
                    }
                    if (table.getCustomOtherConditionsEntries() != null && !table.getCustomOtherConditionsEntries().isEmpty()) {
                        for (ExternalDbMapEntry entity : table.getCustomOtherConditionsEntries()) {
                            final FilterEntry persistentEntry = DbmapFactory.eINSTANCE.createFilterEntry();
                            persistentEntry.setName(entity.getName());
                            persistentEntry.setExpression(entity.getExpression());
                            persistentEntry.setFilterKind(FilterTableEntry.OTHER_FILTER);
                            persistentTable.getFilterEntries().add(persistentEntry);
                        }
                    }
                }

                emfMapperData.getOutputTables().add(persistentTable);

            }
        }

    }

    public static List<String> convertContextParameterValue(List<String> paramValues, String paramValue) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(paramValue)) {
            if (paramValue.indexOf("+") != -1) { //$NON-NLS-1$
                return paramValues;
            }
            String paramValueTemp = paramValue;
            int newNameLength = paramValueTemp.length();
            // Cases:context.a.context.b.context.c ... /context.a.context.b /context.a.b /a.context.b
            if (ContextParameterUtils.isContainContextParam(paramValueTemp)) {
                if (paramValueTemp.startsWith(ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX)) {
                    int contextPrefixLength = ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX.length();
                    int index = paramValueTemp.indexOf(".", contextPrefixLength); //$NON-NLS-1$
                    if (index > contextPrefixLength) {
                        paramValues.add(paramValueTemp.substring(0, index));
                        String contextSub = paramValueTemp.substring(index + 1, newNameLength);
                        if (ContextParameterUtils.isContainContextParam(contextSub) && contextSub.split("\\.").length > 2) { //$NON-NLS-1$
                            return convertContextParameterValue(paramValues, contextSub);
                        } else {
                            paramValues.add(contextSub);
                        }
                    }
                } else {
                    int index = paramValueTemp.indexOf(".");//$NON-NLS-1$
                    if (index > 0) {
                        paramValues.add(paramValueTemp.substring(0, index));
                        String contextSub = paramValueTemp.substring(index + 1, newNameLength);
                        if (ContextParameterUtils.isContainContextParam(contextSub) && contextSub.split("\\.").length > 2) { //$NON-NLS-1$
                            return convertContextParameterValue(paramValues, contextSub);
                        } else {
                            paramValues.add(contextSub);
                        }
                    }
                }
            }
        }
        return paramValues;
    }
}
