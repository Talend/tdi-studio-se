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
package org.talend.designer.dbmap.language.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.IConnection;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.AbstractDbLanguage;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.language.generation.DbMapSqlConstants;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.utils.DataMapExpressionParser;

/**
 *
 * wzhang class global comment. Detailled comment
 */
public class OracleGenerationManager extends DbGenerationManager {

    private static final String JOIN = "(+)";

    public OracleGenerationManager() {
        super(new OracleLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    /**
     *
     * ggu Comment method "buildSqlSelect".
     *
     * @param component
     * @param outputTableName
     * @param tabSpaceString
     * @return
     */
    @Override
    public String buildSqlSelect(DbMapComponent dbMapComponent, String outputTableName, String tabString) {
        queryColumnsName = "\""; //$NON-NLS-1$
        aliasAlreadyDeclared.clear();
        queryColumnsSegments.clear();
        querySegments.clear();
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

            IConnection connection = nameToOutputConnection.get(outputTable.getName());
            List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
            if (connection != null) {
                IMetadataTable metadataTable = connection.getMetadataTable();
                if (metadataTable != null) {
                    columns.addAll(metadataTable.getListColumns());
                    // call this function seems no use now but only problems
                    // outputTable = removeUnmatchingEntriesWithColumnsOfMetadataTable(outputTable, metadataTable);
                }
            }
            appendSqlQuery(sb, "\"", false); //$NON-NLS-1$
            appendSqlQuery(sb, DbMapSqlConstants.SELECT);
            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
            appendSqlQuery(sb, tabSpaceString);

            List<ExternalDbMapEntry> metadataTableEntries = outputTable.getMetadataTableEntries();
            if (metadataTableEntries != null) {
                int lstSizeOutTableEntries = metadataTableEntries.size();
                for (int i = 0; i < lstSizeOutTableEntries; i++) {
                    ExternalDbMapEntry dbMapEntry = metadataTableEntries.get(i);
                    String expression = dbMapEntry.getExpression();
                    expression = initExpression(component, dbMapEntry);
                    expression = addQuoteForSpecialChar(expression, component);
                    // for (IMetadataColumn column : columns) {
                    // if (expression != null && column.getLabel().equals(dbMapEntry.getName())) {
                    //                            expression = expression.replaceFirst("." + dbMapEntry.getName(), //$NON-NLS-1$
                    //                                    "." + column.getOriginalDbColumnName()); //$NON-NLS-1$
                    // break;
                    // }
                    // }
                    boolean added = false;
                    if (!DEFAULT_TAB_SPACE_STRING.equals(this.tabSpaceString)) {
                        expression += DbMapSqlConstants.SPACE + DbMapSqlConstants.AS + DbMapSqlConstants.SPACE
                                + getAliasOf(dbMapEntry.getName());
                        added = true;
                    }
                    String columnSegment = expression;
                    if (i > 0) {
                        appendSqlQuery(sb, DbMapSqlConstants.COMMA);
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        queryColumnsName += DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE;
                        columnSegment = DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE + columnSegment;
                    }
                    if (expression != null && expression.trim().length() > 0) {
                        String exp = replaceVariablesForExpression(component, expression);
                        appendSqlQuery(sb, exp);
                        boolean needAlias = needAlias(columns, dbMapEntry, expression);
                        if (!added && needAlias && isUseAliasInOutputTable()) {
                            String name = DbMapSqlConstants.SPACE + DbMapSqlConstants.AS + DbMapSqlConstants.SPACE
                                    + getAliasOf(dbMapEntry.getName());
                            appendSqlQuery(sb, name);
                        }
                        queryColumnsName += expression;
                        queryColumnsSegments.add(columnSegment);
                    } else {
                        appendSqlQuery(sb, DbMapSqlConstants.LEFT_COMMENT);
                        String str = outputTable.getName() + DbMapSqlConstants.DOT + dbMapEntry.getName();
                        appendSqlQuery(sb, Messages.getString("DbGenerationManager.OuputExpSetMessage", str));//$NON-NLS-1$
                        appendSqlQuery(sb, DbMapSqlConstants.RIGHT_COMMENT);
                    }
                }
            }

            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
            appendSqlQuery(sb, tabSpaceString);
            appendSqlQuery(sb, DbMapSqlConstants.FROM);

            List<ExternalDbMapTable> inputTables = data.getInputTables();

            // load input table in hash
            boolean explicitJoin = false;
            int lstSizeInputTables = inputTables.size();
            Map<String, ExternalDbMapTable> nameToInputTable = new HashMap<String, ExternalDbMapTable>();
            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                nameToInputTable.put(inputTable.getName(), inputTable);
                IJoinType joinType = language.getJoin(inputTable.getJoinType());
                if (!language.unuseWithExplicitJoin().contains(joinType) && i > 0) {
                    explicitJoin = true;
                }

            }

            StringBuilder sbWhere = new StringBuilder();
            this.tabSpaceString = DEFAULT_TAB_SPACE_STRING;
            boolean isFirstClause = true;
            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                if (buildConditions(component, sbWhere, inputTable, false, isFirstClause)) {
                    isFirstClause = false;
                }
            }
            this.tabSpaceString = tabString;

            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
            appendSqlQuery(sb, tabSpaceString);

            IJoinType previousJoinType = null;

            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                IJoinType joinType = null;
                if (i == 0) {
                    joinType = AbstractDbLanguage.JOIN.NO_JOIN;
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
                            appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                            appendSqlQuery(sb, tabSpaceString);
                        }
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                    }
                    String labelJoinType = joinType.getLabel();
                    appendSqlQuery(sb, labelJoinType);
                    appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                    if (joinType == AbstractDbLanguage.JOIN.CROSS_JOIN) {
                        ExternalDbMapTable nextTable = null;
                        if (i < lstSizeInputTables) {
                            nextTable = inputTables.get(i);
                            buildTableDeclaration(component, sb, nextTable, false, false, true);
                        }

                    } else {

                        // ExternalDbMapTable rightTable = joinLeftToJoinRightTables.get(inputTable.getName());
                        buildTableDeclaration(component, sb, inputTable, false, false, true);
                        // if (rightTable != null) {
                        // } else {
                        // sb.append(" <!! NO JOIN CLAUSES FOR '" + inputTable.getName() + "' !!> ");
                        // }
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        appendSqlQuery(sb, DbMapSqlConstants.ON);
                        appendSqlQuery(sb, DbMapSqlConstants.LEFT_BRACKET);
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        if (!buildConditions(component, sb, inputTable, true, true)) {
                            appendSqlQuery(sb, DbMapSqlConstants.LEFT_COMMENT);
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            appendSqlQuery(sb, Messages.getString("DbGenerationManager.conditionNotSet"));//$NON-NLS-1$
                            appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                            appendSqlQuery(sb, DbMapSqlConstants.RIGHT_COMMENT);
                        }
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        appendSqlQuery(sb, DbMapSqlConstants.RIGHT_BRACKET);
                    }

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
                this.tabSpaceString = DEFAULT_TAB_SPACE_STRING;
                List<ExternalDbMapEntry> customWhereConditionsEntries = outputTable.getCustomWhereConditionsEntries();
                if (customWhereConditionsEntries != null) {
                    for (ExternalDbMapEntry entry : customWhereConditionsEntries) {
                        String exp = initExpression(component, entry);
                        if (exp != null && !DbMapSqlConstants.EMPTY.equals(exp.trim())) {
                            // if (containWith(exp, DbMapSqlConstants.GROUP_BY_PATTERN, true)
                            // || containWith(exp, DbMapSqlConstants.ORDER_BY_PATTERN, true)) {
                            // byAddition.add(exp);
                            // } else if (containWith(exp, DbMapSqlConstants.GROUP_BY_PATTERN, false)
                            // || containWith(exp, DbMapSqlConstants.ORDER_BY_PATTERN, false)) {
                            // containWhereAddition.add(exp);
                            // } else
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
                this.tabSpaceString = tabString;
            }

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
                sb.append(whereClauses);
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

    @Override
    protected String getSpecialRightJoin(ExternalDbMapTable table) {
        // when use oracle's right join (+)
        if (language.getJoin(table.getJoinType()) == OracleLanguage.ORACLEJOIN.RIGHT_OUTER_JOIN_ORACLE) {
            return JOIN;
        }
        return DbMapSqlConstants.EMPTY;
    }

    @Override
    protected String getSpecialLeftJoin(ExternalDbMapTable table) {
        // when use oracle's left join (+)
        if (language.getJoin(table.getJoinType()) == OracleLanguage.ORACLEJOIN.LEFT_OUTER_JOIN_ORACLE) {
            return JOIN + DbMapSqlConstants.SPACE;
        }
        return DbMapSqlConstants.EMPTY;
    }

    @Override
    protected String addQuoteForSpecialChar(String expression, DbMapComponent component) {
        if (expression == null) {
            return expression;
        }
        List<String> specialList = new ArrayList<String>();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        if (inputConnections == null) {
            return expression;
        }
        for (IConnection iconn : inputConnections) {
            IMetadataTable metadataTable = iconn.getMetadataTable();
            if (metadataTable != null) {
                List<IMetadataColumn> lColumn = metadataTable.getListColumns();
                for (IMetadataColumn co : lColumn) {
                    String columnLabel = co.getOriginalDbColumnName();
                    String exp = MetadataToolHelper.validateValueNoLengthLimit(columnLabel);
                    if (!exp.equals(columnLabel)) {
                        specialList.add(columnLabel);
                    }
                }
            }
        }
        for (String specialColumn : specialList) {
            if (expression.contains(specialColumn)) {
                if (map.get(expression) == null) {
                    List<String> list = new ArrayList<String>();
                    list.add(specialColumn);
                    map.put(expression, list);
                } else {
                    List<String> list = map.get(expression);
                    list.add(specialColumn);
                }
            }
        }
        if (map.size() > 0) {
            List<String> list = map.get(expression);
            Collections.sort(list);
            String specialColumn = list.get(list.size() - 1);
            if (expression.contains(specialColumn)) {
                int begin = expression.indexOf(specialColumn);
                int length = specialColumn.length();
                int allLength = expression.length();
                if (specialColumn.trim().startsWith("\\\"") && specialColumn.trim().endsWith("\\\"")) {
                    return expression;
                }
                expression = expression.substring(0, begin) + "\\\"" + expression.substring(begin, begin + length) + "\\\""
                        + expression.substring(begin + length, allLength);
                return expression;
            }
        }
        return expression;
    }

    @Override
    protected boolean needAlias(List<IMetadataColumn> columns, ExternalDbMapEntry dbMapEntry, String expression) {
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(language);
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        if (tableEntriesLocationsSources.length > 1) {
            return true;
        } else {
            for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
                TableEntryLocation location = tableEntriesLocationsSource;
                String entryName = getAliasOf(dbMapEntry.getName());
                if (location != null && entryName != null && !entryName.startsWith("_") //$NON-NLS-1$
                        && !entryName.equals(location.columnName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
