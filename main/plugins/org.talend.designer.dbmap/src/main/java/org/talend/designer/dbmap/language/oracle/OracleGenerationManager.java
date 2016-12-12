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
import org.talend.designer.dbmap.managers.MapperManager;
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
    public String buildSqlSelect(DbMapComponent component, String outputTableName, String tabString) {
        queryColumnsName = "\""; //$NON-NLS-1$
        aliasAlreadyDeclared.clear();
        this.tabSpaceString = tabString;

        List<IConnection> outputConnections = (List<IConnection>) component.getOutgoingConnections();

        Map<String, IConnection> nameToOutputConnection = new HashMap<String, IConnection>();
        for (IConnection connection : outputConnections) {
            nameToOutputConnection.put(connection.getUniqueName(), connection);
        }

        ExternalDbMapData data = (ExternalDbMapData) component.getExternalData();

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
            sb.append("\""); //$NON-NLS-1$
            sb.append(DbMapSqlConstants.SELECT);
            sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);

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
                    if (i > 0) {
                        sb.append(DbMapSqlConstants.COMMA);
                        sb.append(DbMapSqlConstants.SPACE);

                        queryColumnsName += DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE;
                    }
                    if (expression != null && expression.trim().length() > 0) {
                        sb.append(expression);
                        if (component.getMapperMain() == null) {
                            component.getExternalEmfData();
                        }
                        if (component.getMapperMain() != null) {
                            MapperManager mapperManager = component.getMapperMain().getMapperManager();
                            DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(
                                    mapperManager.getCurrentLanguage());
                            TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser
                                    .parseTableEntryLocations(expression);
                            boolean columnChanged = false;
                            if (tableEntriesLocationsSources.length > 1) {
                                columnChanged = true;
                            } else {
                                for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
                                    TableEntryLocation location = tableEntriesLocationsSource;
                                    String entryName = getAliasOf(dbMapEntry.getName());
                                    if (location != null && entryName != null
                                            && !entryName.startsWith("_") && !entryName.equals(location.columnName)) {//$NON-NLS-1$
                                        columnChanged = true;

                                    }
                                }
                            }
                            if (!added && columnChanged) {
                                sb.append(DbMapSqlConstants.SPACE + DbMapSqlConstants.AS + DbMapSqlConstants.SPACE
                                        + getAliasOf(dbMapEntry.getName()));
                            }
                        }
                        queryColumnsName += expression;
                    } else {
                        sb.append(DbMapSqlConstants.LEFT_COMMENT);
                        String str = outputTable.getName() + DbMapSqlConstants.DOT + dbMapEntry.getName();
                        sb.append(Messages.getString("DbGenerationManager.OuputExpSetMessage", str)); //$NON-NLS-1$
                        sb.append(DbMapSqlConstants.RIGHT_COMMENT);
                    }
                }
            }

            sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
            sb.append(DbMapSqlConstants.FROM);

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
            boolean isFirstClause = true;
            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                if (buildConditions(component, sbWhere, inputTable, false, isFirstClause)) {
                    isFirstClause = false;
                }
            }

            sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);

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
                            sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
                        }
                        sb.append(DbMapSqlConstants.SPACE);
                    }
                    String labelJoinType = joinType.getLabel();
                    sb.append(labelJoinType);
                    sb.append(DbMapSqlConstants.SPACE);
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
                        sb.append(DbMapSqlConstants.SPACE);
                        sb.append(DbMapSqlConstants.ON);
                        sb.append(DbMapSqlConstants.LEFT_BRACKET);
                        sb.append(DbMapSqlConstants.SPACE);
                        if (!buildConditions(component, sb, inputTable, true, true)) {
                            sb.append(DbMapSqlConstants.LEFT_COMMENT);
                            sb.append(DbMapSqlConstants.SPACE);
                            sb.append(Messages.getString("DbGenerationManager.conditionNotSet")); //$NON-NLS-1$
                            sb.append(DbMapSqlConstants.SPACE);
                            sb.append(DbMapSqlConstants.RIGHT_COMMENT);
                        }
                        sb.append(DbMapSqlConstants.SPACE);
                        sb.append(DbMapSqlConstants.RIGHT_BRACKET);
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
                                originalWhereAddition.add(exp);
                            } else {
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
                            otherAddition.add(exp);
                        }
                    }
                }
            }

            String whereClauses = sbWhere.toString();

            boolean whereFlag = whereClauses.trim().length() > 0;
            boolean whereAddFlag = !whereAddition.isEmpty();

            boolean whereOriginalFlag = !originalWhereAddition.isEmpty();
            if (whereFlag || whereAddFlag || whereOriginalFlag) {
                sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
                sb.append(DbMapSqlConstants.WHERE);
            }
            if (whereFlag) {
                sb.append(whereClauses);
            }
            if (whereAddFlag) {
                for (int i = 0; i < whereAddition.size(); i++) {
                    if (i == 0 && whereFlag || i > 0) {
                        sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
                        sb.append(DbMapSqlConstants.SPACE);
                        sb.append(DbMapSqlConstants.AND);
                    }
                    sb.append(DbMapSqlConstants.SPACE);
                    sb.append(whereAddition.get(i));
                }
            }
            if (whereOriginalFlag) {
                for (String s : originalWhereAddition) {
                    sb.append(DbMapSqlConstants.NEW_LINE);
                    sb.append(DbMapSqlConstants.SPACE);
                    sb.append(s);
                }
            }
            if (!otherAddition.isEmpty()) {
                sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
                for (String s : otherAddition) {
                    sb.append(s);
                    sb.append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString);
                }
            }
        }
        String sqlQuery = sb.toString();
        if (DEFAULT_TAB_SPACE_STRING.equals(tabSpaceString)) {
            List<String> contextList = getContextList(component);
            boolean haveReplace = false;
            for (String context : contextList) {
                if (sqlQuery.contains(context)) {
                    sqlQuery = sqlQuery.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    haveReplace = true;
                }
                if (queryColumnsName.contains(context)) {
                    queryColumnsName = queryColumnsName.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
            }
            if (!haveReplace) {
                List<String> connContextList = getConnectionContextList(component);
                for (String context : connContextList) {
                    if (sqlQuery.contains(context)) {
                        sqlQuery = sqlQuery.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                    if (queryColumnsName.contains(context)) {
                        queryColumnsName = queryColumnsName.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                }
            }
        }
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
                expression = expression.substring(0, begin) + "\\\"" + expression.substring(begin, begin + length) + "\\\""
                        + expression.substring(begin + length, allLength);
                return expression;
            }
        }
        return expression;
    }

}
