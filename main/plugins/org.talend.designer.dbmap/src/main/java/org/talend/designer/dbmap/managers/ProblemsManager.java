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
package org.talend.designer.dbmap.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractConnection;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.codegen.IAloneProcessNodeConfigurer;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.language.operator.IDbOperator;
import org.talend.designer.dbmap.language.operator.IDbOperatorManager;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ProblemsManager {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    public static final String KEY_OUTPUT_EXPRESSION_EMPTY = "OUTPUT_EXPRESSION_EMPTY"; //$NON-NLS-1$

    public static final String KEY_NO_MATCHING = "KEY_NO_MATCHING"; //$NON-NLS-1$

    public static final String KEY_INPUT_EXPRESSION_EMPTY = "KEY_INPUT_EXPRESSION_EMPTY"; //$NON-NLS-1$

    public static final String KEY_OPERATOR_EMPTY = "KEY_OPERATOR_EMPTY"; //$NON-NLS-1$

    private MapperManager mapperManager;

    private ICodeProblemsChecker codeChecker;

    private ECodeLanguage codeLanguage;

    private IAloneProcessNodeConfigurer nodeConfigurer;

    /**
     * DOC amaumont ProblemsManager constructor comment.
     */
    private ProblemsManager() {
        super();
    }

    /**
     * DOC amaumont ProblemsManager constructor comment.
     *
     * @param manager
     */
    public ProblemsManager(final MapperManager mapperManager) {
        this.mapperManager = mapperManager;
        IDbLanguage currentLanguage = mapperManager.getCurrentLanguage();
        codeChecker = currentLanguage.getCodeChecker();
        this.nodeConfigurer = new IAloneProcessNodeConfigurer() {

            @Override
            public void configure(INode processNode) {

                IExternalNode mapperNode = mapperManager.getComponent();
                if (processNode.getUniqueName().equals(mapperNode.getUniqueName())) {

                    IExternalNode processExternalNode = (IExternalNode) processNode;
                    processExternalNode.setExternalData(mapperNode.getExternalData());

                    IODataComponentContainer dataComponents = mapperNode.getIODataComponents();

                    List<IODataComponent> mapperInputsDataComponent = dataComponents.getInputs();
                    HashMap<String, IMetadataTable> connectionNameToInputMetadataTable = new HashMap<String, IMetadataTable>();
                    for (IODataComponent dataComponent : mapperInputsDataComponent) {
                        connectionNameToInputMetadataTable.put(dataComponent.getConnection().getName(), dataComponent.getTable());
                    }
                    List<IConnection> processIncomingConnections = (List<IConnection>) processExternalNode
                            .getIncomingConnections();
                    for (IConnection connection : processIncomingConnections) {
                        if (connection instanceof AbstractConnection) {
                            IMetadataTable metadataTable = connectionNameToInputMetadataTable.get(connection.getName());
                            ((AbstractConnection) connection).setMetadataTable(metadataTable);
                        }
                    }

                    List<IMetadataTable> metadataListOut = new ArrayList<IMetadataTable>();

                    List<IODataComponent> mapperOutputsDataComponent = dataComponents.getOuputs();
                    HashMap<String, IMetadataTable> connectionNameToOutputMetadataTable = new HashMap<String, IMetadataTable>();
                    for (IODataComponent dataComponent : mapperOutputsDataComponent) {
                        connectionNameToOutputMetadataTable
                                .put(dataComponent.getConnection().getName(), dataComponent.getTable());
                    }
                    List<IConnection> processOutgoingConnections = (List<IConnection>) processExternalNode
                            .getOutgoingConnections();
                    for (IConnection connection : processOutgoingConnections) {
                        if (connection instanceof AbstractConnection) {
                            IMetadataTable metadataTable = connectionNameToOutputMetadataTable.get(connection.getName());
                            ((AbstractConnection) connection).setMetadataTable(metadataTable);
                            metadataListOut.add(metadataTable);
                        }
                    }
                    processExternalNode.setMetadataList(metadataListOut);

                } else {
                    // throw new IllegalArgumentException("Should be the same node..."); //$NON-NLS-1$
                }

            }

        };
    }

    /**
     *
     * Check all problems and save in cache for Java only.
     */
    public void checkProblems() {
        if (codeLanguage == ECodeLanguage.JAVA) {
            codeChecker.checkProblems(nodeConfigurer);
        }
    }

    /**
     * DOC amaumont Comment method "checkExpressionSyntax".
     *
     * @param expression
     */
    public List<Problem> checkExpressionSyntax(String expression) {
        ICodeProblemsChecker codeChecker = mapperManager.getCurrentLanguage().getCodeChecker();
        return codeChecker.checkProblemsForExpression(expression);
    }

    /**
     * DOC amaumont Comment method "checkProblemsForAllEntries".
     *
     * @param forceRefreshData TODO
     */
    public void checkProblemsForAllEntriesOfAllTables(boolean forceRefreshData) {
        List<DataMapTableView> tablesView = mapperManager.getUiManager().getInputsTablesView();
        tablesView.addAll(mapperManager.getUiManager().getVarsTablesView());
        tablesView.addAll(mapperManager.getUiManager().getOutputsTablesView());
        if (forceRefreshData) {
            mapperManager.getComponent().restoreMapperModelFromInternalData();
            checkProblems();
        }
        for (DataMapTableView view : tablesView) {
            checkProblemsForAllEntries(view, false);
        }
    }

    /**
     * DOC amaumont Comment method "processAllExpressions".
     *
     * @param forceRefreshData TODO
     */
    @SuppressWarnings("unchecked")
    public void checkProblemsForAllEntries(DataMapTableView dataMapTableView, boolean forceRefreshData) {
        if (forceRefreshData) {
            mapperManager.getComponent().restoreMapperModelFromInternalData();
            checkProblems();
        }
        List<IColumnEntry> columnsEntriesList = dataMapTableView.getDataMapTable().getColumnEntries();
        if (checkProblemsForAllEntries(columnsEntriesList)) {
            dataMapTableView.getTableViewerCreatorForColumns().getTableViewer().refresh(true);
        }
        if (dataMapTableView.getZone() == Zone.OUTPUTS) {
            List<ITableEntry> constraintEntriesList = dataMapTableView.getTableViewerCreatorForWhereFilters().getInputList();
            if (checkProblemsForAllEntries(constraintEntriesList)) {
                dataMapTableView.getTableViewerCreatorForWhereFilters().getTableViewer().refresh(true);
            }
            constraintEntriesList = dataMapTableView.getTableViewerCreatorForOtherFilters().getInputList();
            if (checkProblemsForAllEntries(constraintEntriesList)) {
                dataMapTableView.getTableViewerCreatorForOtherFilters().getTableViewer().refresh(true);
            }
        }
    }

    public boolean checkProblemsForAllEntries(List<? extends ITableEntry> entriesList) {
        boolean errorsHasChanged = false;
        for (ITableEntry entry : entriesList) {
            boolean haveProblemsBefore = entry.getProblems() != null;
            mapperManager.getProblemsManager().checkProblemsForTableEntry(entry, false);
            boolean haveProblemsAfter = entry.getProblems() != null;
            if (haveProblemsBefore != haveProblemsAfter) {
                errorsHasChanged = true;
            }
        }
        return errorsHasChanged;
    }

    public void checkProblemsForTableEntry(ITableEntry tableEntry, boolean forceRefreshData) {

        // if (forceRefreshData) {
        // mapperManager.getComponent().refreshMapperConnectorData();
        // checkProblems();
        // }

        List<Problem> problems = new ArrayList<Problem>(0);
        String expression = tableEntry.getExpression();
        if (tableEntry instanceof InputColumnTableEntry) {
            InputColumnTableEntry inputEntry = (InputColumnTableEntry) tableEntry;
            IDbOperatorManager operatorsManager = mapperManager.getCurrentLanguage().getOperatorsManager();
            IDbOperator dbOperator = operatorsManager.getOperatorFromValue(inputEntry.getOperator());

            boolean operatorIsSet = dbOperator != null;
            boolean expressionIsSet = expression != null && expression.trim().length() > 0;

            Problem problem = null;

            String errorMessage = null;
            String key = null;
            if (!operatorIsSet && expressionIsSet) {
                errorMessage = Messages.getString("ProblemsManager.operatorNotSet", //$NON-NLS-1$
                        new Object[] { inputEntry.getName() });
                key = KEY_OPERATOR_EMPTY;
                problem = new Problem(null, errorMessage, ProblemStatus.ERROR);
                problem.setKey(key);
                problems.add(problem);
            }
            if (operatorIsSet && !expressionIsSet && !dbOperator.isMonoOperand()) {
                errorMessage = Messages.getString("ProblemsManager.inputExpressionEmpty", //$NON-NLS-1$
                        new Object[] { inputEntry.getParentName(), inputEntry.getName() });
                key = KEY_INPUT_EXPRESSION_EMPTY;
                problem = new Problem(null, errorMessage, ProblemStatus.ERROR);
                problem.setKey(key);
                problems.add(problem);
            }

            Problem warningProblem = null;
            if (inputEntry.isUnmatchingEntry()) {
                InputTable inputTable = (InputTable) inputEntry.getParent();
                String message = Messages.getString("ProblemsManager.entryDoesntMatch", //$NON-NLS-1$
                        new Object[] { inputEntry.getParentName(), inputEntry.getName(), inputTable.getTableName() });
                warningProblem = new Problem(null, message, ProblemStatus.WARNING);
                warningProblem.setKey(KEY_NO_MATCHING);
                problems.add(warningProblem);
            }
        } else if (tableEntry instanceof OutputColumnTableEntry) {
            String errorMessage = null;
            Problem problem = null;
            if (expression == null || EMPTY_STRING.equals(expression.trim())) {
                errorMessage = Messages.getString("ProblemsManager.outputExpressionEmpty", //$NON-NLS-1$
                        new Object[] { tableEntry.getParentName(), tableEntry.getName() });
            }
            if (errorMessage != null) {
                problem = new Problem(null, errorMessage, ProblemStatus.ERROR);
                problem.setKey(KEY_OUTPUT_EXPRESSION_EMPTY);
                problems.add(problem);
            }

        }
        tableEntry.setProblems(problems.size() > 0 ? problems : null);

    }

}
