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

import static java.util.Optional.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MappingTypeRetriever;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.AbstractDbLanguage;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.language.operator.IDbOperator;
import org.talend.designer.dbmap.language.operator.IDbOperatorManager;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class DbGenerationManager {

    private Map<String, ExternalDbMapTable> nameToInputTable;

    private HashMap<String, ExternalDbMapTable> nameToVarsTable;

    protected IDbLanguage language;

    protected final Set<String> aliasAlreadyDeclared = new HashSet<String>();

    protected String queryColumnsName = ""; //$NON-NLS-1$

    protected String tabSpaceString = DEFAULT_TAB_SPACE_STRING;

    private final String DOT_STR = "."; //$NON-NLS-1$

    protected static final String DEFAULT_TAB_SPACE_STRING = ""; //$NON-NLS-1$

    protected List<String> queryColumnsSegments = new ArrayList<String>();

    protected List<String> querySegments = new ArrayList<String>();

    protected DataMapExpressionParser parser;

    private Boolean useDelimitedIdentifiers;

    private Boolean useAliasInOutputTable;

    protected Set<String> subQueryTable = new HashSet<String>();

    protected INode source;

    protected Set<String> inputSchemaContextSet = new HashSet<String>();

    /**
     * DOC amaumont GenerationManager constructor comment.
     *
     * @param language2
     */
    public DbGenerationManager(IDbLanguage language) {
        super();
        this.language = language;
        this.parser = new DataMapExpressionParser(language);
    }

    /**
     * DOC amaumont Comment method "setInputTables".
     *
     * @param inputTables
     */
    public void setInputTables(List<ExternalDbMapTable> inputTables) {
        // this.inputTables = inputTables;
        nameToInputTable = new HashMap<String, ExternalDbMapTable>(inputTables.size());
        for (ExternalDbMapTable table : inputTables) {
            nameToInputTable.put(table.getName(), table);
        }
    }

    /**
     * DOC amaumont Comment method "setInputTables".
     *
     * @param varsTables
     */
    public void setVarsTables(List<ExternalDbMapTable> varsTables) {
        // this.inputTables = varsTables;
        nameToVarsTable = new HashMap<String, ExternalDbMapTable>(varsTables.size());
        for (ExternalDbMapTable table : varsTables) {
            nameToVarsTable.put(table.getName(), table);
        }
    }

    public ExternalDbMapTable getInputTable(String tableName) {
        return nameToInputTable.get(tableName);
    }

    public ExternalDbMapTable getVarsTable(String tableName) {
        return nameToVarsTable.get(tableName);
    }

    public boolean isInputTable(String tableName) {
        return getInputTable(tableName) != null;
    }

    public boolean isVarsTable(String tableName) {
        return getVarsTable(tableName) != null;
    }

    public String getTableColumnVariable(String tableName, String columnName) {
        return StringHelper.replacePrms(this.language.getTemplateTableColumnVariable(), new Object[] { tableName, columnName });
    }

    public String getGeneratedCodeTableColumnVariable(String tableName, String columnName) {
        return StringHelper.replacePrms(this.language.getTemplateGeneratedCodeTableColumnVariable(), new Object[] { tableName,
                columnName });
    }

    public String getTableColumnVariable(TableEntryLocation location) {
        return this.language.getLocation(location.tableName, location.columnName);
    }

    public String getVarsColumnVariable(String columnName) {
        return StringHelper.replacePrms(this.language.getTemplateVarsColumnVariable(), new Object[] { columnName });
    }

    public String indent(final int i) {
        return StringUtils.repeat("  ", i); //$NON-NLS-1$
    }

    public String getQueryColumnsName() {
        return this.queryColumnsName.replaceAll("[\r\n]", " ");
    }

    /**
     * DOC amaumont Comment method "ckeckConstraintsAreEmpty".
     *
     * @param ExternalDbMapTable
     * @return
     */
    public boolean checkFiltersAreEmpty(ExternalDbMapTable outputTable) {
        List<ExternalDbMapEntry> constraints = outputTable.getCustomWhereConditionsEntries();
        for (ExternalDbMapEntry whereEntry : constraints) {
            String constraintExpression = whereEntry.getExpression();
            if (constraintExpression != null && constraintExpression.trim().length() > 0) {
                return false;
            }
        }
        constraints = outputTable.getCustomOtherConditionsEntries();
        for (ExternalDbMapEntry otherEntry : constraints) {
            String constraintExpression = otherEntry.getExpression();
            if (constraintExpression != null && constraintExpression.trim().length() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for language.
     *
     * @return the language
     */
    public IDbLanguage getLanguage() {
        return this.language;
    }

    public IDbOperatorManager getOperatorsManager() {
        return this.language.getOperatorsManager();
    }

    /**
     * DOC amaumont Comment method "removeUnmatchingEntriesWithColumnsOfMetadataTable".
     *
     * @param outputTable
     * @param metadataTable
     */
    protected ExternalDbMapTable removeUnmatchingEntriesWithColumnsOfMetadataTable(ExternalDbMapTable externalDbMapTable,
            IMetadataTable metadataTable) {
        ExternalDbMapTable clonedTable = null;
        try {
            clonedTable = (ExternalDbMapTable) externalDbMapTable.clone();
        } catch (CloneNotSupportedException e) {
            ExceptionHandler.process(e);
        }

        List<ExternalDbMapEntry> metadataTableEntries = clonedTable.getMetadataTableEntries();

        if (metadataTableEntries != null) {

            HashMap<String, IMetadataColumn> hNameToColumn = new HashMap<String, IMetadataColumn>();

            List<IMetadataColumn> listColumns = metadataTable.getListColumns();
            for (IMetadataColumn column : listColumns) {
                hNameToColumn.put(column.getLabel(), column);
            }
            List<ExternalDbMapEntry> dbMapEntriesToRemove = new ArrayList<ExternalDbMapEntry>();
            for (ExternalDbMapEntry externalTableEntry : metadataTableEntries) {
                String entryName = externalTableEntry.getName();
                IMetadataColumn column = hNameToColumn.get(entryName);
                if (column == null) {
                    dbMapEntriesToRemove.add(externalTableEntry);
                }
            }
            metadataTableEntries.removeAll(dbMapEntriesToRemove);
        }
        return clonedTable;

    }

    /**
     *
     * ggu Comment method "buildSqlSelect".
     *
     * @param component
     * @param outputTableName
     * @return
     */
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        boolean checkUseUpdateStatement = checkUseUpdateStatement(component, outputTableName);
        if (checkUseUpdateStatement) {
            return buildSqlSelect(component, outputTableName, DEFAULT_TAB_SPACE_STRING, checkUseUpdateStatement);
        } else {
            return buildSqlSelect(component, outputTableName, DEFAULT_TAB_SPACE_STRING);
        }
    }

    protected String getAliasOf(String tableName) {
        return tableName;
    }

    protected String addQuotes(String name) {
        return name;
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
    public String buildSqlSelect(DbMapComponent dbMapComponent, String outputTableName, String tabString) {
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
        List<ExternalDbMapTable> inputTables = data.getInputTables();
        List<String> contextList = getContextList(component);
        collectSchemaContextParam(dbMapComponent, inputTables, contextList);
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
                    String exp = replaceVariablesForExpression(component, expression);
                    String columnSegment = exp;
                    if (i > 0) {
                        appendSqlQuery(sb, DbMapSqlConstants.COMMA);
                        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                        queryColumnsName += DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE;
                        columnSegment = DbMapSqlConstants.COMMA + DbMapSqlConstants.SPACE + columnSegment;
                    }
                    if (expression != null && expression.trim().length() > 0) {
                        appendSqlQuery(sb, exp);
                        String alias = exp;
                        if (!added && isUseAliasInOutputTable()) {
                            String name = DbMapSqlConstants.SPACE + DbMapSqlConstants.AS + DbMapSqlConstants.SPACE
                                    + getAliasOf(dbMapEntry.getName());
                            appendSqlQuery(sb, name);
                            alias = getAliasOf(dbMapEntry.getName());
                        }
                        queryColumnsName += alias;
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
                        if (!buildConditions(component, sb, inputTable, true, true, true)) {
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

    protected void collectSchemaContextParam(DbMapComponent dbMapComponent, List<ExternalDbMapTable> inputTables,
            List<String> contextList) {
        List<IConnection> incomingConnections = (List<IConnection>) dbMapComponent.getIncomingConnections();
        if (incomingConnections != null) {
            for (IConnection connection : incomingConnections) {
                INode input = connection.getSource();
                if (input != null) {
                    IElementParameter eltSchemaNameParam = input.getElementParameter("ELT_SCHEMA_NAME"); //$NON-NLS-1$
                    if (eltSchemaNameParam != null && eltSchemaNameParam.getValue() != null) {
                        String schema = String.valueOf(eltSchemaNameParam.getValue());
                        if (schema != null && !inputSchemaContextSet.contains(schema) && contextList.contains(schema)) {
                            inputSchemaContextSet.add(schema);
                        }
                    }
                    IElementParameter eltTableNameParam = input.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
                    if (eltTableNameParam != null && eltTableNameParam.getValue() != null) {
                        String table = String.valueOf(eltTableNameParam.getValue());
                        if (table != null && !inputSchemaContextSet.contains(table) && contextList.contains(table)) {
                            inputSchemaContextSet.add(table);
                        }
                    }
                }
            }
        }
        if (inputTables != null) {
            for (ExternalDbMapTable table : inputTables) {
                if (table.getAlias() != null && !inputSchemaContextSet.contains(table.getAlias())
                        && contextList.contains(table.getAlias())) {
                    inputSchemaContextSet.add(table.getAlias());
                }
            }
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
            appendSqlQuery(sb, tabSpaceString);

            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                boolean commaCouldBeAdded = i > 0;
                buildTableDeclaration(component, sb, inputTable, commaCouldBeAdded, false, false);
            }

            // where
            StringBuilder sbWhere = new StringBuilder();
            this.tabSpaceString = DEFAULT_TAB_SPACE_STRING;
            boolean isFirstClause = true;
            for (int i = 0; i < lstSizeInputTables; i++) {
                ExternalDbMapTable inputTable = inputTables.get(i);
                if (buildConditions4WhereClause(isFirstClause, component, sbWhere, inputTable, false)) {
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

    protected DbMapComponent getDbMapComponent(DbMapComponent dbMapComponent) {
        DbMapComponent component = dbMapComponent;
        INode realGraphicalNode = dbMapComponent.getRealGraphicalNode();
        if (realGraphicalNode != null) {
            IExternalNode externalNode = realGraphicalNode.getExternalNode();
            if (externalNode instanceof DbMapComponent) {
                component = (DbMapComponent) externalNode;
            }
        }
        checkParameters(component);
        return component;
    }

    protected void checkParameters(DbMapComponent component) {
        checkSpecialParameters(component);
    }

    protected boolean checkUseUpdateStatement(DbMapComponent dbMapComponent, String outputTableName) {
        List<IConnection> outputConnections = (List<IConnection>) dbMapComponent.getOutgoingConnections();
        if (outputConnections != null) {
            IConnection iconn = this.getConnectonByMetadataName(outputConnections, outputTableName);
            if (iconn != null&&iconn.getTarget()!=null) {
                source = iconn.getTarget();
                IElementParameter useUpdateStatementParam = source.getElementParameter("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
                if (useUpdateStatementParam != null && useUpdateStatementParam.isShow(source.getElementParameters())
                        && useUpdateStatementParam.getValue() != null) {
                    return Boolean.valueOf(useUpdateStatementParam.getValue().toString());
                }
            }
        }
        return false;
    }

    /**
     * This piece regulates which columns are going to be skipped in UPDATE QUERY generation in SET section if
     * <b>SET_COLUMN</b> property exists. This
     * enhancement is needed for tELTOutput component in case of <b>USE_UPDATE_STATEMENT</b>
     *
     * @return List of columns that going to be skipped, if no <b>SET_COLUMN</b> specified then return empty list.
     */
    protected List<Boolean> getSetColumnsForUpdateQuery() {
        return ofNullable(source.getElementParameter("SET_COLUMN"))
                .filter(iep -> iep.isShow(source.getElementParameters()))
                .flatMap(iep -> ofNullable((List<Map<String, ? extends Object>>)iep.getValue()))
                .orElseGet(Collections::emptyList).stream()
                .map(m -> !Boolean.valueOf(m.get("UPDATE_COLUMN").toString()))
                .collect(toList());
    }

    protected void checkSpecialParameters(DbMapComponent component) {
        /**
         * in elt related component javajets(like tELTMSSqlMap_main.javajet), they don't get DbGenerationManager by
         * DbMapComponent#getGenerationManager() while they construct a new manager manually, so some parameters may not
         * be initialised, then need to check these parameters here manually to make sure they are initialised.
         */
        if (this.useDelimitedIdentifiers == null) {
            this.useDelimitedIdentifiers = false;
            IElementParameter activeDelimitedIdentifiersEP = component
                    .getElementParameter(EParameterName.ACTIVE_DATABASE_DELIMITED_IDENTIFIERS.getName());
            if (activeDelimitedIdentifiersEP != null) {
                Object value = activeDelimitedIdentifiersEP.getValue();
                if (value != null) {
                    setUseDelimitedIdentifiers(Boolean.valueOf(value.toString()));
                }
            }
        }

        if (this.useAliasInOutputTable == null) {
            this.useAliasInOutputTable = false;
            IElementParameter useAliasInOutputTableEP = component
                    .getElementParameter(EParameterName.USE_ALIAS_IN_OUTPUT_TABLE.getName());
            if (useAliasInOutputTableEP != null) {
                Object value = useAliasInOutputTableEP.getValue();
                if (value != null) {
                    setUseAliasInOutputTable(Boolean.valueOf(value.toString()));
                }
            }
        }
    }

    /**
     *
     * DOC wchen Comment method "replaceExpression".
     *
     * @param expression
     * @param component
     */
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
        if (globalMapList.size() > 0) {
            String tempExpression = expression.trim();
            if ((tempExpression.startsWith("\"+") && tempExpression.endsWith("+\"")) //$NON-NLS-1$//$NON-NLS-2$
                    || (tempExpression.startsWith("\" +") && tempExpression.endsWith("+ \""))) {//$NON-NLS-1$ //$NON-NLS-2$
                return expression;
            }
        }
        for (String globalMapStr : globalMapList) {
            String regex = parser.getGlobalMapExpressionRegex(globalMapStr);
            String replacement = parser.getGlobalMapReplacement(globalMapStr);
            expression = expression.replaceAll(regex, "\" +" + replacement + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return expression;
    }

    protected void replaceQueryContext(List<String> querySegments, String context) {
        if (querySegments == null || querySegments.size() == 0) {
            return;
        }
        for (int i = 0; i < querySegments.size(); i++) {
            String segment = querySegments.get(i);
            if (segment.contains(context)) {
                segment = segment.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                querySegments.set(i, segment);
            }
        }
    }

    protected void replaceQueryGlobalMap(List<String> querySegments, String regex, String globalMapString) {
        for (int i = 0; i < querySegments.size(); i++) {
            String segment = querySegments.get(i);
            if (segment.contains(globalMapString)) {
                segment = segment.replaceAll(regex, "\" +" + globalMapString + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$
                querySegments.set(i, segment);
            }
        }
    }

    protected boolean needAlias(List<IMetadataColumn> columns, ExternalDbMapEntry dbMapEntry, String expression) {
        return false;
    }

    protected void appendSqlQuery(StringBuilder sb, String value) {
        appendSqlQuery(sb, value, true);
    }

    protected void appendSqlQuery(StringBuilder sb, String value, boolean addToQuerySegment) {
        if (!"".equals(value)) {//$NON-NLS-1$
            sb.append(value);
            if (addToQuerySegment) {
                querySegments.add(value);
            }
        }
    }

    public List<String> getQuerySegments() {
        return querySegments;
    }

    public List<String> getQueryColumnsSegments() {
        return queryColumnsSegments;
    }

    protected String handleQuery(String query) {
        if (query != null) {
            if (!query.trim().endsWith("\"")) { //$NON-NLS-1$
                query = query + "\""; //$NON-NLS-1$
            } else if (query.trim().endsWith("\\\"")) { //$NON-NLS-1$
                query = query + " \""; //$NON-NLS-1$
            } else {
                if (query.trim().endsWith("+ \"")) { //$NON-NLS-1$
                    if (DEFAULT_TAB_SPACE_STRING.equals(this.tabSpaceString)) {
                        query = query.substring(0, query.lastIndexOf("+ \"")); //$NON-NLS-1$
                    } else {
                        query = query + "\"";
                    }
                }
            }
        }
        return query;
    }

    public static boolean containWith(String expression, String pattern, boolean start) {
        if (expression != null) {
            expression = expression.trim();
            try {
                if (start) {
                    pattern = DbMapSqlConstants.EMPTY + '^' + pattern;
                }
                Pattern regex = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
                Matcher regexMatcher = regex.matcher(expression);
                return regexMatcher.find();
            } catch (PatternSyntaxException ex) {
                //
            }
        }
        return false;
    }

    protected List<String> getContextList(DbMapComponent component) {
        List<String> contextList = new ArrayList<String>();
        IProcess process = component.getProcess();
        IContext context = process.getContextManager().getDefaultContext();
        List<IContextParameter> paraList = context.getContextParameterList();
        for (IContextParameter para : paraList) {
            contextList.add(ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX + para.getName());
        }

        return contextList;
    }
    
    protected Set<String> getGlobalMapList(DbMapComponent component, String sqlQuery) {
        return parser.getGlobalMapSet(sqlQuery);
    }

    /**
     * DOC amaumont Comment method "buildConditions".
     *
     * @param sb
     * @param inputTable
     * @param writeForJoin TODO
     * @param isFirstClause TODO
     */
    protected boolean buildConditions(DbMapComponent component, StringBuilder sb, ExternalDbMapTable inputTable,
            boolean writeForJoin, boolean isFirstClause) {
        return buildConditions(component, sb, inputTable, writeForJoin, isFirstClause, false);
    }

    /**
     * DOC amaumont Comment method "buildConditions".
     *
     * @param sb
     * @param inputTable
     * @param writeForJoin TODO
     * @param isFirstClause TODO
     * @param isSqlQuert TODO
     */
    protected boolean buildConditions(DbMapComponent component, StringBuilder sb, ExternalDbMapTable inputTable,
            boolean writeForJoin, boolean isFirstClause, boolean isSqlQuert) {
        List<ExternalDbMapEntry> inputEntries = inputTable.getMetadataTableEntries();
        int lstSizeEntries = inputEntries.size();
        boolean atLeastOneConditionWritten = false;
        for (int j = 0; j < lstSizeEntries; j++) {
            ExternalDbMapEntry dbMapEntry = inputEntries.get(j);
            if (writeForJoin == dbMapEntry.isJoin()) {
                boolean conditionWritten = buildCondition(component, sb, inputTable, isFirstClause, dbMapEntry, !writeForJoin, isSqlQuert);
                if (conditionWritten) {
                    atLeastOneConditionWritten = true;
                }
                if (isFirstClause && conditionWritten) {
                    isFirstClause = false;
                }
            }
        }
        return atLeastOneConditionWritten;
    }

    /**
     * build conditions for update case only DOC hzhao Comment method "buildConditions".
     * 
     * @param isFirstClause
     * @param component
     * @param sb
     * @param inputTable
     * @param isSqlQuert
     * @return
     */
    protected boolean buildConditions4WhereClause(boolean isFirstClause, DbMapComponent component, StringBuilder sb,
            ExternalDbMapTable inputTable,
            boolean isSqlQuert) {
        List<ExternalDbMapEntry> inputEntries = inputTable.getMetadataTableEntries();
        int lstSizeEntries = inputEntries.size();
        boolean atLeastOneConditionWritten = false;
        for (int j = 0; j < lstSizeEntries; j++) {
            ExternalDbMapEntry dbMapEntry = inputEntries.get(j);
            boolean conditionWritten = buildCondition(component, sb, inputTable, isFirstClause, dbMapEntry, true, isSqlQuert);
            if (conditionWritten) {
                atLeastOneConditionWritten = true;
            }
            if (isFirstClause && conditionWritten) {
                isFirstClause = false;
            }
        }
        return atLeastOneConditionWritten;
    }

    /**
     * DOC amaumont Comment method "buildCondition".
     *
     * @param sbWhere
     * @param table
     * @param isFirstClause
     * @param dbMapEntry
     * @param writeCr TODO
     */
    private boolean buildCondition(DbMapComponent component, StringBuilder sbWhere, ExternalDbMapTable table,
            boolean isFirstClause, ExternalDbMapEntry dbMapEntry, boolean writeCr) {
        return buildCondition(component, sbWhere, table, isFirstClause, dbMapEntry, writeCr, false);
    }

    /**
     * DOC amaumont Comment method "buildCondition".
     *
     * @param sbWhere
     * @param table
     * @param isFirstClause
     * @param dbMapEntry
     * @param writeCr TODO
     */
    private boolean buildCondition(DbMapComponent component, StringBuilder sbWhere, ExternalDbMapTable table,
            boolean isFirstClause, ExternalDbMapEntry dbMapEntry, boolean writeCr, boolean isSqlQuery) {
        String expression = initExpression(component, dbMapEntry);
        IDbOperator dbOperator = getOperatorsManager().getOperatorFromValue(dbMapEntry.getOperator());
        boolean operatorIsSet = dbOperator != null;
        boolean expressionIsSet = expression != null && expression.trim().length() > 0;

        boolean conditionWritten = false;

        if (operatorIsSet) {

            if (writeCr) {
                appendSqlQuery(sbWhere, DbMapSqlConstants.NEW_LINE, isSqlQuery);
                appendSqlQuery(sbWhere, tabSpaceString, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
            }
            if (!isFirstClause) {
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.AND, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
            }
            String entryName = dbMapEntry.getName();
            String tableName = table.getName();
            if (table.getAlias() == null) {
                tableName = getHandledTableName(component, table.getName(), table.getAlias());
            } else {
                tableName = getHandledField(component, table.getAlias());
            }
            if (!subQueryTable.contains(tableName)) {
                entryName = getOriginalColumnName(entryName, component, table);
                entryName = getColumnName(null, entryName);
            }
            String locationInputEntry = language.getLocation(tableName, getHandledField(component, entryName));
            appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
            appendSqlQuery(sbWhere, locationInputEntry, isSqlQuery);
            appendSqlQuery(sbWhere, getSpecialRightJoin(table), isSqlQuery);

            appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
            if (operatorIsSet) {
                appendSqlQuery(sbWhere, dbOperator.getOperator(), isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
            } else if (!operatorIsSet && expressionIsSet) {
                appendSqlQuery(sbWhere, DbMapSqlConstants.LEFT_COMMENT, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
                appendSqlQuery(sbWhere, Messages.getString("DbGenerationManager.InputOperationSetMessage", entryName), isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.RIGHT_COMMENT, isSqlQuery);
            }
            if (operatorIsSet && !expressionIsSet && !dbOperator.isMonoOperand()) {
                String str = table.getName() + DbMapSqlConstants.DOT + entryName;
                appendSqlQuery(sbWhere, DbMapSqlConstants.LEFT_COMMENT, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
                appendSqlQuery(sbWhere, Messages.getString("DbGenerationManager.InputExpSetMessage", str), isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.SPACE, isSqlQuery);
                appendSqlQuery(sbWhere, DbMapSqlConstants.RIGHT_COMMENT, isSqlQuery);
            } else if (expressionIsSet) {
                String exp = replaceVariablesForExpression(component, expression);
                appendSqlQuery(sbWhere, exp, isSqlQuery);
                appendSqlQuery(sbWhere, getSpecialLeftJoin(table), isSqlQuery);
            }
            conditionWritten = true;

        }

        return conditionWritten;
    }

    protected String getSpecialRightJoin(ExternalDbMapTable table) {
        return DbMapSqlConstants.EMPTY;
    }

    protected String getSpecialLeftJoin(ExternalDbMapTable table) {
        return DbMapSqlConstants.EMPTY;
    }

    /**
     * DOC amaumont Comment method "buildFromTableDeclaration".
     *
     * @param sb
     * @param inputTable
     * @param commaCouldBeAdded
     * @param crCouldBeAdded TODO
     * @param writingInJoin TODO
     */
    protected void buildTableDeclaration(DbMapComponent component, StringBuilder sb, ExternalDbMapTable inputTable,
            boolean commaCouldBeAdded, boolean crCouldBeAdded, boolean writingInJoin) {
        appendSqlQuery(sb, DbMapSqlConstants.SPACE);
        String alias = inputTable.getAlias();
        if (alias != null) {
            List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
            IConnection iconn = null;
            if (inputConnections != null) {
                iconn = this.getConnectonByName(inputConnections, inputTable.getTableName());
            }
            boolean isELTDBMap = false;
            if (iconn != null) {
                INode source = iconn.getSource();
                isELTDBMap = isELTDBMap(source);
            }
            if (isELTDBMap) {
                buildTableDeclaration(component, sb, inputTable);
            } else if (!aliasAlreadyDeclared.contains(inputTable.getName())) {
                if (crCouldBeAdded) {
                    appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                    appendSqlQuery(sb, tabSpaceString);
                }
                if (commaCouldBeAdded) {
                    appendSqlQuery(sb, DbMapSqlConstants.COMMA);
                    appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                }
                String handledTableName = getHandledTableName(component, inputTable.getTableName(), alias);
                appendSqlQuery(sb, handledTableName);
                appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                String handledField = getHandledField(component, alias);
                appendSqlQuery(sb, handledField);
                aliasAlreadyDeclared.add(alias);
            } else {
                if (writingInJoin) {
                    appendSqlQuery(sb, getHandledTableName(component, inputTable.getName(), alias));
                }
            }
        } else {
            if (crCouldBeAdded) {
                appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                appendSqlQuery(sb, tabSpaceString);
            }
            if (commaCouldBeAdded) {
                appendSqlQuery(sb, DbMapSqlConstants.COMMA);
                appendSqlQuery(sb, DbMapSqlConstants.SPACE);
            }
            buildTableDeclaration(component, sb, inputTable);
        }
    }

    protected IConnection getConnectonByName(List<IConnection> inputConnections, String metaTableName) {
        IConnection retConnection = null;
        for (IConnection iconn : inputConnections) {
            IMetadataTable metadataTable = iconn.getMetadataTable();
            String tName = iconn.getName();
            if (tName.equals(metaTableName) && metadataTable != null) {
                retConnection = iconn;
                break;
            }
        }
        return retConnection;
    }

    protected IConnection getConnectonByMetadataName(List<IConnection> connections, String metaTableName) {
        IConnection retConnection = null;
        for (IConnection iconn : connections) {
            IMetadataTable metadataTable = iconn.getMetadataTable();
            String tName = iconn.getMetaName() != null ? iconn.getMetaName() : iconn.getUniqueName();
            if (tName.equals(metaTableName) && metadataTable != null) {
                retConnection = iconn;
                break;
            }
        }
        return retConnection;
    }

    protected void buildTableDeclaration(DbMapComponent component, StringBuilder sb, ExternalDbMapTable inputTable) {
        Object inConns = component.getIncomingConnections();

        List<IConnection> inputConnections = null;
        if (inConns != null) {
            inputConnections = (List<IConnection>) inConns;
        }
        if (inputConnections != null) {
            IConnection iconn = this.getConnectonByName(inputConnections, inputTable.getTableName());

            if (iconn == null) {
                return;
            }
            boolean replace = false;
            String inputTableName = inputTable.getName();
            IMetadataTable metadataTable = iconn.getMetadataTable();
            INode source = iconn.getSource();
            String tableName = metadataTable.getTableName();
            if (isELTDBMap(source)) {
                DbMapComponent externalNode = null;
                if (source instanceof DbMapComponent) {
                    externalNode = (DbMapComponent) source;
                } else {
                    externalNode = (DbMapComponent) source.getExternalNode();
                }
                DbGenerationManager genManager = externalNode.getGenerationManager();

                /* the new tabSpaceString in subquery must not be same with the parent!!! */
                String deliveredTable = genManager.buildSqlSelect(externalNode, tableName, tabSpaceString + "  "); //$NON-NLS-1$
                subQueryTable.add(inputTableName);
                int begin = 1;
                int end = deliveredTable.length() - 1;
                if (begin <= end) {
                    appendSqlQuery(sb, "("); //$NON-NLS-1$
                    appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                    appendSqlQuery(sb, tabSpaceString);
                    appendSqlQuery(sb, "  "); //$NON-NLS-1$

                    appendSqlQuery(sb, deliveredTable.substring(begin, end));
                    appendSqlQuery(sb, DbMapSqlConstants.NEW_LINE);
                    appendSqlQuery(sb, tabSpaceString);
                    appendSqlQuery(sb, " ) "); //$NON-NLS-1$
                }
            }
            String tableColneName = tableName;
            tableColneName = MetadataToolHelper.validateTableName(tableColneName);
            if (inputTableName.contains(".") && tableName != null) { //$NON-NLS-1$
                MapExpressionParser mapParser2 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                List<Map<String, String>> tableNameList = mapParser2.parseInTableEntryLocations(inputTableName);
                for (Map<String, String> tableNameMap : tableNameList) {
                    Set<Entry<String, String>> setTable = tableNameMap.entrySet();
                    Iterator<Entry<String, String>> iteTable = setTable.iterator();

                    while (iteTable.hasNext()) {
                        Entry<String, String> tableEntry = iteTable.next();
                        String tableLabel = tableEntry.getKey();
                        String schemaValue = tableEntry.getValue();
                        if (tableLabel.equals(metadataTable.getLabel()) && tableColneName.equals(tableLabel)) {
                            String name = schemaValue + "." + tableName; //$NON-NLS-1$
                            appendSqlQuery(sb, name);
                            replace = true;
                        }
                    }

                }
            } else if (tableName != null) {
                if (inputTableName.equals(metadataTable.getLabel()) && tableColneName.equals(inputTableName)) {
                    appendSqlQuery(sb, tableName);
                    replace = true;
                }
            }
            if (!replace) {
                String exp = replaceVariablesForExpression(component, inputTable.getName());
                appendSqlQuery(sb, exp);
            }
        }
    }

    protected static boolean isELTDBMap(INode node) {
        return node.isELTComponent()
        /**
         * The source map node using EConnectionType.TABLE wrote Database, but it wrote data to the table defined in
         * *ELTOutput component rather than the table defined in the source map node ****So****,maybe should always
         * generate subsql when input node is a map ignoring the EConnectionType
         */
        // && iconn.getLineStyle() == EConnectionType.TABLE_REF
                && node.getComponent().getName().endsWith("Map"); //$NON-NLS-1$
    }

    protected String addQuoteForSpecialChar(String expression, DbMapComponent component) {
        return expression;
    }

    protected String initExpression(DbMapComponent component, ExternalDbMapEntry dbMapEntry) {
        String quote = getQuote(component);
        String quto_mark = TalendQuoteUtils.QUOTATION_MARK;
        String expression = dbMapEntry.getExpression();
        if (expression != null) {
            List<Map<String, String>> itemNameList = null;

            // MapExpressionParser mapParser = new MapExpressionParser("((\\s*(\\w+)\\s*\\.)*)(\\w+)");
            // List<String> parseInTableEntryLocations = mapParser.parseInTableEntryLocations2(expression);
            // for (String entryLocation : parseInTableEntryLocations) {
            //
            // }

            // context.schema.context.table.column
            // context.schema.table.column
            // schema.context.table.column
            // schema.table.column
            // table.column
            // add \\w*#* for oracle 12 , schema name start with C##
            MapExpressionParser mapParser1 = new MapExpressionParser("((\\s*(\\w*#*\\w+)\\s*\\.)*)(\\w+)"); //$NON-NLS-1$
            itemNameList = mapParser1.parseInTableEntryLocations2(expression);

            if (itemNameList == null || itemNameList.isEmpty()) {
                MapExpressionParser mapParser2 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                itemNameList = mapParser2.parseInTableEntryLocations(expression);
            }

            String quto_markParser = "[\\\\]?\\" + quto_mark; //$NON-NLS-1$
            for (Map<String, String> itemNamemap : itemNameList) {
                Set<Entry<String, String>> set = itemNamemap.entrySet();
                Iterator<Entry<String, String>> ite = set.iterator();
                while (ite.hasNext()) {
                    Entry<String, String> entry = ite.next();
                    String columnValue = entry.getKey();
                    String tableValue = entry.getValue();

                    String tableNameValue = tableValue;
                    // find original table name if tableValue is alias
                    String originaltableName = tableValue;
                    ExternalDbMapData externalData = component.getExternalData();
                    final List<ExternalDbMapTable> inputTables = externalData.getInputTables();
                    for (ExternalDbMapTable inputTable : inputTables) {
                        if (inputTable.getAlias() != null && inputTable.getAlias().equals(tableValue)) {
                            originaltableName = inputTable.getTableName();
                            tableNameValue = inputTable.getAlias();
                        }
                    }

                    List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
                    if (inputConnections != null) {

                        for (IConnection iconn : inputConnections) {
                            IMetadataTable metadataTable = iconn.getMetadataTable();
                            String tName = iconn.getName();
                            if ((originaltableName.equals(tName) || tableValue.equals(tName)) && metadataTable != null) {
                                List<IMetadataColumn> lColumn = metadataTable.getListColumns();
                                String tableName = metadataTable.getTableName();
                                String tableColneName = tableName;
                                tableColneName = MetadataToolHelper.validateTableName(tableColneName);
                                if (tableValue.contains(".") && tableName != null) { //$NON-NLS-1$
                                    MapExpressionParser mapParser2 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                                    List<Map<String, String>> tableNameList = mapParser2.parseInTableEntryLocations(tableValue);

                                    for (Map<String, String> tableNameMap : tableNameList) {
                                        Set<Entry<String, String>> setTable = tableNameMap.entrySet();
                                        Iterator<Entry<String, String>> iteTable = setTable.iterator();

                                        while (iteTable.hasNext()) {
                                            Entry<String, String> tableEntry = iteTable.next();
                                            String tableLabel = tableEntry.getKey();
                                            String schemaValue = tableEntry.getValue();
                                            if (tableLabel.equals(metadataTable.getLabel()) && tableColneName.equals(tableLabel)) {
                                                tableName = tableName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$//$NON-NLS-2$
                                                expression = expression.replaceFirst(tableValue, schemaValue + "." + tableName); //$NON-NLS-1$
                                            }
                                        }

                                    }
                                } else if (tableName != null) {
                                    if (tableValue.equals(metadataTable.getLabel()) && tableColneName.equals(tableValue)) {
                                        tableName = tableName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        expression = expression.replaceFirst(tableValue, tableName);
                                    }
                                }
                                for (IMetadataColumn co : lColumn) {
                                    if (columnValue.equals(co.getLabel())) {
                                        String oriName = co.getOriginalDbColumnName();
                                        // if OriginalDbColumn is empty , still use label to generate sql
                                        if (oriName == null || "".equals(oriName)) { //$NON-NLS-1$
                                            continue;
                                        }
                                        if (expression.trim().equals(tableValue + "." + oriName)) {
                                            expression = tableValue + "." + getColumnName(iconn, oriName, quote);
                                            expression = expression.replaceAll(quto_markParser,"\\\\" +quto_mark); //$NON-NLS-1$
                                            continue;
                                        }
                                        if (expression.trim().equals(originaltableName + "." + oriName)) {
                                            expression = originaltableName + "." + getColumnName(iconn, oriName, quote);
                                            expression = expression.replaceAll(quto_markParser,"\\\\" +quto_mark); //$NON-NLS-1$
                                            continue;
                                        }
                                        // if it is temp delived table, use label to generate sql
                                        if (iconn.getLineStyle() == EConnectionType.TABLE_REF) {
                                            continue;
                                        }
                                        if (!isRefTableConnection(iconn) && isUseDelimitedIdentifiers()) {
                                            oriName = getColumnName(iconn, oriName, quote);
                                        } else {
                                            oriName = oriName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                        expression = expression.replaceFirst(tableValue + "\\." + co.getLabel(), //$NON-NLS-1$
                                                tableValue + "\\." + oriName); //$NON-NLS-1$
                                        expression = replaceAuotes(component, expression, quto_markParser, quto_mark);
                                    }
                                }

                            }
                        }
                    }
                }
            }

        }

        return expression;
    }

    protected String replaceAuotes(DbMapComponent component, String expression, String quoParser, String quote) {
        if (isComplexExpression(component, expression)) {
            return expression;
        }
        if(!expression.contains("'")){
            return expression.replaceAll(quoParser,"\\\\" +quote); //$NON-NLS-1$;
        }
        
        List<Integer> indexs = new ArrayList<>();
        for(int i=0;i<expression.length();i++){
            if("'".equals(String.valueOf(expression.charAt(i)))){
                indexs.add(i);
            }
        }
        StringBuffer result = new StringBuffer();
        int start = 0;
        for(int i=0;i<indexs.size();i++){
            if(i == 0){
                if(indexs.size() == 1 && indexs.get(i) == 0){
                    result.append(expression.substring(0, indexs.get(i)+1));
                }else{
                    String exp = expression.substring(start, indexs.get(i));
                    result.append(exp.replaceAll(quoParser,"\\\\" +quote)); //$NON-NLS-1$
                }
            }
            if(i % 2 == 0 && i != indexs.size() - 1){
                result.append(expression.substring(indexs.get(i), indexs.get(i+1)+1));
                if (i < indexs.size() - 2) {
                    String exp = expression.substring(indexs.get(i + 1) + 1, indexs.get(i + 2));
                    result.append(exp.replaceAll(quoParser,"\\\\" +quote)); //$NON-NLS-1$
                }
                start = indexs.get(i+1)+1;
            }else if(i == indexs.size() - 1){
                String exp = expression.substring(indexs.get(i)+1, expression.length());
                result.append(exp.replaceAll(quoParser,"\\\\" +quote)); //$NON-NLS-1$
            }
        }
        return result.toString();
    }
    
    protected boolean isComplexExpression(DbMapComponent component, String expression) {
        if (parser.isContainsGlobalMapExpression(expression)) {
            return true;
        }
        return false;
    }

    private String getQuote(DbMapComponent component){
        String quote = TalendQuoteUtils.QUOTATION_MARK;
        IElementParameter mappingPara = component.getElementParameter(EParameterName.MAPPING.getName());
        if(mappingPara == null){
            return quote;
        }
        String mapping = (String) mappingPara.getValue();
        if(mapping == null){
            return quote;
        }
        MappingTypeRetriever mappingTypeRetriever = MetadataTalendType.getMappingTypeRetriever(mapping);
        if (mappingTypeRetriever == null) {
            return quote;
        }
        Dbms dbms = mappingTypeRetriever.getDbms();
        String product = dbms.getProduct();
        EDatabaseTypeName type = EDatabaseTypeName.getTypeFromProductName(product);
        return TalendQuoteUtils.getQuoteByDBType(type);
    }

    private String getOriginalColumnName(String entryName, DbMapComponent component, ExternalDbMapTable table) {
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        if (inputConnections != null) {
            for (IConnection iconn : inputConnections) {
                IMetadataTable metadataTable = iconn.getMetadataTable();
                String tName = iconn.getName();
                String tableValue = table.getTableName();
                if (tableValue != null && tableValue.equals(tName) && metadataTable != null) {
                    List<IMetadataColumn> lColumn = metadataTable.getListColumns();
                    for (IMetadataColumn colu : lColumn) {
                        if (colu.getLabel().equals(entryName)) {
                            String tempName = colu.getOriginalDbColumnName();
                            if (tempName != null && tempName.length() > 0) {
                                entryName = tempName;
                                return entryName;
                            }
                        }
                    }
                }
            }
        }
        return entryName;
    }

    protected List<String> getConnectionContextList(DbMapComponent component) {
        List<String> contextList = new ArrayList<String>();
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        List<Map<String, String>> itemNameList = null;
        for (IConnection iconn : inputConnections) {
            String connName = iconn.getName();
            if (!ContextParameterUtils.containContextVariables(connName)) {
                continue;
            }

            MapExpressionParser mapParser1 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
            itemNameList = mapParser1.parseInTableEntryLocations(connName);
            if (itemNameList == null) {
                return contextList;
            }
            IMetadataTable metadataTable = iconn.getMetadataTable();
            for (Map<String, String> itemNamemap : itemNameList) {
                Set<Entry<String, String>> set = itemNamemap.entrySet();
                Iterator<Entry<String, String>> ite = set.iterator();
                while (ite.hasNext()) {
                    Entry<String, String> entry = ite.next();
                    String tableValue = entry.getKey();
                    String schemaValue = entry.getValue();
                    if (metadataTable != null && tableValue.equals(metadataTable.getLabel())) {
                        if (!contextList.contains(schemaValue)) {
                            contextList.add(schemaValue);
                        }
                    }
                }
            }

        }
        return contextList;
    }

    protected String getHandledTableName(DbMapComponent component, String tableName, String alias) {
        if (alias == null) {
            return replaceVariablesForExpression(component, tableName);
        } else {
            List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
            IConnection iconn = this.getConnectonByName(inputConnections, tableName);
            if (iconn != null) {
                String handledTableName = "";
                boolean inputIsELTDBMap = false;
                INode source = iconn.getSource();
                String schemaValue = "";
                String tableValue = "";
                if (source != null) {
                    inputIsELTDBMap = isELTDBMap(source);
                    if (inputIsELTDBMap) {
                        tableValue = iconn.getName();
                    } else {
                        IElementParameter schemaParam = source.getElementParameter("ELT_SCHEMA_NAME");
                        IElementParameter tableParam = source.getElementParameter("ELT_TABLE_NAME");
                        if (schemaParam != null && schemaParam.getValue() != null) {
                            schemaValue = schemaParam.getValue().toString();
                        }
                        if (tableParam != null && tableParam.getValue() != null) {
                            tableValue = tableParam.getValue().toString();
                        }
                    }
                }

                String schemaNoQuote = TalendTextUtils.removeQuotes(schemaValue);
                boolean hasSchema = !"".equals(schemaNoQuote);
                if (hasSchema) {
                    handledTableName = schemaValue + "+\".\"+";
                }
                handledTableName = handledTableName + tableValue;
                return "\" +" + handledTableName + "+ \"";
            }
        }
        return tableName;

    }

    protected String getColumnName(IConnection conn, String name) {
        if (!isRefTableConnection(conn) && isUseDelimitedIdentifiers()) {
            return getNameWithDelimitedIdentifier(name);
        } else {
            return name;
        }
    }

    protected String getColumnName(IConnection conn, String name, String quote) {
        if (!isRefTableConnection(conn) && isUseDelimitedIdentifiers()) {
            return getNameWithDelimitedIdentifier(name, quote);
        } else {
            return name;
        }
    }

    protected boolean isRefTableConnection(IConnection conn) {
        return conn != null && conn.getLineStyle() == EConnectionType.TABLE_REF;
    }

    protected String getNameWithDelimitedIdentifier(String name) {
        final String delimitedIdentifier = getDelimitedIdentifiers();
        String newName = name;
        newName = newName.replaceAll("\"", "\"\"");
        newName = delimitedIdentifier + newName + delimitedIdentifier;
        return newName;
    }

    protected String getNameWithDelimitedIdentifier(String name, String quote) {
        String newName = name;
        newName = newName.replaceAll("\"", "\"\"");
        newName = quote + newName + quote;
        return newName;
    }

    protected String getDelimitedIdentifiers() {
        return "\""; //$NON-NLS-1$
    }

    protected String getHandledField(DbMapComponent component, String field) {
        if (field != null) {
            List<String> contextList = getContextList(component);
            boolean haveReplace = false;
            for (String context : contextList) {
                if (field.contains(context)) {
                    field = field.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    haveReplace = true;
                    break;
                }
            }
            if (!haveReplace) {
                field = field.replace("\"", "\\\"");
            }
        }
        return field;
    }

    public boolean isUseDelimitedIdentifiers() {
        return Boolean.TRUE.equals(this.useDelimitedIdentifiers);
    }

    public void setUseDelimitedIdentifiers(boolean useDelimitedIdentifiers) {
        this.useDelimitedIdentifiers = useDelimitedIdentifiers;
    }

    public boolean isUseAliasInOutputTable() {
        return Boolean.TRUE.equals(this.useAliasInOutputTable);
    }

    public void setUseAliasInOutputTable(boolean useAliasInOutputTable) {
        this.useAliasInOutputTable = useAliasInOutputTable;
    }

    public boolean isConditionChecked(DbMapComponent component, ExternalDbMapTable inputTable) {
        List<ExternalDbMapEntry> inputEntries = inputTable.getMetadataTableEntries();
        boolean atLeastOneConditionIsChecked = false;
        for (ExternalDbMapEntry dbMapEntry : inputEntries) {
            if (dbMapEntry.isJoin()) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(dbMapEntry.getExpression())
                        && org.apache.commons.lang.StringUtils.isNotEmpty(dbMapEntry.getOperator())) {
                    atLeastOneConditionIsChecked = true;
                    break;
                }
            }
        }
        return atLeastOneConditionIsChecked;
    }

    public String buildSqlUpdate(DbMapComponent dbMapComponent, String outputTableName, String tabString) {
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
                                keyColumn = addQuotes(columnEntry) + " = " + exp;//$NON-NLS-1$
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
                        appendSqlQuery(sb, addQuotes(columnEntry) + " = " + exp); //$NON-NLS-1$
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
                                appendSqlQuery(sb, labelJoinType);
                                appendSqlQuery(sb, DbMapSqlConstants.SPACE);
                                buildTableDeclaration(component, sb, nextTable, false, true, true);
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
                IJoinType joinType = language.getJoin(inputTable.getJoinType());
                if (joinType == AbstractDbLanguage.JOIN.CROSS_JOIN) {
                    // if join type is CROSS JOIN the condition will only in where clause no matter explicit join
                    // checked or not
                    if (buildConditions4WhereClause(isFirstClause, component, sbWhere, inputTable, false)) {
                        isFirstClause = false;
                    }
                } else {
                    if (buildConditions(component, sbWhere, inputTable, false, isFirstClause, false)) {
                        isFirstClause = false;
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