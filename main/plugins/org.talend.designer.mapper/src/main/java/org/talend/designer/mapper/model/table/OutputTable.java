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
package org.talend.designer.mapper.model.table;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.language.generation.JavaGenerationManager;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class OutputTable extends AbstractInOutTable {

    protected List<FilterTableEntry> filterTableEntries = new ArrayList<FilterTableEntry>(0);

    private boolean reject;

    private boolean rejectInnerJoin;

    private String isJoinTableOf;

    private boolean isErrorRejectTable;

    private ExtendedTableModel<FilterTableEntry> tableFiltersEntriesModel;

    public OutputTable(MapperManager mapperManager, IMetadataTable metadataTable, String name) {
        super(mapperManager, metadataTable, name);
        this.tableFiltersEntriesModel = new ExtendedTableModel<FilterTableEntry>(
                name + " : model for Filters", filterTableEntries); //$NON-NLS-1$
    }

    public OutputTable(MapperManager mapperManager, IMetadataTable metadataTable, IOConnection connection, String name) {
        super(mapperManager, metadataTable, connection, name);
        this.tableFiltersEntriesModel = new ExtendedTableModel<FilterTableEntry>(
                name + " : model for Filters", filterTableEntries); //$NON-NLS-1$
    }

    public void initFromExternalData(ExternalMapperTable externalMapperTable) {
        super.initFromExternalData(externalMapperTable);
        if (externalMapperTable != null) {
            this.reject = externalMapperTable.isReject();
            this.rejectInnerJoin = externalMapperTable.isRejectInnerJoin();
            this.isJoinTableOf = externalMapperTable.getIsJoinTableOf();
            this.isErrorRejectTable = externalMapperTable.isErrorRejectTable();
            List<ExternalMapperTableEntry> externalConstraintTableEntries = externalMapperTable.getConstraintTableEntries();
            if (externalConstraintTableEntries != null) {
                if (!mapperManager.isAdvancedMap()) {
                    for (ExternalMapperTableEntry entry : externalConstraintTableEntries) {
                        FilterTableEntry filterTableEntry = new FilterTableEntry(this, entry.getName(), entry.getExpression());
                        // mapperManager.getProblemsManager().checkProblemsForTableEntry(filterTableEntry, false);
                        addFilterEntry(filterTableEntry);
                    }
                } else {
                    ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();
                    JavaGenerationManager javaGenerationManager = new JavaGenerationManager(currentLanguage);
                    DataMapExpressionParser expressionParser = new DataMapExpressionParser(currentLanguage);
                    String expressionFilter = javaGenerationManager.buildConditions(externalConstraintTableEntries,
                            expressionParser);
                    if (!expressionFilter.trim().equals("")) { //$NON-NLS-1$
                        this.getExpressionFilter().setExpression(expressionFilter);
                        this.setActivateExpressionFilter(true);
                    }
                }

            }
        }
    }

    @Override
    protected AbstractInOutTableEntry getNewTableEntry(IMetadataColumn metadataColumn) {
        return new OutputColumnTableEntry(this, metadataColumn);
    }

    public boolean isReject() {
        return this.reject;
    }

    public void setReject(boolean reject) {
        this.reject = reject;
    }

    public void addFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableFiltersEntriesModel.add(constraintTableEntry);
    }

    public void addFilterEntry(FilterTableEntry constraintTableEntry, Integer index) {
        this.tableFiltersEntriesModel.add(constraintTableEntry, index);
    }

    public void removeFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableFiltersEntriesModel.remove(constraintTableEntry);
    }

    public List<FilterTableEntry> getFilterEntries() {
        return this.tableFiltersEntriesModel.getBeansList();
    }

    /**
     * Getter for rejectInnerJoin.
     *
     * @return the rejectInnerJoin
     */
    public boolean isRejectInnerJoin() {
        return this.rejectInnerJoin;
    }

    /**
     * Sets the rejectInnerJoin.
     *
     * @param rejectInnerJoin the rejectInnerJoin to set
     */
    public void setRejectInnerJoin(boolean rejectInnerJoin) {
        this.rejectInnerJoin = rejectInnerJoin;
    }

    /**
     * Getter for tableFiltersEntriesModel.
     *
     * @return the tableFiltersEntriesModel
     */
    public ExtendedTableModel<FilterTableEntry> getTableFiltersEntriesModel() {
        return this.tableFiltersEntriesModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.table.AbstractInOutTable#hasReadOnlyMetadataColumns()
     */
    @Override
    public boolean hasReadOnlyMetadataColumns() {
        boolean hasReadOnlyMetadataColumns = false;

        IOConnection connection = getConnection();

        if (connection != null) {
            INode target = connection.getTarget();
            if (target != null) {
                hasReadOnlyMetadataColumns = connection.isReadOnly() || target.isReadOnly() || connection.getTable().isReadOnly();

                if (!hasReadOnlyMetadataColumns) {
                    for (IElementParameter param : target.getElementParameters()) {
                        if (param.getFieldType() == EParameterFieldType.SCHEMA_TYPE) {
                            if (param.isReadOnly()) {
                                hasReadOnlyMetadataColumns = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return hasReadOnlyMetadataColumns;
    }

    public String getIsJoinTableOf() {
        return this.isJoinTableOf;
    }

    public void setIsJoinTableOf(String isJoinTableOf) {
        this.isJoinTableOf = isJoinTableOf;
    }

    public boolean isErrorRejectTable() {
        return isErrorRejectTable;

    }

    public void setErrorRejectTable(boolean isErrorRejectTable) {
        this.isErrorRejectTable = isErrorRejectTable;
    }

}
