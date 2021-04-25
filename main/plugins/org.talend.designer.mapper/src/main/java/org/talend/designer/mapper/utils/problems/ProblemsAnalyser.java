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
package org.talend.designer.mapper.utils.problems;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.mapper.MapperComponent;
import org.talend.designer.mapper.MapperMain;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.external.converter.ExternalDataConverter;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.language.generation.JavaGenerationManager;
import org.talend.designer.mapper.language.generation.JavaGenerationManager.PROBLEM_KEY_FIELD;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.IUIMatchingMode;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.TMAP_MATCHING_MODE;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
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

    public List<Problem> checkProblems(ExternalMapperData externalData) {

        problems.clear();

        if (externalData != null) {
            checkLookupTablesKeyProblems(externalData);

            ICodeProblemsChecker codeChecker = LanguageProvider.getCurrentLanguage().getCodeChecker();

            if (mapperManager.isCheckSyntaxEnabled()) {
                List<ExternalMapperTable> extInputTables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
                List<ExternalMapperTable> extVarTables = new ArrayList<ExternalMapperTable>(externalData.getVarsTables());
                List<ExternalMapperTable> extOutputTables = new ArrayList<ExternalMapperTable>(externalData.getOutputTables());
                checkExpressionSyntaxProblems(extInputTables, codeChecker);
                checkExpressionSyntaxProblems(extVarTables, codeChecker);
                checkExpressionSyntaxProblems(extOutputTables, codeChecker);

                List<? extends IConnection> incomingConnections = new ArrayList<IConnection>(this.mapperManager
                        .getAbstractMapComponent().getIncomingConnections());
                ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
                MapperMain mapperMain = ((MapperComponent) mapperManager.getAbstractMapComponent()).getMapperMain();
                ArrayList<IOConnection> inputsIOConnections = mapperMain.createIOConnections(incomingConnections);
                ArrayList<InputTable> inputTables = converter.prepareInputTables(inputsIOConnections, externalData);

                checkKeysProblems(inputTables);

                checkOutputTablesProblems(extOutputTables);
            }

        }

        return getProblems();
    }

    public List<Problem> getLookupTableProblem(InputTable inputTable, boolean atLeastOneExpressionFilled) {
        problems.clear();
        if (!atLeastOneExpressionFilled) {
            IUIMatchingMode matchingMode = inputTable.getMatchingMode();
            if (MATCHING_MODE.ALL_ROWS != matchingMode.getMatchingMode()) {
                addProblem(new Problem(null,
                        "The lookup table '" + inputTable.getName() + "' should have at least one expression key filled.", //$NON-NLS-1$ //$NON-NLS-2$
                        ProblemStatus.WARNING));
            }
        } else {
            IUIMatchingMode matchingMode = inputTable.getMatchingMode();
            if (MATCHING_MODE.ALL_ROWS == matchingMode.getMatchingMode()) {
                addProblem(new Problem(null, "The expression key can't be used in lookup table '" + inputTable.getName()
                        + "' with match mode 'All Rows'.", ProblemStatus.WARNING));
            }
        }
        return getProblems();
    }

    /**
     * DOC amaumont Comment method "checkOutputTablesProblems".
     *
     * @param extOutputTables
     */
    private void checkOutputTablesProblems(List<ExternalMapperTable> extOutputTables) {
        ArrayList<ExternalMapperTable> rejectTables = new ArrayList<ExternalMapperTable>();
        ArrayList<ExternalMapperTable> normalTables = new ArrayList<ExternalMapperTable>();
        ArrayList<ExternalMapperTable> normalTablesWithFilter = new ArrayList<ExternalMapperTable>();
        for (ExternalMapperTable outputTable : extOutputTables) {
            if (outputTable.isReject()) {
                rejectTables.add(outputTable);
            } else if (!outputTable.isRejectInnerJoin()) {
                if (outputTable.getExpressionFilter() != null && outputTable.isActivateExpressionFilter()
                        && !outputTable.getExpressionFilter().trim().equals("") //$NON-NLS-1$
                        || outputTable.getConstraintTableEntries() != null && outputTable.getConstraintTableEntries().size() > 0) {
                    normalTablesWithFilter.add(outputTable);
                } else {
                    normalTables.add(outputTable);
                }
            }
        }
        if (rejectTables.size() > 0 && normalTables.size() > 0) {
            String tables = ""; //$NON-NLS-1$
            int normalTablesListSize = normalTables.size();
            for (int i = 0; i < normalTablesListSize; i++) {
                ExternalMapperTable normalOutputTable = normalTables.get(i);
                if (i > 0) {
                    tables += ", "; //$NON-NLS-1$
                }
                tables += "\"" + normalOutputTable.getName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
            }
            String description = Messages.getString("Problem.warning.unusableReject", new Object[] { tables }); //$NON-NLS-1$
            addProblem(new Problem(null, description, ProblemStatus.WARNING));
        }

    }

    public void checkLookupTablesKeyProblems(ExternalMapperData externalData) {
        List<ExternalMapperTable> lookupTables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
        List<? extends IConnection> mainConnections = mapperManager.getAbstractMapComponent().getIncomingConnections(
                EConnectionType.FLOW_MAIN);
        if (mainConnections.size() == 1) {
            ExternalMapperTable mainTable = null;
            for (ExternalMapperTable table : lookupTables) {
                if (mainConnections.get(0).getName().equals(table.getName())) {
                    mainTable = table;
                    break;
                }
            }
            if (mainTable != null) {
                lookupTables.remove(mainTable);
            }
        }

        for (ExternalMapperTable table : lookupTables) {
            List<ExternalMapperTableEntry> columnEntries = table.getMetadataTableEntries();
            boolean atLeastOneExpressionFilled = false;
            if (columnEntries != null) {
                for (ExternalMapperTableEntry entry : columnEntries) {
                    if (!StringUtils.isEmpty(entry.getExpression())) {
                        atLeastOneExpressionFilled = true;
                        break;
                    }
                }
            }

            if (!atLeastOneExpressionFilled) {
                String matchingMode = table.getMatchingMode();
                if (!TMAP_MATCHING_MODE.ALL_ROWS.name().equals(matchingMode)) {
                    addProblem(new Problem(null,
                            "The lookup table '" + table.getName() + "' should have at least one expression key filled.", //$NON-NLS-1$ //$NON-NLS-2$
                            ProblemStatus.WARNING));
                }
            } else {
                String matchingMode = table.getMatchingMode();
                if (TMAP_MATCHING_MODE.ALL_ROWS.name().equals(matchingMode)) {
                    addProblem(new Problem(null, "The expression key can't be used in lookup table '" + table.getName()
                            + "' with match mode 'All Rows'.", ProblemStatus.WARNING));
                }
            }

        }

    }

    /**
     * DOC amaumont Comment method "checkKeysProblems".
     *
     * @param incomingConnections
     * @param inputTables
     */
    private void checkKeysProblems(ArrayList<InputTable> inputTables) {

        ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();
        if (!mapperManager.isAdvancedMap()) {
            for (InputTable table : inputTables) {
                if (table.isMainConnection()) {
                    continue;
                }
                String tableName = table.getName();
                List<IColumnEntry> columnEntries = table.getColumnEntries();
                for (IColumnEntry entry : columnEntries) {
                    InputColumnTableEntry inputEntry = (InputColumnTableEntry) entry;
                    String columnName = entry.getName();
                    if (mapperManager.checkEntryHasInvalidUncheckedKey(inputEntry)) {
                        String description = "Key of " + currentLanguage.getLocation(tableName, columnName) //$NON-NLS-1$
                                + " input entry should be checked or expression should be removed. "; //$NON-NLS-1$
                        addProblem(new Problem(null, description, ProblemStatus.WARNING));
                    }
                    if (mapperManager.checkEntryHasInvalidCheckedKey(inputEntry)) {
                        String description = "Key of " + currentLanguage.getLocation(tableName, columnName) //$NON-NLS-1$
                                + " input entry should be unchecked or expression should be filled. "; //$NON-NLS-1$
                        addProblem(new Problem(null, description, ProblemStatus.WARNING));
                    }
                }
            }
        }

    }

    /**
     * DOC amaumont Comment method "checkExpressionSyntaxProblems".
     *
     * @param tables
     * @param codeChecker
     */
    private void checkExpressionSyntaxProblems(List<ExternalMapperTable> tables, ICodeProblemsChecker codeChecker) {

        ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();
        boolean keyLanguageCheckerIsUsed = currentLanguage.getCodeLanguage() == ECodeLanguage.JAVA;

        for (ExternalMapperTable table : tables) {
            List<ExternalMapperTableEntry> metadataTableEntries = table.getMetadataTableEntries();
            // loop on all entries of current table
            if (metadataTableEntries != null) {
                checkExpressionSyntaxProblems(codeChecker, currentLanguage, keyLanguageCheckerIsUsed, table,
                        metadataTableEntries, JavaGenerationManager.PROBLEM_KEY_FIELD.METADATA_COLUMN);
            }

            List<ExternalMapperTableEntry> globalMapKeysValues = table.getGlobalMapKeysValues();
            // loop on all entries of current table
            if (globalMapKeysValues != null) {
                checkExpressionSyntaxProblems(codeChecker, currentLanguage, keyLanguageCheckerIsUsed, table, globalMapKeysValues,
                        JavaGenerationManager.PROBLEM_KEY_FIELD.GLOBAL_MAP);
            }

            if (table.getConstraintTableEntries() != null || table.isActivateExpressionFilter()
                    && table.getExpressionFilter() != null && !table.getExpressionFilter().trim().equals("")) { //$NON-NLS-1$
                String prefix = "Filter invalid in table " + table.getName() + " : "; //$NON-NLS-1$ //$NON-NLS-2$
                if (table.getConstraintTableEntries() != null) {
                    for (ExternalMapperTableEntry entry : table.getConstraintTableEntries()) {

                        checkFilterEntry(codeChecker, keyLanguageCheckerIsUsed, table, prefix, entry.getExpression());

                    }
                }
                if (table.isActivateExpressionFilter() && table.getExpressionFilter() != null
                        && !table.getExpressionFilter().trim().equals("")) { //$NON-NLS-1$
                    checkFilterEntry(codeChecker, keyLanguageCheckerIsUsed, table, prefix, table.getExpressionFilter());
                }
            }
        } // for (ExternalMapperTable table : tables) {
    }

    private void checkExpressionSyntaxProblems(ICodeProblemsChecker codeChecker, ILanguage currentLanguage,
            boolean keyLanguageCheckerIsUsed, ExternalMapperTable table, List<ExternalMapperTableEntry> entries,
            PROBLEM_KEY_FIELD problemKeyField) {
        for (ExternalMapperTableEntry entry : entries) {
            List<Problem> problems = null;
            if (keyLanguageCheckerIsUsed) {
                String key = mapperManager.buildProblemKey(problemKeyField, table.getName(), entry.getName());
                problems = codeChecker.getProblemsFromKey(key);
            } else {
                problems = checkCodeProblems(entry.getExpression());
            }
            if (problems != null) {
                String location = currentLanguage.getLocation(table.getName(), entry.getName());
                String prefix = "Expression of " + problemKeyField.getLabel() + " " + location + " is invalid : "; //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
                for (Problem problem : problems) {
                    if (!problem.getDescription().startsWith(prefix)) {
                        String description = prefix + problem.getDescription() + "."; //$NON-NLS-1$
                        problem.setDescription(description);
                    }
                    addProblem(problem);
                }
            }
        } // for (ExternalMapperTableEntry entry : metadataTableEntries) {
    }

    /**
     * DOC amaumont Comment method "checkFilterEntry".
     *
     * @param codeChecker
     * @param keyIsUsed
     * @param table
     * @param prefix
     * @param entry
     */
    private void checkFilterEntry(ICodeProblemsChecker codeChecker, boolean keyIsUsed, ExternalMapperTable table, String prefix,
            String expression) {
        List<Problem> problems = null;
        if (keyIsUsed) {
            problems = codeChecker.getProblemsFromKey(mapperManager.buildProblemKey(
                    JavaGenerationManager.PROBLEM_KEY_FIELD.FILTER, table.getName(), null));
        } else {
            problems = checkCodeProblems(expression);
        }

        if (problems != null) {
            for (Problem problem : problems) {
                if (!problem.getDescription().startsWith(prefix)) {
                    String description = prefix + problem.getDescription() + "."; //$NON-NLS-1$
                    problem.setDescription(description);
                }
                addProblem(problem);
            }
        }
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
