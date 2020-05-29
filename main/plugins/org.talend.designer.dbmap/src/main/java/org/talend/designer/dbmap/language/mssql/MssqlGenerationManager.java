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
package org.talend.designer.dbmap.language.mssql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.AbstractDbLanguage;
import org.talend.designer.dbmap.language.GenericDbLanguage;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.language.generation.DbMapSqlConstants;

public class MssqlGenerationManager extends DbGenerationManager {

    public MssqlGenerationManager() {
        super(new GenericDbLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        boolean checkUseUpdateStatement = checkUseUpdateStatement(component, outputTableName);
        if (checkUseUpdateStatement) {
            return buildSqlSelect(component, outputTableName, DEFAULT_TAB_SPACE_STRING, checkUseUpdateStatement);
        } else {
            return super.buildSqlSelect(component, outputTableName, DEFAULT_TAB_SPACE_STRING);
        }
    }

    public String buildSqlSelect(DbMapComponent dbMapComponent, String outputTableName, String tabString,
            boolean checkUseUpdateStatement) {
        queryColumnsName = "\""; //$NON-NLS-1$
        aliasAlreadyDeclared.clear();
        queryColumnsSegments.clear();
        querySegments.clear();
        subQueryTable.clear();
        inputSchemaContextSet.clear();

        this.tabSpaceString = tabString;
        DbMapComponent component = getDbMapComponent(dbMapComponent);

        List<IConnection> outputConnections = (List<IConnection>) component.getOutgoingConnections();

        Map<String, IConnection> nameToOutputConnection = new HashMap<String, IConnection>();
        for (IConnection connection : outputConnections) {
            nameToOutputConnection.put(connection.getUniqueName(), connection);
        }

        ExternalDbMapData data = component.getExternalData();
        StringBuilder sb = new StringBuilder();

        List<ExternalDbMapTable> outputTables = data.getOutputTables();
        int lstOutputTablesSize = outputTables.size();
        ExternalDbMapTable outputTable = null;
        for (int i = 0; i < lstOutputTablesSize; i++) {
            ExternalDbMapTable temp = outputTables.get(i);
            if (outputTableName.equals(temp.getName())) {
                outputTable = temp;
                break;
            }
        }

        if (outputTable != null) {
            String outTableName = outputTable.getTableName();
            IConnection connection = nameToOutputConnection.get(outputTable.getName());
            List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
            if (connection != null) {
                IMetadataTable metadataTable = connection.getMetadataTable();
                if (metadataTable != null) {
                    columns.addAll(metadataTable.getListColumns());
                }
            }
            // Update
            String targetSchemaTable = outTableName;
            IElementParameter eltSchemaNameParam = source.getElementParameter("ELT_SCHEMA_NAME"); //$NON-NLS-1$
            if (eltSchemaNameParam != null && eltSchemaNameParam.getValue() != null) {
                String schema = TalendQuoteUtils.removeQuotesIfExist(String.valueOf(eltSchemaNameParam.getValue()));
                if (org.apache.commons.lang.StringUtils.isNotEmpty(schema)) {
                    targetSchemaTable = addQuotes(schema) + DbMapSqlConstants.DOT + addQuotes(outTableName);
                }
            }

            appendSqlQuery(sb, "\"", false); //$NON-NLS-1$
            appendSqlQuery(sb, DbMapSqlConstants.UPDATE);
            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
            appendSqlQuery(sb, targetSchemaTable);
            appendSqlQuery(sb, tabSpaceString);
            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);

            // Set
            String keyColumn = DbMapSqlConstants.EMPTY;
            List<ExternalDbMapEntry> metadataTableEntries = outputTable.getMetadataTableEntries();
            if (metadataTableEntries != null) {
                appendSqlQuery(sb, "SET"); //$NON-NLS-1$
                appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                boolean isKey = false;
                int lstSizeOutTableEntries = metadataTableEntries.size();
                List<Boolean> setColumns = getSetColumnsForUpdateQuery();
                final boolean hasDeactivatedColumns = !setColumns.isEmpty();
                boolean isFirstColumn = true;
                for (int i = 0; i < lstSizeOutTableEntries; i++) {
                    if (hasDeactivatedColumns && setColumns.get(i)) {
                        continue;
                    }
                    ExternalDbMapEntry dbMapEntry = metadataTableEntries.get(i);
                    String columnEntry = dbMapEntry.getName();
                    String expression = dbMapEntry.getExpression();
                    expression = initExpression(component, dbMapEntry);
                    expression = addQuoteForSpecialChar(expression, component);
                    //
                    if (!DEFAULT_TAB_SPACE_STRING.equals(this.tabSpaceString)) {
                        expression += DbMapSqlConstants.SPACE + DbMapSqlConstants.AS + DbMapSqlConstants.SPACE
                                + getAliasOf(dbMapEntry.getName());
                    }
                    String exp = replaceVariablesForExpression(component, expression);
                    String columnSegment = exp;
                    // Added isFirstColumn to conform old behaior if first column is skipped
                    if (i > 0 && !isFirstColumn) {
                        queryColumnsName += DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE;
                        columnSegment = DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE + columnSegment;
                    }
                    if (expression != null && expression.trim().length() > 0) {
                        queryColumnsName += exp;
                        queryColumnsSegments.add(columnSegment);
                    }
                    //
                    if (!isKey) {
                        for (IMetadataColumn column : columns) {
                            String columnName = column.getLabel();
                            if (columnName.equals(dbMapEntry.getName()) && column.isKey()) {
                                isKey = column.isKey();
                                keyColumn = addQuotes(columnEntry) + " = " + expression;//$NON-NLS-1$
                                break;
                            }
                        }
                        if (isKey) {
                            continue;
                        }
                    }
                    if (expression != null && expression.trim().length() > 0) {
                        // Append COMMA and NEW_LINE for all columns except FIRST.
                        if (!isFirstColumn) {
                            appendSqlQuery(sb, DbMapSqlConstants.COMMA);
                            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                        } else {
                            isFirstColumn = false;
                        }
                        appendSqlQuery(sb, addQuotes(columnEntry) + " = " + expression); //$NON-NLS-1$
                    }
                }
            }
            if ("\"".equals(queryColumnsName)) {
                throw new IllegalArgumentException("Specify at least 1 column for UPDATE QUERY in SET section");
            }
            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);

            // From
            appendSqlQuery(sb, tabSpaceString);
            appendSqlQuery(sb, DbMapSqlConstants.FROM);
            // load input table in hash
            List<ExternalDbMapTable> inputTables = data.getInputTables();
            // load input table in hash
            boolean explicitJoin = false;
            int lstSizeInputTables = inputTables.size();

            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
            appendSqlQuery(sb, tabSpaceString);
            IJoinType previousJoinType = null;

            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                IJoinType joinType = language.getJoin(inputTable.getJoinType());
                if (!language.unuseWithExplicitJoin().contains(joinType) && i > 0) {
                    explicitJoin = true;
                } else {
                    explicitJoin = false;
                }
                if (i == 0) {
                    joinType = AbstractDbLanguage.JOIN.NO_JOIN;
                    previousJoinType = joinType;
                } else {
                    joinType = language.getJoin(inputTable.getJoinType());
                }
                boolean commaCouldBeAdded = !explicitJoin && i > 0;
                boolean crCouldBeAdded = false;
                if (language.unuseWithExplicitJoin().contains(joinType) && !explicitJoin) {
                    buildTableDeclaration(component, sb, inputTable, commaCouldBeAdded, crCouldBeAdded, false);

                } else if (!language.unuseWithExplicitJoin().contains(joinType) && explicitJoin) {
                    if (i > 0) {
                        if (previousJoinType == null) {
                            buildTableDeclaration(component, sb, inputTables.get(i - 1), commaCouldBeAdded, crCouldBeAdded, true);
                            previousJoinType = joinType;
                        } else {
                            // appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                            appendSqlQuery(sb, tabSpaceString);
                        }
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                    }
                    String labelJoinType = joinType.getLabel();
                    if (joinType == AbstractDbLanguage.JOIN.CROSS_JOIN) {
                        ExternalDbMapTable nextTable = null;
                        if (i < lstSizeInputTables) {
                            nextTable = inputTables.get(i);
                            buildTableDeclaration(component, sb, nextTable, false, false, true);
                        }

                    } else {
                        if (isConditionChecked(component, inputTable)) {
                            appendSqlQuery(sb, labelJoinType);
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            buildTableDeclaration(component, sb, inputTable, false, false, true);
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            appendSqlQuery(sb, DbMapSqlConstants.ON);
                            appendSqlQuery(sb, DbMapSqlConstants.LEFT_BRACKET);
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            buildConditions(component, sb, inputTable, true, true, true);
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            appendSqlQuery(sb, DbMapSqlConstants.RIGHT_BRACKET);
                        } else {
                            commaCouldBeAdded = true;
                            buildTableDeclaration(component, sb, inputTable, commaCouldBeAdded, crCouldBeAdded, false);
                        }

                    }

                }
            }

