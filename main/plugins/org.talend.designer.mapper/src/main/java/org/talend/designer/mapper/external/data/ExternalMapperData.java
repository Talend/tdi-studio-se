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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.designer.abstractmap.managers.MapDataDelegateHelper;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ExternalMapperData implements IExternalData {

    /**
     *
     */
    private static final long serialVersionUID = 4877887839142463534L;

    private ExternalMapperUiProperties uiProperties = new ExternalMapperUiProperties();

    private List<ExternalMapperTable> inputTables = new ArrayList<ExternalMapperTable>(0);

    private List<ExternalMapperTable> outputTables = new ArrayList<ExternalMapperTable>(0);

    private List<ExternalMapperTable> varsTables = new ArrayList<ExternalMapperTable>(0);

    public List<ExternalMapperTable> getInputTables() {
        return this.inputTables;
    }

    public void setInputTables(List<ExternalMapperTable> tables) {
        this.inputTables = tables;
    }

    public List<ExternalMapperTable> getOutputTables() {
        return this.outputTables;
    }

    public void setOutputTables(List<ExternalMapperTable> outputTables) {
        this.outputTables = outputTables;
    }

    public List<ExternalMapperTable> getVarsTables() {
        return this.varsTables;
    }

    public void setVarsTables(List<ExternalMapperTable> varsTables) {
        this.varsTables = varsTables;
    }

    public ExternalMapperUiProperties getUiProperties() {
        return this.uiProperties;
    }

    public void setUiProperties(ExternalMapperUiProperties layoutProperties) {
        this.uiProperties = layoutProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    public IExternalData clone() throws CloneNotSupportedException {
        ExternalMapperData cloned = (ExternalMapperData) super.clone();
        cloned.uiProperties = (ExternalMapperUiProperties) uiProperties.clone();
        cloned.inputTables = (List<ExternalMapperTable>) ((ArrayList) inputTables).clone();
        int inputTablesListSize = inputTables.size();
        for (int i = 0; i < inputTablesListSize; i++) {
            cloned.inputTables.set(i, (ExternalMapperTable) cloned.inputTables.get(i).clone());
        }
        cloned.outputTables = (List<ExternalMapperTable>) ((ArrayList) outputTables).clone();
        int listSizeoutputTables = outputTables.size();
        for (int i = 0; i < listSizeoutputTables; i++) {
            cloned.outputTables.set(i, (ExternalMapperTable) cloned.outputTables.get(i).clone());
        }
        cloned.varsTables = (List<ExternalMapperTable>) ((ArrayList) varsTables).clone();
        int listSizevarsTables = varsTables.size();
        for (int i = 0; i < listSizevarsTables; i++) {
            cloned.varsTables.set(i, (ExternalMapperTable) cloned.varsTables.get(i).clone());
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
        result = prime * result + ((this.inputTables == null) ? 0 : this.inputTables.hashCode());
        result = prime * result + ((this.outputTables == null) ? 0 : this.outputTables.hashCode());
        result = prime * result + ((this.varsTables == null) ? 0 : this.varsTables.hashCode());
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
        final ExternalMapperData other = (ExternalMapperData) obj;
        if(this.inputTables.size() != other.inputTables.size()){
            return false;
        }
        if(this.outputTables.size() != other.outputTables.size()){
            return false;
        }
        if(this.varsTables.size() != other.varsTables.size()){
            return false;
        }

        for(ExternalMapperTable inTable:inputTables){
            boolean found = false;
            for(ExternalMapperTable otherTable:other.inputTables){
                if(inTable.getName().equals(otherTable.getName())){
                    found = true;
                    if(!inTable.equals(otherTable)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }

        for(ExternalMapperTable outTable:outputTables){
            boolean found = false;
            for(ExternalMapperTable otherTable:other.outputTables){
                if(outTable.getName().equals(otherTable.getName())){
                    found = true;
                    if(!outTable.equals(otherTable)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }

        for(ExternalMapperTable varTable:varsTables){
            boolean found = false;
            for(ExternalMapperTable var:other.varsTables){
                if(varTable.getName().equals(var.getName())){
                    found = true;
                    if(!varTable.equals(var)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        return true;
    }

    public Map<IExternalMapTable, List<IExternalMapEntry>> getExpressionColumns(String expression, ExternalDataType... types) {
        MapDataDelegateHelper delegate = new MapDataDelegateHelper(getInputTables(), getOutputTables(), getVarsTables());
        return delegate.getExpressionColumns(expression, types);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalData#getJoinTableNames(java.lang.String)
     */
    public List<String> getJoinedTableNames(String mainTable) {
        List<String> joinedTableNames = new ArrayList<String>();
        if (getOutputTables() != null) {
            for (ExternalMapperTable table : getOutputTables()) {
                if (table.getIsJoinTableOf() != null && table.getIsJoinTableOf().equals(mainTable)) {
                    joinedTableNames.add(table.getName());
                }
            }
        }
        return joinedTableNames;
    }

    public Map<String, Set<String>> getLinkedTableColumnsMapping() {
        Map<String, Set<String>> tableColumnsMapping = new HashMap<String, Set<String>>();
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        List<ExternalMapperTable> tablesHasFilter = new ArrayList<ExternalMapperTable>();
        tablesHasFilter.addAll(inputTables);
        tablesHasFilter.addAll(outputTables);
        for (ExternalMapperTable table : tablesHasFilter) {
            TableEntryLocation[] tableEntryLocations = dataMapExpressionParser.parseTableEntryLocations(table
                    .getExpressionFilter());
            for (TableEntryLocation tableEntryLocation : tableEntryLocations) {
                if (!tableColumnsMapping.containsKey(tableEntryLocation.tableName)) {
                    Set<String> columns = new HashSet<String>();
                    columns.add(tableEntryLocation.columnName);
                    tableColumnsMapping.put(tableEntryLocation.tableName, columns);
                } else {
                    tableColumnsMapping.get(tableEntryLocation.tableName).add(tableEntryLocation.columnName);
                }
            }
        }
        List<ExternalMapperTable> tablesHasColumnExpression = new ArrayList<ExternalMapperTable>();
        tablesHasColumnExpression.addAll(varsTables);
        tablesHasColumnExpression.addAll(outputTables);
        for (ExternalMapperTable table : tablesHasColumnExpression) {
            List<ExternalMapperTableEntry> metadataTableEntries = table.getMetadataTableEntries();
            for (ExternalMapperTableEntry entry : metadataTableEntries) {
                TableEntryLocation[] tableEntryLocations = dataMapExpressionParser
                        .parseTableEntryLocations(entry.getExpression());
                for (TableEntryLocation tableEntryLocation : tableEntryLocations) {
                    if (!tableColumnsMapping.containsKey(tableEntryLocation.tableName)) {
                        Set<String> columns = new HashSet<String>();
                        columns.add(tableEntryLocation.columnName);
                        tableColumnsMapping.put(tableEntryLocation.tableName, columns);
                    } else {
                        tableColumnsMapping.get(tableEntryLocation.tableName).add(tableEntryLocation.columnName);
                    }
                }
            }
        }
        return tableColumnsMapping;
    }
}
