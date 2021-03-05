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
package org.talend.designer.mapper.language.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.model.process.BlockCode;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class GenerationManager {

    private Map<String, ExternalMapperTable> nameToInputTable;

    private HashMap<String, ExternalMapperTable> nameToVarsTable;

    protected ILanguage language;

    private List<BlockCode> clocksCodeToClose;

    /**
     * DOC amaumont GenerationManager constructor comment.
     *
     * @param language2
     */
    public GenerationManager(ILanguage language) {
        super();
        this.language = language;
    }

    /**
     * DOC amaumont Comment method "setInputTables".
     *
     * @param inputTables
     */
    public void setInputTables(List<ExternalMapperTable> inputTables) {
        // this.inputTables = inputTables;
        nameToInputTable = new HashMap<String, ExternalMapperTable>(inputTables.size());
        for (ExternalMapperTable table : inputTables) {
            nameToInputTable.put(table.getName(), table);
        }
    }

    /**
     * DOC amaumont Comment method "setInputTables".
     *
     * @param varsTables
     */
    public void setVarsTables(List<ExternalMapperTable> varsTables) {
        // this.inputTables = varsTables;
        nameToVarsTable = new HashMap<String, ExternalMapperTable>(varsTables.size());
        for (ExternalMapperTable table : varsTables) {
            nameToVarsTable.put(table.getName(), table);
        }
    }

    public ExternalMapperTable getInputTable(String tableName) {
        return nameToInputTable.get(tableName);
    }

    public ExternalMapperTable getVarsTable(String tableName) {
        return nameToVarsTable.get(tableName);
    }

    public boolean isInputTable(String tableName) {
        return getInputTable(tableName) != null;
    }

    public boolean isVarsTable(String tableName) {
        return getVarsTable(tableName) != null;
    }

    public String getTableColumnVariable(String tableName, String columnName) {
        return this.language.getLocation(tableName, columnName);
    }

    public String getGeneratedCodeTableColumnVariable(String uniqueNameComponent, String tableName, String columnName,
            boolean prefixTableNameWithComponentName) {
        String template = null;
        if (prefixTableNameWithComponentName) {
            template = this.language.getTemplateGeneratedCodeTableColumnVariableWithComponentNamePrefix();
        } else {
            template = this.language.getTemplateGeneratedCodeTableColumnVariable();
        }
        return StringHelper.replacePrms(template, new Object[] { uniqueNameComponent, tableName, columnName });
    }

    // public String getGeneratedCodeTableColumnVariable(String uniqueNameComponent, String tableName, String
    // columnName) {
    // return StringHelper.replacePrms(this.language.getTemplateGeneratedCodeTableColumnVariable(), new Object[] {
    // uniqueNameComponent, tableName, columnName });
    // }

    public String getTableColumnVariable(TableEntryLocation location) {
        return this.language.getLocation(location.tableName, location.columnName);
    }

    public String getVarsColumnVariable(String columnName) {
        return StringHelper.replacePrms(this.language.getTemplateVarsColumnVariable(), new Object[] { columnName });
    }

    public String indent(final int i) {
        return StringUtils.repeat("  ", i); //$NON-NLS-1$
    }

    /**
     * DOC amaumont Comment method "ckeckConstraintsAreEmpty".
     *
     * @param ExternalMapperTable
     * @return
     */
    public boolean checkFiltersAreEmpty(ExternalMapperTable outputTable) {
        List<ExternalMapperTableEntry> constraints = outputTable.getConstraintTableEntries();
        int lstSize = constraints.size();
        boolean oneConstraintIsNotEmpty = false;
        for (int i = 0; i < lstSize; i++) {

            String constraintExpression = ((ExternalMapperTableEntry) constraints.get(i)).getExpression();
            if (constraintExpression != null && constraintExpression.trim().length() > 0) {
                oneConstraintIsNotEmpty = true;
                break;
            }
        }
        return !oneConstraintIsNotEmpty;
    }

    /**
     * DOC amaumont Comment method "getBlocksCodeToClose".
     */
    public List<BlockCode> getBlocksCodeToClose() {
        return clocksCodeToClose;
    }

    /**
     * DOC amaumont Comment method "getBlocksCodeToClose".
     */
    public List<BlockCode> setBlocksCodeToClose(BlockCode blockCode) {
        return clocksCodeToClose;
    }

    /**
     * DOC amaumont Comment method "getBlocksCodeToClose".
     */
    public void addBlocksCodeToClose(BlockCode blockCode) {
        if (clocksCodeToClose == null) {
            clocksCodeToClose = new ArrayList<BlockCode>();
        }
        clocksCodeToClose.add(0, blockCode);
    }

}
