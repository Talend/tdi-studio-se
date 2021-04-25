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
package org.talend.designer.mapper.external.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.node.AbstractExternalMapTable;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ExternalMapperTable extends AbstractExternalMapTable implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 8768004929161302382L;

    /**
     * <code>constraintTableEntries</code> has been replaced by expressionFilter since 2.1.0 r4515
     * <code>constraintTableEntries</code> can't be removed to keep the ascendant compatibility.
     */
    private List<ExternalMapperTableEntry> constraintTableEntries;

    private List<ExternalMapperTableEntry> metadataTableEntries;

    private List<ExternalMapperTableEntry> globalMapKeysValues;

    private static final String ERROR_REJECT_MESSAGE = "errorMessage";//$NON-NLS-1$

    private static final String ERROR_REJECT_STACK_TRACE = "errorStackTrace";//$NON-NLS-1$

    private String name;

    private boolean minimized;

    private String sizeState;

    /**
     * Used only for outputs.
     */
    private boolean reject;

    /**
     * Used only for outputs.
     */
    private boolean rejectInnerJoin;

    /**
     * Used only for output errorReject.
     */
    private boolean isErrorRejectTable;

    /**
     * Used only for outputs.
     */
    private String isJoinTableOf;

    private String expressionFilter;

    private boolean activateExpressionFilter;

    private boolean activateCondensedTool;

    private boolean activateColumnNameFilter;

    private String columnNameFilter;

    private String matchingMode;

    private String lookupMode;

    /**
     * Used only for lookup inputs. Reject main row if this lookup row doesn't exist.
     */
    private boolean innerJoin;

    /**
     * Used only for lookup inputs. Reject main row if this lookup row doesn't exist.
     */
    private boolean persistent;

    /**
     * Used for saving the schema id of table.
     */
    private String id;

    public ExternalMapperTable() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String medataTableName) {
        this.name = medataTableName;
    }

    @Override
    public boolean isMinimized() {
        return this.minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public List<ExternalMapperTableEntry> getMetadataTableEntries() {
        return this.metadataTableEntries;
    }

    public List<? extends IExternalMapEntry> returnTableEntries() {
        return getMetadataTableEntries();
    }

    public void setMetadataTableEntries(List<ExternalMapperTableEntry> tableEntries) {
        this.metadataTableEntries = tableEntries;
    }

    /**
     * Getter for globalMapKeyValues.
     *
     * @return the globalMapKeyValues
     */
    public List<ExternalMapperTableEntry> getGlobalMapKeysValues() {
        return globalMapKeysValues;
    }

    /**
     * Sets the globalMapKeyValues.
     *
     * @param globalMapKeyValues the globalMapKeyValues to set
     */
    public void setGlobalMapKeysValues(List<ExternalMapperTableEntry> globalMapKeyValues) {
        this.globalMapKeysValues = globalMapKeyValues;
    }

    /**
     *
     * DOC amaumont Comment method "getConstraintTableEntries". <code>constraintTableEntries</code> has been replaced by
     * expressionFilter since 2.1.0 r4515 <code>constraintTableEntries</code> can't be removed to keep the ascendant
     * compatibility.
     *
     * @return
     */
    public List<ExternalMapperTableEntry> getConstraintTableEntries() {
        return this.constraintTableEntries;
    }

    /**
     *
     * DOC amaumont Comment method "setConstraintTableEntries". <code>constraintTableEntries</code> has been replaced by
     * expressionFilter since 2.1.0 r4515 <code>constraintTableEntries</code> can't be removed to keep the ascendant
     * compatibility.
     *
     * @param constraintTableEntries
     */
    public void setConstraintTableEntries(List<ExternalMapperTableEntry> constraintTableEntries) {
        this.constraintTableEntries = constraintTableEntries;
    }

    /**
     *
     * used only for outputs.
     *
     * @return
     */
    public boolean isReject() {
        return this.reject;
    }

    /**
     *
     * used only for outputs.
     *
     * @param reject
     */
    public void setReject(boolean reject) {
        this.reject = reject;
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
     * Getter for innerJoin.
     *
     * @return the innerJoin
     */
    public boolean isInnerJoin() {
        return this.innerJoin;
    }

    /**
     * Sets the innerJoin.
     *
     * @param innerJoin the innerJoin to set
     */
    public void setInnerJoin(boolean innerJoin) {
        this.innerJoin = innerJoin;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @Override
    public String getExpressionFilter() {
        return this.expressionFilter;
    }

    public void setExpressionFilter(String expressionFilter) {
        this.expressionFilter = expressionFilter;
    }

    /**
     * Getter for activateExpressionFilter.
     *
     * @return the activateExpressionFilter
     */
    @Override
    public boolean isActivateExpressionFilter() {
        return this.activateExpressionFilter;
    }

    /**
     * Sets the activateExpressionFilter.
     *
     * @param activateExpressionFilter the activateExpressionFilter to set
     */
    public void setActivateExpressionFilter(boolean activateExpressionFilter) {
        this.activateExpressionFilter = activateExpressionFilter;
    }

    /**
     * Getter for lookupType.
     *
     * @return the lookupType
     */
    public String getMatchingMode() {
        return this.matchingMode;
    }

    /**
     * Sets the lookupType.
     *
     * @param matchingMode the lookupType to set
     */
    public void setMatchingMode(String matchingMode) {
        this.matchingMode = matchingMode;
    }

    /**
     * Getter for lookupMode.
     *
     * @return the lookupMode
     */
    public String getLookupMode() {
        return lookupMode;
    }

    /**
     * Sets the lookupMode.
     *
     * @param lookupMode the lookupMode to set
     */
    public void setLookupMode(String lookupMode) {
        this.lookupMode = lookupMode;
    }

    /**
     * Getter for tableSizeState.
     *
     * @return the tableSizeState
     */
    public String getSizeState() {
        return this.sizeState;
    }

    /**
     * Sets the tableSizeState.
     *
     * @param tableSizeState the tableSizeState to set
     */
    public void setSizeState(String tableSizeState) {
        this.sizeState = tableSizeState;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        ExternalMapperTable cloned = (ExternalMapperTable) super.clone();
        if (constraintTableEntries != null) {
            cloned.constraintTableEntries = (List<ExternalMapperTableEntry>) ((ArrayList) constraintTableEntries).clone();
            int listSizeconstraintTableEntries = constraintTableEntries.size();
            for (int i = 0; i < listSizeconstraintTableEntries; i++) {
                cloned.constraintTableEntries.set(i, (ExternalMapperTableEntry) cloned.constraintTableEntries.get(i).clone());
            }
        }
        if (metadataTableEntries != null) {
            cloned.metadataTableEntries = (List<ExternalMapperTableEntry>) ((ArrayList) metadataTableEntries).clone();
            int listSizemetadataTableEntries = metadataTableEntries.size();
            for (int i = 0; i < listSizemetadataTableEntries; i++) {
                cloned.metadataTableEntries.set(i, (ExternalMapperTableEntry) cloned.metadataTableEntries.get(i).clone());

            }
        }
        return cloned;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.activateExpressionFilter ? 1231 : 1237);
        result = prime * result + ((this.constraintTableEntries == null) ? 0 : this.constraintTableEntries.hashCode());
        result = prime * result + ((this.expressionFilter == null) ? 0 : this.expressionFilter.hashCode());
        result = prime * result + ((this.columnNameFilter == null) ? 0 : this.columnNameFilter.hashCode());
        result = prime * result + (this.innerJoin ? 1231 : 1237);
        result = prime * result + (this.persistent ? 1231 : 1237);
        result = prime * result + ((this.matchingMode == null) ? 0 : this.matchingMode.hashCode());
        result = prime * result + ((this.lookupMode == null) ? 0 : this.lookupMode.hashCode());
        result = prime * result + ((this.metadataTableEntries == null) ? 0 : this.metadataTableEntries.hashCode());
        result = prime * result + (this.minimized ? 1231 : 1237);
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + (this.reject ? 1231 : 1237);
        result = prime * result + (this.rejectInnerJoin ? 1231 : 1237);
        result = prime * result + ((this.sizeState == null) ? 0 : this.sizeState.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExternalMapperTable other = (ExternalMapperTable) obj;
        if (this.activateExpressionFilter != other.activateExpressionFilter) {
            return false;
        }

        if (this.expressionFilter == null) {
            if (other.expressionFilter != null) {
                return false;
            }
        } else if (!this.expressionFilter.equals(other.expressionFilter)) {
            return false;
        }
        if (this.innerJoin != other.innerJoin) {
            return false;
        }
        if (this.matchingMode == null) {
            if (other.matchingMode != null) {
                return false;
            }
        } else if (!this.matchingMode.equals(other.matchingMode)) {
            return false;
        }
        if (this.lookupMode == null) {
            if (other.lookupMode != null) {
                return false;
            }
        } else if (!this.lookupMode.equals(other.lookupMode)) {
            return false;
        }

        if (this.minimized != other.minimized) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.reject != other.reject) {
            return false;
        }
        if (this.rejectInnerJoin != other.rejectInnerJoin) {
            return false;
        }
        if (this.persistent != other.persistent) {
            return false;
        }
        if (this.sizeState == null) {
            if (other.sizeState != null) {
                return false;
            }
        } else if (!this.sizeState.equals(other.sizeState)) {
            return false;
        }
        if (this.activateCondensedTool != other.activateCondensedTool) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.columnNameFilter == null) {
            if (other.columnNameFilter != null) {
                return false;
            }
        } else if (!this.columnNameFilter.equals(other.columnNameFilter)) {
            return false;
        }

        List<ExternalMapperTableEntry> this_constraintTableEntries = null;
        if (this.constraintTableEntries == null) {
            this_constraintTableEntries = new ArrayList<ExternalMapperTableEntry>();
        } else {
            this_constraintTableEntries = new ArrayList<ExternalMapperTableEntry>(this.constraintTableEntries);
        }
        List<ExternalMapperTableEntry> this_metadataTableEntries = null;
        if (this.metadataTableEntries == null) {
            this_metadataTableEntries = new ArrayList<ExternalMapperTableEntry>();
        } else {
            this_metadataTableEntries = new ArrayList<ExternalMapperTableEntry>(this.metadataTableEntries);
        }
        List<ExternalMapperTableEntry> this_globalMapKeysValues = null;
        if (this.globalMapKeysValues == null) {
            this_globalMapKeysValues = new ArrayList<ExternalMapperTableEntry>();
        } else {
            this_globalMapKeysValues = new ArrayList<ExternalMapperTableEntry>(this.globalMapKeysValues);
        }

        List<ExternalMapperTableEntry> other_constraintTableEntries = null;
        if (other.constraintTableEntries == null) {
            other_constraintTableEntries = new ArrayList<ExternalMapperTableEntry>();
        } else {
            other_constraintTableEntries = new ArrayList<ExternalMapperTableEntry>(other.constraintTableEntries);
        }
        List<ExternalMapperTableEntry> other_metadataTableEntries = null;
        if (other.metadataTableEntries == null) {
            other_metadataTableEntries = new ArrayList<ExternalMapperTableEntry>();
        } else {
            other_metadataTableEntries = new ArrayList<ExternalMapperTableEntry>(other.metadataTableEntries);
        }
        List<ExternalMapperTableEntry> other_globalMapKeysValues = null;
        if (other.globalMapKeysValues == null) {
            other_globalMapKeysValues = new ArrayList<ExternalMapperTableEntry>();
        } else {
            other_globalMapKeysValues = new ArrayList<ExternalMapperTableEntry>(other.globalMapKeysValues);
        }

        if (this_constraintTableEntries.size() != other_constraintTableEntries.size()) {
            return false;
        }
        for (ExternalMapperTableEntry oriObj : this_constraintTableEntries) {
            boolean found = false;
            for (ExternalMapperTableEntry otherObj : other_constraintTableEntries) {
                if (oriObj.getName().equals(otherObj.getName())) {
                    found = true;
                    if (!oriObj.equals(otherObj)) {
                        return false;
                    }
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }

        if (this_metadataTableEntries.size() != other_metadataTableEntries.size()) {
            return false;
        }
        for (ExternalMapperTableEntry oriObj : this_metadataTableEntries) {
            boolean found = false;
            for (ExternalMapperTableEntry otherObj : other_metadataTableEntries) {
                if (oriObj.getName().equals(otherObj.getName())) {
                    found = true;
                    if (!oriObj.equals(otherObj)) {
                        return false;
                    }
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }

        if (this_globalMapKeysValues.size() != other_globalMapKeysValues.size()) {
            return false;
        }
        for (ExternalMapperTableEntry oriObj : this_globalMapKeysValues) {
            boolean found = false;
            for (ExternalMapperTableEntry otherObj : other_globalMapKeysValues) {
                if (oriObj.getName().equals(otherObj.getName())) {
                    found = true;
                    if (!oriObj.equals(otherObj)) {
                        return false;
                    }
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        return true;
    }

    public String getIsJoinTableOf() {
        return this.isJoinTableOf;
    }

    public void setIsJoinTableOf(String isJoinTableOf) {
        this.isJoinTableOf = isJoinTableOf;
    }

    public boolean isErrorRejectTable() {
        // new system for error reject
        if (isErrorRejectTable) {
            return isErrorRejectTable;
        }
        // only for compatibility for 4.0.0
        if (isErrorRejectTableFor400(this)) {
            return true;
        }
        return false;
    }

    public void setErrorRejectTable(boolean isErrorRejectTable) {
        this.isErrorRejectTable = isErrorRejectTable;
    }

    private boolean isErrorRejectTableFor400(ExternalMapperTable table) {
        boolean errorCode = false;
        boolean errorMessage = false;
        if (table == null || table.getMetadataTableEntries() == null) {
            return false;
        }
        for (ExternalMapperTableEntry entry : table.getMetadataTableEntries()) {
            if (ERROR_REJECT_MESSAGE.equals(entry.getName())) {
                errorMessage = true;
            } else if (ERROR_REJECT_STACK_TRACE.equals(entry.getName())) {
                errorCode = true;
            }
        }
        if (errorCode && errorMessage) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isActivateCondensedTool() {
        return this.activateCondensedTool;
    }

    public void setActivateCondensedTool(boolean activateCondensedTool) {
        this.activateCondensedTool = activateCondensedTool;
    }

    public boolean isSelfFilter() {
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        TableEntryLocation[] tableEntryLocations = dataMapExpressionParser.parseTableEntryLocations(this.expressionFilter);
        for (TableEntryLocation tableEntryLocation : tableEntryLocations) {
            if (!this.name.equals(tableEntryLocation.tableName)) {
                return false;
            }
        }
        return true;
    }

    public boolean isActivateColumnNameFilter() {
        return this.activateColumnNameFilter;
    }

    public void setActivateColumnNameFilter(boolean activateColumnNameFilter) {
        this.activateColumnNameFilter = activateColumnNameFilter;
    }

    public String getColumnNameFilter() {
        return this.columnNameFilter;
    }

    public void setColumnNameFilter(String columnNameFilter) {
        this.columnNameFilter = columnNameFilter;
    }
}
