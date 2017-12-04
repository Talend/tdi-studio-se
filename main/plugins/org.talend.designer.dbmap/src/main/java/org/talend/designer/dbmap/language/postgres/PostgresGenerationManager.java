// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.List;

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
import org.talend.designer.dbmap.language.generation.MapExpressionParser;

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
        if (expression != null) {
            List<? extends IConnection> inputConnections = component.getIncomingConnections();

            ExternalDbMapData data = (ExternalDbMapData) component.getExternalData();

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

                for (IMetadataColumn co : connection.getMetadataTable().getListColumns()) {
                    String columnLabel = co.getOriginalDbColumnName();
                    if (columnLabel == null || "".equals(columnLabel)) {
                        columnLabel = co.getLabel();
                    }
                    String[] patternSubs = getPattenSubs(schemaStr, tableNameStr, columnLabel);
                    MapExpressionParser parser = new MapExpressionParser(patternSubs[0]);
                    expression = parser.replaceLocation(expression, patternSubs[0], patternSubs[1]);
                    if (inputTable.getAlias() != null && !"".equals(inputTable.getAlias())) {
                        patternSubs = getPattenSubs("", inputTable.getAlias(), columnLabel);
                        parser = new MapExpressionParser(patternSubs[0]);
                        expression = parser.replaceLocation(expression, patternSubs[0], patternSubs[1]);
                    }
                }
            }
        }

        List<IMetadataTable> metadataList = component.getMetadataList();
        if (metadataList != null) {
            for (IMetadataTable table : metadataList) {
                String tableName = table.getLabel();
                for (IMetadataColumn column : table.getListColumns()) {
                    String columnLabel = column.getOriginalDbColumnName();
                    if (columnLabel == null || "".equals(columnLabel)) {
                        columnLabel = column.getLabel();
                    }
                    String[] patternSubs = getPattenSubs("", tableName, columnLabel);
                    MapExpressionParser parser = new MapExpressionParser(patternSubs[0]);
                    expression = parser.replaceLocation(expression, patternSubs[0], patternSubs[1]);
                }
            }
        }
        return expression;

    }

    private String[] getPattenSubs(String schemaStr, String tableNameStr, String columnLabel) {
        StringBuffer tempExp = new StringBuffer();
        StringBuffer sub = new StringBuffer();
        int i = 1;
        if (!"".equals(schemaStr)) {
            tempExp.append(REG_SPACE + schemaStr + REG_SPACE);
            sub.append(REG_REPLACE + i++ + getHandledField(schemaStr, true) + REG_REPLACE + i++);
        }
        if (!"".equals(tableNameStr)) {
            if ("".equals(schemaStr)) {
                tempExp.append(REG_SPACE + tableNameStr + REG_SPACE);
                sub.append(REG_REPLACE + i++ + getHandledField(tableNameStr, true) + REG_REPLACE + i++);
            } else {
                tempExp.append(REG_SPACE + "\\." + REG_SPACE);
                sub.append(REG_REPLACE + i++ + "\\." + REG_REPLACE + i++);
                tempExp.append(REG_SPACE + tableNameStr + REG_SPACE);
                sub.append(REG_REPLACE + i++ + getHandledField(tableNameStr, true) + REG_REPLACE + i++);

            }
        }
        if ("".equals(tempExp.toString())) {
            tempExp.append(REG_SPACE + columnLabel + REG_SPACE);
            sub.append(REG_REPLACE + i++ + getHandledField(columnLabel, true) + REG_REPLACE + i++);
        } else {
            tempExp.append(REG_SPACE + "\\." + REG_SPACE);
            sub.append(REG_REPLACE + i++ + "\\." + REG_REPLACE + i++);
            tempExp.append(REG_SPACE + columnLabel + REG_SPACE);
            sub.append(REG_REPLACE + i++ + getHandledField(columnLabel, true) + REG_REPLACE + i++);
        }
        String exp = tempExp.toString().substring(REG_SPACE.length());
        exp = exp.toString().substring(0, exp.lastIndexOf(REG_SPACE));
        exp = "\\b" + exp + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$

        return new String[] { exp, sub.toString() };
    }

    private StringBuffer getSchemaAndTable(String schemaStr, String tableNameStr) {
        StringBuffer tempExp = new StringBuffer();
        if (!"".equals(schemaStr)) {
            tempExp.append(getHandledField(schemaStr));
        }
        if (!"".equals(tableNameStr)) {
            if ("".equals(schemaStr)) {
                tempExp.append(getHandledField(tableNameStr));
            } else {
                tempExp.append(".");
                tempExp.append(getHandledField(tableNameStr));
            }
        }
        return tempExp;
    }

    private String getHandledField(String field, boolean inRegx) {
        if (ContextParameterUtils.isContainContextParam(field)) {
            return field;
        } else if (inRegx) {
            return "\\\\\"" + field + "\\\\\"";
        } else {
            return "\\\"" + field + "\\\"";
        }

    }

    @Override
    protected String getHandledField(String field) {
        return getHandledField(field, false);
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
    protected String getHandledTableName(DbMapComponent component, String name) {
        return getHandledTableName(component, name, null, false);
    }

    protected String getHandledTableName(DbMapComponent component, String name, String aliasName, boolean generateSubSql) {
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        if (inputConnections == null) {
            return name;
        }
        for (IConnection iconn : inputConnections) {
            boolean inputIsELTDBMap = false;
            INode source = iconn.getSource();
            String schemaStr = "";
            String tableNameStr = "";
            if (source != null) {
                inputIsELTDBMap = isELTDBMap(source);
                if (inputIsELTDBMap) {
                    tableNameStr = iconn.getName();
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

            String tableName = schemaStr;
            if ("".equals(tableName)) {
                tableName = tableNameStr;
            } else {
                if (!"".equals(tableNameStr)) {
                    tableName = tableName + "." + tableNameStr;
                }
            }
            if (tableName.equals(name)) {
                StringBuffer sb = new StringBuffer();
                if (inputIsELTDBMap && generateSubSql) {
                    DbMapComponent externalNode = null;
                    if (source instanceof DbMapComponent) {
                        externalNode = (DbMapComponent) source;
                    } else {
                        externalNode = (DbMapComponent) source.getExternalNode();
                    }
                    DbGenerationManager genManager = externalNode.getGenerationManager();
                    String deliveredTable = genManager.buildSqlSelect(externalNode, iconn.getMetadataTable().getTableName(),
                            tabSpaceString + "  "); //$NON-NLS-1$
                    int begin = 1;
                    int end = deliveredTable.length() - 1;
                    if (begin <= end) {
                        sb.append("(").append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString).append("  "); //$NON-NLS-1$ //$NON-NLS-2$
                        sb.append(deliveredTable.substring(begin, end)).append(DbMapSqlConstants.NEW_LINE).append(tabSpaceString)
                                .append(" ) "); //$NON-NLS-1$
                    }
                    if (aliasName != null && !aliasName.trim().isEmpty()) {
                        tableNameStr = aliasName;
                    }
                }
                StringBuffer tempExp = getSchemaAndTable(schemaStr, tableNameStr);
                sb.append(tempExp);
                return sb.toString();
            }
        }
        return name;
    }
}
