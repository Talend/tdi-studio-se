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
package org.talend.designer.mapper.model;

import java.util.List;

import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.table.VarsTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MapperModel {

    private List<InputTable> inputDataMapTables;

    private List<OutputTable> outputDataMapTables;

    private List<VarsTable> varsDataMapTables;

    public MapperModel(List<InputTable> inputDataMapTables, List<OutputTable> outputDataMapTables,
            List<VarsTable> varsDataMapTables) {
        super();
        this.inputDataMapTables = inputDataMapTables;
        this.outputDataMapTables = outputDataMapTables;
        this.varsDataMapTables = varsDataMapTables;
    }

    public List<InputTable> getInputDataMapTables() {
        return this.inputDataMapTables;
    }

    public void setInputDataMapTables(List<InputTable> inputDataMapTables) {
        this.inputDataMapTables = inputDataMapTables;
    }

    public List<OutputTable> getOutputDataMapTables() {
        return this.outputDataMapTables;
    }

    public void setOutputDataMapTables(List<OutputTable> outputDataMapTables) {
        this.outputDataMapTables = outputDataMapTables;
    }

    public List<VarsTable> getVarsDataMapTables() {
        return this.varsDataMapTables;
    }

    public void setVarsDataMapTables(List<VarsTable> varsDataMapTables) {
        this.varsDataMapTables = varsDataMapTables;
    }

}
