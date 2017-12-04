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
package org.talend.designer.dbmap.utils.problems;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.Problem;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.dbmap.MapperMain;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.converter.ExternalDataConverter;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
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
            ArrayList<OutputTable> outputTables = converter.prepareOutputTables(outputsIOConnections,
                    this.mapperManager.getComponent().getMetadataList(), externalData);

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

}