            // where
            StringBuilder sbWhere = new StringBuilder();
            this.tabSpaceString = DEFAULT_TAB_SPACE_STRING;
            boolean isFirstClause = true;
            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                if (buildConditions(component, sbWhere, inputTable, false, isFirstClause, false)) {
                    isFirstClause = false;
                }
            }
            /*
             * for addition conditions
             */
            // like as input.newcolumn1>100
            List<String> whereAddition = new ArrayList<String>();
            // olny pure start with group or order, like as order/group by input.newcolumn1
            // List<String> byAddition = new ArrayList<String>();
            // like as input.newcolumn1>100 group/oder by input.newcolumn1
            // List<String> containWhereAddition = new ArrayList<String>();
            // like as "OR/AND input.newcolumn1", will keep original
            List<String> originalWhereAddition = new ArrayList<String>();
            List<String> otherAddition = new ArrayList<String>();

            if (outputTable != null) {
                List<ExternalDbMapEntry> customWhereConditionsEntries = outputTable.getCustomWhereConditionsEntries();
                if (customWhereConditionsEntries != null) {
                    for (ExternalDbMapEntry entry : customWhereConditionsEntries) {
                        String exp = initExpression(component, entry);
                        if (exp != null && !DbMapSqlConstants.EMPTY.equals(exp.trim())) {
                            if (containWith(exp, DbMapSqlConstants.OR, true) || containWith(exp, DbMapSqlConstants.AND, true)) {
                                exp = replaceVariablesForExpression(component, exp);
                                originalWhereAddition.add(exp);
                            } else {
                                exp = replaceVariablesForExpression(component, exp);
                                whereAddition.add(exp);
                            }
                        }
                    }
                }

                List<ExternalDbMapEntry> customOtherConditionsEntries = outputTable.getCustomOtherConditionsEntries();
                if (customOtherConditionsEntries != null) {
                    for (ExternalDbMapEntry entry : customOtherConditionsEntries) {
                        String exp = initExpression(component, entry);
                        if (exp != null && !DbMapSqlConstants.EMPTY.equals(exp.trim())) {
                            exp = replaceVariablesForExpression(component, exp);
                            otherAddition.add(exp);
                        }
                    }
                }
            }
            this.tabSpaceString = tabString;

            String whereClauses = sbWhere.toString();
            boolean whereFlag = whereClauses.trim().length() > 0;
            boolean whereAddFlag = !whereAddition.isEmpty();
            boolean whereOriginalFlag = !originalWhereAddition.isEmpty();
            if (whereFlag || whereAddFlag || whereOriginalFlag) {
                appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                appendSqlQuery(sb, tabSpaceString);
                appendSqlQuery(sb, DbMapSqlConstants.WHERE);
            }
            if (whereFlag) {
                appendSqlQuery(sb, whereClauses);
            }
            if (whereAddFlag) {
                for (int i = 0; i < whereAddition.size(); i++) {
                    if (i == 0 && whereFlag || i > 0) {
                        appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                        appendSqlQuery(sb, tabSpaceString);
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        appendSqlQuery(sb, DbMapSqlConstants.AND);
                    }
                    appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                    appendSqlQuery(sb, whereAddition.get(i));
                }
            }
            if (whereOriginalFlag) {
                for (String s : originalWhereAddition) {
                    appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                    appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                    appendSqlQuery(sb, s);
                }
            }
            if (!otherAddition.isEmpty()) {
                appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                appendSqlQuery(sb, tabSpaceString);
                for (String s : otherAddition) {
                    appendSqlQuery(sb, s);
                    appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                    appendSqlQuery(sb, tabSpaceString);
                }
            }
        }

        String sqlQuery = sb.toString();
        sqlQuery = handleQuery(sqlQuery);
        queryColumnsName = handleQuery(queryColumnsName);
        return sqlQuery;
    }

}
