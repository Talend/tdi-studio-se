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
package org.talend.designer.webservice.data;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class InputMappingData {

    private String parameterName;

    private String InputColumnValue;

    private String inputElementValue;

    private ParameterInfo parameter;

    private List<IMetadataColumn> metadataColumnList = new ArrayList<IMetadataColumn>();

    public List<IMetadataColumn> getMetadataColumnList() {
        return this.metadataColumnList;
    }

    public void setMetadataColumnList(List<IMetadataColumn> metadataColumnList) {
        this.metadataColumnList = metadataColumnList;
    }

    public ParameterInfo getParameter() {
        return this.parameter;
    }

    public void setParameter(ParameterInfo parameter) {
        this.parameter = parameter;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getInputColumnValue() {
        return this.InputColumnValue;
    }

    public void setInputColumnValue(String inputColumnValue) {
        this.InputColumnValue = inputColumnValue;
    }

    public String getInputElementValue() {
        return this.inputElementValue;
    }

    public void setInputElementValue(String inputElementValue) {
        this.inputElementValue = inputElementValue;
    }

}
