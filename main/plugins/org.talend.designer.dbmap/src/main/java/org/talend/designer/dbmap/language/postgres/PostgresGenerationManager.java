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
package org.talend.designer.dbmap.language.postgres;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.GenericDbLanguage;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.language.generation.DbMapSqlConstants;

/**
 *
 * add for bug TDI-20733,user elt,the query is generated wrong in tPostgresqlMap
 */
public class PostgresGenerationManager extends DbGenerationManager {

    private static final String REG_SPACE = "(\\s*)";

    private static final String REG_REPLACE = "$";

    public PostgresGenerationManager() {
        super(new GenericDbLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        String query = super.buildSqlSelect(component, outputTableName);

        // fix for TDI-20733

        return query;
    }

    @Override
    protected String initExpression(DbMapComponent component, ExternalDbMapEntry dbMapEntry) {
        String expression = super.initExpression(component, dbMapEntry);
        // String expression = dbMapEntry.getExpression();
        Set<String> replacedStrings = new HashSet<String>();
        if (expression != null) {
            List<? extends IConnection> inputConnections = component.getIncomingConnections();

            ExternalDbMapData data = component.getExternalData();

            if (inputConnections == null) {
                return expression;
            }
            for (ExternalDbMapTable input : data.getInputTables()) {
                ExternalDbMapTable inputTable = input;
                IConnection connection = null;
                for (IConnection iconn : inputConnections) {
                    if (iconn.getName().equals(input.getTableName())) {
                        connection = iconn;
                        break;
                    }

                }
                if (connection == null) {
                    return expression;
                }
                INode source = connection.getSource();
                String schemaStr = "";
                String tableNameStr = "";
                if (source != null) {
                    if (isELTDBMap(source)) {
                        tableNameStr = connection.getName();
                    } else {
                        IElementParameter schema = source.getElementParameter("ELT_SCHEMA_NAME");
                        IElementParameter tableName = source.getElementParameter("ELT_TABLE_NAME");
                        if (schema != null && schema.getValue() != null) {
                            schemaStr = TalendTextUtils.removeQuotes(schema.getValue().toString());
                        }
                        if (tableName != null && tableName.getValue() != null) {
                            tableNameStr = TalendTextUtils.removeQuotes(tableName.getValue().toString());
                        }
                    }
                }
                boolean needReplaceSchema = !StringUtils.isEmpty(schemaStr) && !isVariable(schemaStr);
                boolean needReplaceTable = !StringUtils.isEmpty(tableNameStr) && !isVariable(tableNameStr);

                if (inputTable.getAlias() != null && !"".equals(inputTable.getAlias())
                        && !replacedStrings.contains(inputTable.getAlias())) {
                    expression = replaceFields4Expression(component, expression, inputTable.getAlias());
                    replacedStrings.add(inputTable.getAlias());
                } else {
                    if (needReplaceSchema && !replacedStrings.contains(schemaStr)) {
                        expression = replaceFields4Expression(component, expression, schemaStr);
                        replacedStrings.add(schemaStr);
                    }
                    if (needReplaceTable && !replacedStrings.contains(tableNameStr)) {
                        expression = replaceFields4Expression(component, expression, tableNameStr);
                        replacedStrings.add(tableNameStr);
                    }
                }
                for (IMetadataColumn co : connection.getMetadataTable().getListColumns()) {
                    String columnLabel = co.getOriginalDbColumnName();
                    if (!replacedStrings.contains(columnLabel) && expression.contains(columnLabel)) {
                        expression = replaceFields4Expression(component, expression, columnLabel);
                        replacedStrings.add(columnLabel);
                    }
                }
            }

            // for update , the sql might have output table and columns
            List<IMetadataTable> metadataList = component.getMetadataList();
            if (metadataList != null) {
                for (IMetadataTable table : metadataList) {
                    String tableName = table.getLabel();
                    expression = replaceFields4Expression(component, expression, tableName);
                    replacedStrings.add(expression);
                    for (IMetadataColumn column : table.getListColumns()) {
                        String columnLabel = column.getOriginalDbColumnName();
                        if (!replacedStrings.contains(columnLabel) && expression.contains(columnLabel)) {
                            expression = replaceFields4Expression(component, expression, columnLabel);
                            replacedStrings.add(columnLabel);
                        }
                    }
                }
            }
        }

        return expression;

    }

    private String replaceFields4Expression(DbMapComponent component, String expression, String subExpression) {
        String subExpressionWithQuote = getHandledField(component, subExpression);
        if (expression.indexOf(subExpressionWithQuote) != -1) {
            return expression;
        } else {
            return expression.replaceAll("\\b" + subExpression + "\\b", getHandledField(subExpression, true));
        }

    }

    @Override
    protected String getHandledField(DbMapComponent component, String field) {
        return getHandledField(field, false);
    }

    private String getHandledField(String field, boolean inRegx) {
        if (isVariable(field)) {
            return field;
        } else if (inRegx) {
            return "\\\\\"" + field + "\\\\\"";
        } else {
            return "\\\"" + field + "\\\"";
        }

    }

    @Override
    protected String getColumnName(IConnection conn, String name) {
        return name;
    }

    @Override
    protected void buildTableDeclaration(DbMapComponent component, StringBuilder sb, ExternalDbMapTable inputTable) {
        sb.append(getHandledTableName(component, inputTable.getTableName(), inputTable.getAlias(), true));
    }

    @Override
    protected String getAliasOf(String tableName) {
        return "\\\"" + tableName + "\\\""; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String addQuotes(String name) {
        return "\\\"" + name + "\\\""; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getHandledTableName(DbMapComponent component, String tableName, String alias) {
        return getHandledTableName(component, tableName, alias, false);
    }

    protected String getHandledTableName(DbMapComponent component, String tableName, String aliasName, boolean generateSubSql) {
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        if (inputConnections == null) {
            return tableName;
        }
        IConnection iconn = this.getConnectonByName(inputConnections, tableName);
        if (iconn == null) {
            return tableName;
        }
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
        String tableNoQuote = TalendTextUtils.removeQuotes(tableValue);
        boolean hasSchema = !"".equals(schemaNoQuote);
        StringBuffer sb = new StringBuffer();
        if (inputIsELTDBMap && generateSubSql) {
            generateSubQuery(component, sb, source, iconn, tableNoQuote, aliasName);
        } else {
            if (aliasName == null) {
                String tableAndSchema = "";
                if (hasSchema) {
                    tableAndSchema = getHandledField(component, schemaNoQuote);
                    tableAndSchema = tableAndSchema + ".";
                }
                tableAndSchema = tableAndSchema + getHandledField(component, tableNoQuote);

                if (isVariable(schemaNoQuote) || isVariable(tableNoQuote)) {
                    tableAndSchema = replaceVariablesForExpression(component, tableAndSchema);
                }
                sb.append(tableAndSchema);
            } else {
                sb.append("\\\"\"+");
                if (hasSchema) {
                    sb.append(schemaValue);
                    sb.append("+\"\\\".\\\"\"+");
                }
                sb.append(tableValue);
                sb.append("+\"\\\"");
            }
        }
        return sb.toString();
    }

    private boolean isVariable(String expression) {
        return !StringUtils.isEmpty(expression)
                && (ContextParameterUtils.isContainContextParam(expression) || parser.getGlobalMapSet(expression).size() > 0);
    }

    private void generateSubQuery(DbMapComponent component, StringBuffer sb, INode source, IConnection iconn, String tableName,
            String aliasName) {
        DbMapComponent externalNode = null;
        if (source instanceof DbMapComponent) {
            externalNode = (DbMapComponent) source;
        } else {
            externalNode = (DbMapComponent) source.getExternalNode();
        }
        DbGenerationManager genManager = externalNode.getGenerationManager();
        String deliveredTable = genManager.buildSqlSelect(externalNode, iconn.getMetadataTable().getTableName(), tabSpaceString
                + "  "); //$NON-NLS-1$
        int begin = 1;
        int end = deliveredTable.length() - 1;
        if (begin <= end) {
            sb.append("(").append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString).append("  "); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(deliveredTable.substring(begin, end)).append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString)
                    .append(" ) "); //$NON-NLS-1$
        }

        if (aliasName != null) {
            sb.append(getHandledField(component, aliasName));
        } else {
            sb.append(getHandledField(component, tableName));
        }

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
                expression = replaceContextValue(expression, context);
                haveReplace = true;
            }
        }
        if (!haveReplace) {
            List<String> connContextList = getConnectionContextList(component);
            for (String context : connContextList) {
                if (expression.contains(context)) {
                    expression = replaceContextValue(expression, context);
                }
            }
        }
        Set<String> globalMapList = getGlobalMapList(component, expression);
        for (String globalMapStr : globalMapList) {
            String replacement = globalMapStr;
            if (globalMapStr.contains("\\\\")) {
                replacement = globalMapStr.replaceAll("\\\\", "\\\\\\\\");
            }
            String regex = parser.getGlobalMapExpressionRegex(globalMapStr);
            expression = expression.replaceAll(regex, "\\\\\"\"+" + replacement + "+\"\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return expression;
    }

    private String replaceContextValue(String expression, String context) {
        if (inputSchemaContextSet.contains(context)) {
            expression = expression.replaceAll("\\b" + context + "\\b", "\\\\\"\"+" + context + "+\"\\\\\"");
        } else {
            expression = expression.replaceAll("\\b" + context + "\\b", "\" +" + context + "+ \"");
        }
        return expression;
    }
}
