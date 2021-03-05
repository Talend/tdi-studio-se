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
package org.talend.designer.abstractmap.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.IExternalData.ExternalDataType;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.core.model.process.node.IExternalMapTable;

/**
 * cli class global comment. Detailled comment
 */
public class MapDataDelegateHelper {

    private List<? extends IExternalMapTable> inputTables;

    private List<? extends IExternalMapTable> outputTables;

    private List<? extends IExternalMapTable> varsTables;

    public MapDataDelegateHelper(List<? extends IExternalMapTable> inputTables, List<? extends IExternalMapTable> outputTables,
            List<? extends IExternalMapTable> varsTables) {
        super();
        this.inputTables = inputTables;
        this.outputTables = outputTables;
        this.varsTables = varsTables;
    }

    protected List<? extends IExternalMapTable> getInputTables() {
        return this.inputTables;
    }

    protected List<? extends IExternalMapTable> getOutputTables() {
        return this.outputTables;
    }

    protected List<? extends IExternalMapTable> getVarsTables() {
        return this.varsTables;
    }

    /**
     *
     * ggu Comment method "getExpressionColumns".
     *
     * @param expression -> if null, will return the all table for type
     * @param types ->
     * @return
     */
    public Map<IExternalMapTable, List<IExternalMapEntry>> getExpressionColumns(String expression, ExternalDataType... types) {

        Map<IExternalMapTable, List<IExternalMapEntry>> tableMap = new HashMap<IExternalMapTable, List<IExternalMapEntry>>();

        if (types != null && types.length > 0) {

            for (ExternalDataType type : types) {
                switch (type) {
                case INPUT:
                    return getExpressionColumns(getInputTables(), expression);
                case OUTPUT:
                    return getExpressionColumns(getOutputTables(), expression);
                case VAR:
                    return getExpressionColumns(getVarsTables(), expression);
                default:
                }

            }
        } else {
            addAll(tableMap, getExpressionColumns(getInputTables(), expression));
            addAll(tableMap, getExpressionColumns(getOutputTables(), expression));
            addAll(tableMap, getExpressionColumns(getVarsTables(), expression));
        }
        return tableMap;
    }

    private void addAll(Map<IExternalMapTable, List<IExternalMapEntry>> targetMap,
            Map<IExternalMapTable, List<IExternalMapEntry>> sourceMap) {
        for (IExternalMapTable table : sourceMap.keySet()) {
            List<IExternalMapEntry> tlist = targetMap.get(table);
            List<IExternalMapEntry> sList = sourceMap.get(table);
            if (sList != null) {
                if (tlist == null) {
                    targetMap.put(table, sList);
                } else {
                    for (IExternalMapEntry entry : sList) {
                        if (!tlist.contains(entry)) {
                            tlist.add(entry);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * cli Comment method "getExpressionColumns".
     *
     */
    private Map<IExternalMapTable, List<IExternalMapEntry>> getExpressionColumns(List<? extends IExternalMapTable> checkedTables,
            String expression) {
        Map<IExternalMapTable, List<IExternalMapEntry>> tableMap = new HashMap<IExternalMapTable, List<IExternalMapEntry>>();
        if (checkedTables != null) {
            for (IExternalMapTable emTable : checkedTables) {
                List<? extends IExternalMapEntry> tableEntries = emTable.returnTableEntries();
                if (tableEntries != null) {
                    for (IExternalMapEntry emEntry : tableEntries) {
                        matchAndAddEntry(emTable, emEntry, expression, tableMap);
                    }
                }
            }
        }
        return tableMap;
    }

    private void matchAndAddEntry(IExternalMapTable emTable, IExternalMapEntry emEntry, String expression,
            Map<IExternalMapTable, List<IExternalMapEntry>> tableMap) {
        if (emEntry.getExpression() != null && matchExpression(expression, emEntry.getExpression()) || expression == null) {
            List<IExternalMapEntry> entryList = tableMap.get(emTable);
            if (entryList == null) {
                entryList = new ArrayList<IExternalMapEntry>();
                tableMap.put(emTable, entryList);
            }
            if (!entryList.contains(emEntry)) {
                entryList.add(emEntry);
            }
        }
    }

    private boolean matchExpression(String regex, String expression) {
        PatternCompiler compiler = new Perl5Compiler();
        try {
            Pattern pattern = compiler.compile("\\b(" + UpdateContextVariablesHelper.replaceSpecialChar(regex) + ")(\\b|\\_)"); //$NON-NLS-1$ //$NON-NLS-2$
            PatternMatcher matcher = new Perl5Matcher();
            ((Perl5Matcher) matcher).setMultiline(true);
            if (matcher.contains(expression, pattern)) {
                return true;
            }
        } catch (MalformedPatternException e) {
            //
        }
        return false;
    }
}
