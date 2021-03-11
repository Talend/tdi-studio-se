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
package org.talend.designer.rowgenerator.external.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExternalRowGeneratorData.java,v 1.3 2007/01/31 05:20:52 pub Exp $
 *
 */
public class ExternalRowGeneratorData implements Serializable {

    private List<ExternalRowGenTable> outputTables = new ArrayList<ExternalRowGenTable>();

    /**
     *
     */
    private static final long serialVersionUID = 21232132141L;

    private ExternalRowGeneratorUiProperties uiProperties = new ExternalRowGeneratorUiProperties();

    public ExternalRowGeneratorUiProperties getUiProperties() {
        return this.uiProperties;
    }

    public void setUiProperties(ExternalRowGeneratorUiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    public List<ExternalRowGenTable> getOutputTables() {
        return outputTables;
    }

    /**
     * Sets the outputTables.
     *
     * @param outputTables the outputTables to set
     */
    public void setOutputTables(List<ExternalRowGenTable> outputTables) {
        this.outputTables = outputTables;
    }

}
