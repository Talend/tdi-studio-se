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
package org.talend.designer.dbmap.utils.problems;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.MapperMain;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.converter.ExternalDataConverter;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.ProblemsManager;
import org.talend.designer.dbmap.model.table.AbstractInOutTable;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ProblemsAnalyser.java 1877 2007-02-06 17:16:43Z amaumont $
 *
 */
public class ProblemsAnalyser {

    private MapperManager mapperManager;

    private List<Problem> problems = new ArrayList<Problem>();

    /**
     * DOC amaumont ProblemsAnalyser constructor comment.
     */
    public ProblemsAnalyser(MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
    }

    public List<Problem> checkProblems(ExternalDbMapData externalData) {

        problems.clear();

        if (externalData != null) {

            List<IConnection> incomingConnections = new ArrayList<IConnection>(this.mapperManager.getComponent()
                    .getIncomingConnections());
            List<IConnection> outgoingConnections = new ArrayList<IConnection>(this.mapperManager.getComponent()
                    .getOutgoingConnections());
            ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
            MapperMain mapperMain = mapperManager.getComponent().getMapperMain();
            List<IOConnection> inputsIOConnections = mapperMain.createIOConnections(incomingConnections);
            ArrayList<InputTable> inputTables = converter.prepareInputTables(inputsIOConnections, externalData);
            List<IOConnection> outputsIOConnections = mapperMain.createIOConnections(outgoingConnections);
            ArrayList<OutputTable> outputTables = converter.prepareOutputTables(outputsIOConnections, this.mapperManager
                    .getComponent().getMetadataList(), externalData);

            List<? super AbstractInOutTable> tablesWriteMode = new ArrayList<AbstractInOutTable>();
            tablesWriteMode.addAll(inputTables);
            tablesWriteMode.addAll(outputTables);

            List<? extends AbstractInOutTable> tablesReadMode = (List<? extends AbstractInOutTable>) tablesWriteMode;

            ProblemsManager problemsManager = new ProblemsManager(mapperManager);

            ArrayList<Problem> problemsLocal = new ArrayList<Problem>();

            for (AbstractInOutTable table : tablesReadMode) {
                List<IColumnEntry> columnEntries = table.getColumnEntries();
                problemsManager.checkProblemsForAllEntries(columnEntries);
                for (IColumnEntry entry : columnEntries) {
                    List<Problem> problemsOfEntry = entry.getProblems();
                    if (problemsOfEntry != null) {
                        problemsLocal.addAll(problemsOfEntry);
                    }
                }

            }
            problems.addAll(problemsLocal);

            for (InputTable inputTable : inputTables) {
                Problem needAliasProblem = getNeedAliasProblem(mapperManager.getComponent(), inputTable.getName(),
                        inputTable.getAlias());
                if (needAliasProblem != null) {
                    problems.add(needAliasProblem);
                }
            }

        }

        return getProblems();
    }

    /**
     * DOC amaumont Comment method "getProblems".
     *
     * @return
     */
    public List<Problem> getProblems() {
        return new ArrayList<Problem>(problems);
    }

    /**
     * DOC amaumont Comment method "addProblem".
     *
     * @param problem
     */
    private void addProblem(Problem problem) {
        if (problem != null) {
            problems.add(problem);
        }
    }

    private List<Problem> checkCodeProblems(String expression) {
        return mapperManager.getProblemsManager().checkExpressionSyntax(expression);
    }

    public Problem getNeedAliasProblem(DbMapComponent component, String tableName, String alias) {
        if (alias != null && !"".equals(alias.trim())) {
            return null;
        }
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        IConnection iconn = getConnectonByName(inputConnections, tableName);
        if (iconn != null) {
            boolean inputIsELTDBMap = false;
            INode source = iconn.getSource();
            String schemaValue = "";
            String tableValue = "";
            if (source != null) {
                inputIsELTDBMap = source.isELTComponent() && source.getComponent().getName().endsWith("Map");
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
            String sourceTable = "";
            boolean hasSchema = !"".equals(schemaNoQuote);
            if (hasSchema) {
                sourceTable = schemaNoQuote + ".";
            }
            sourceTable = sourceTable + tableNoQuote;

            boolean needAlias = needAlias(schemaValue);
            if (!needAlias) {
                needAlias = needAlias(tableValue);
            }
            if (needAlias) {
                String errorMessage = Messages.getString("ProblemsAnalyser.needAlias.error1", tableName, sourceTable);
                return new Problem(null, errorMessage, ProblemStatus.WARNING);
            }
        }
        return null;
    }

    private IConnection getConnectonByName(List<IConnection> inputConnections, String metaTableName) {
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

    public boolean needAlias(String value) {
        List<Integer> quoteLocations = getQuoteLocations(value, 0);
        if (!quoteLocations.isEmpty()) {
            for (int i = -1; i < quoteLocations.size(); i = i + 2) {
                if (i < quoteLocations.size()) {
                    Integer start = i < 0 ? 0 : quoteLocations.get(i);
                    Integer end = (i + 1) >= quoteLocations.size() ? value.length() : quoteLocations.get(i + 1);
                    int indexOf = value.substring(start, end).indexOf("+");
                    if (indexOf != -1) {
                        return true;
                    }
                }
            }
        } else {
            if (value.indexOf("+") != -1) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getQuoteLocations(String text, int index) {
        List<Integer> locations = new ArrayList<Integer>();
        if (index > text.length()) {
            return locations;
        }
        int indexOf = text.indexOf("\"", index);
        if (indexOf != -1) {
            boolean isQuote = true;
            if (indexOf > 0) {
                char slash = '\\';
                char charAt = text.charAt(indexOf - 1);
                if (charAt == slash) {
                    isQuote = false;
                }
            }
            if (isQuote) {
                locations.add(indexOf);
                indexOf = indexOf + 1;
            }
            if (indexOf < text.length()) {
                locations.addAll(getQuoteLocations(text, indexOf));
            }

        }
        return locations;
    }
}
