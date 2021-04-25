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
package org.talend.designer.dbmap.model.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IProcess;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: OutputTable.java 1795 2007-02-04 04:15:56Z bqian $
 *
 */
public class OutputTable extends AbstractInOutTable {

    protected List<FilterTableEntry> whereFilterTableEntries = new ArrayList<FilterTableEntry>(0);

    protected List<FilterTableEntry> otherFilterTableEntries = new ArrayList<FilterTableEntry>(0);

    private ExtendedTableModel<FilterTableEntry> tableWhereFiltersEntriesModel;

    private ExtendedTableModel<FilterTableEntry> tableOtherFiltersEntriesModel;

    private String uniqueName;

    private String tableName;

    public OutputTable(MapperManager mapperManager, IMetadataTable metadataTable, String uniqueName, String tableName) {
        super(mapperManager, metadataTable, uniqueName);
        if (uniqueName == null) {
            final IProcess process = mapperManager.getComponent().getProcess();
            this.uniqueName = process.generateUniqueConnectionName("table"); //$NON-NLS-1$
            process.addUniqueConnectionName(this.uniqueName);
        } else {
            this.uniqueName = uniqueName;
        }
        this.tableName = tableName;
        this.tableWhereFiltersEntriesModel = new ExtendedTableModel<FilterTableEntry>(
                uniqueName + " : model for where Filters", whereFilterTableEntries); //$NON-NLS-1$

        this.tableOtherFiltersEntriesModel = new ExtendedTableModel<FilterTableEntry>(
                uniqueName + " : model for other Filters", otherFilterTableEntries); //$NON-NLS-1$
    }

    @Override
    public void initFromExternalData(ExternalDbMapTable externalMapperTable) {
        super.initFromExternalData(externalMapperTable);
        List<IMetadataColumn> columns = this.metadataTable.getListColumns();
        Map<String, ExternalDbMapEntry> nameToPerTabEntry = new HashMap<String, ExternalDbMapEntry>();
        if (externalMapperTable != null && externalMapperTable.getMetadataTableEntries() != null) {
            for (ExternalDbMapEntry perTableEntry : externalMapperTable.getMetadataTableEntries()) {
                nameToPerTabEntry.put(perTableEntry.getName(), perTableEntry);
            }
        }

        for (IMetadataColumn column : columns) {
            AbstractInOutTableEntry columnEntry = getNewTableEntry(column);
            ExternalDbMapEntry externalMapperTableEntry = nameToPerTabEntry.get(columnEntry.getMetadataColumn().getLabel());
            // Entry match with current column
            if (externalMapperTableEntry != null) {
                columnEntry.setExpression(externalMapperTableEntry.getExpression());
            }
            dataMapTableEntries.add(columnEntry);
        }
        if (externalMapperTable != null) {
            List<ExternalDbMapEntry> externalWhereConstraintTableEntries = externalMapperTable.getCustomWhereConditionsEntries();
            if (externalWhereConstraintTableEntries != null) {
                for (ExternalDbMapEntry entry : externalWhereConstraintTableEntries) {
                    FilterTableEntry whereFilterTableEntry = new FilterTableEntry(this, entry.getName(), entry.getExpression(),
                            FilterTableEntry.WHERE_FILTER);
                    // mapperManager.getProblemsManager().checkProblemsForTableEntry(filterTableEntry, false);
                    addWhereFilterEntry(whereFilterTableEntry);
                }
            }
            List<ExternalDbMapEntry> externalOtherConstraintTableEntries = externalMapperTable.getCustomOtherConditionsEntries();
            if (externalOtherConstraintTableEntries != null) {
                for (ExternalDbMapEntry entry : externalOtherConstraintTableEntries) {
                    FilterTableEntry otherFilterTableEntry = new FilterTableEntry(this, entry.getName(), entry.getExpression(),
                            FilterTableEntry.OTHER_FILTER);
                    // mapperManager.getProblemsManager().checkProblemsForTableEntry(filterTableEntry, false);
                    addOtherFilterEntry(otherFilterTableEntry);
                }
            }
        }
    }

    @Override
    protected AbstractInOutTableEntry getNewTableEntry(IMetadataColumn metadataColumn) {
        return new OutputColumnTableEntry(this, metadataColumn);
    }

    public void addWhereFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableWhereFiltersEntriesModel.add(constraintTableEntry);
    }

    public void addOtherFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableOtherFiltersEntriesModel.add(constraintTableEntry);
    }

    public void addWhereFilterEntry(FilterTableEntry constraintTableEntry, Integer index) {
        this.tableWhereFiltersEntriesModel.add(constraintTableEntry, index);
    }

    public void addOtherFilterEntry(FilterTableEntry constraintTableEntry, Integer index) {
        this.tableOtherFiltersEntriesModel.add(constraintTableEntry, index);
    }

    public void removeWhereFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableWhereFiltersEntriesModel.remove(constraintTableEntry);
    }

    public void removeOtherFilterEntry(FilterTableEntry constraintTableEntry) {
        this.tableOtherFiltersEntriesModel.remove(constraintTableEntry);
    }

    public List<FilterTableEntry> getWhereFilterEntries() {
        return this.tableWhereFiltersEntriesModel.getBeansList();
    }

    public List<FilterTableEntry> getOtherFilterEntries() {
        return this.tableOtherFiltersEntriesModel.getBeansList();
    }

    /**
     * Getter for tableWhereFiltersEntriesModel.
     *
     * @return the tableWhereFiltersEntriesModel
     */
    public ExtendedTableModel<FilterTableEntry> getWhereTableFiltersEntriesModel() {
        return this.tableWhereFiltersEntriesModel;
    }

    /**
     * Getter for tableOtherFiltersEntriesModel.
     *
     * @return the tableOtherFiltersEntriesModel
     */
    public ExtendedTableModel<FilterTableEntry> getOtherTableFiltersEntriesModel() {
        return this.tableOtherFiltersEntriesModel;
    }

    /**
     * Getter for uniqueName.
     *
     * @return the uniqueName
     */
    public String getUniqueName() {
        return this.uniqueName;
    }

    /**
     * Getter for tableName.
     *
     * @return the tableName
     */
    public String getTableName() {
        return this.tableName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.table.IDataMapTable#getName()
     */
    @Override
    public String getName() {
        return getUniqueName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.table.IDataMapTable#getTitle()
     */
    @Override
    public String getTitle() {
        return getTableName() + " (" + getUniqueName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
