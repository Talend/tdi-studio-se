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
package org.talend.designer.core.utils;

import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.SAPConnectionUtils;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public final class SAPParametersUtils {

    /**
     * DOC zli Comment method "getSAPIDocParams".
     *
     * @param elem
     * @param connection
     * @param param
     * @param sapIDocName
     */
    public static void getSAPIDocParams(final IElement elem, final Connection connection, final IElementParameter param,
            final String sapIDocName) {

        if (param.getRepositoryValue() == null) {
            return;
        }
        if (connection != null && sapIDocName != null) {
            SAPConnection sapConnection = (SAPConnection) connection;
            SAPIDocUnit iDocUnit = SAPConnectionUtils.findExistIDocUnit(sapConnection, sapIDocName);
            if (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getRepositoryValue().equals("GATEWAYSERVICE")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(TalendTextUtils.addQuotes(iDocUnit.getGatewayService()));
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getRepositoryValue().equals("PROGRAMID")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(TalendTextUtils.addQuotes(iDocUnit.getProgramId()));
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if (param.getFieldType().equals(EParameterFieldType.CHECK) && param.getRepositoryValue().equals("FORMAT_XML")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(iDocUnit.isUseXmlOutput());
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if (param.getFieldType().equals(EParameterFieldType.FILE) && param.getRepositoryValue().equals("FILE_IDOC_XML")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(TalendTextUtils.addQuotes(iDocUnit.getXmlFile()));
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if (param.getFieldType().equals(EParameterFieldType.CHECK) && param.getRepositoryValue().equals("FORMAT_HTML")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(iDocUnit.isUseHtmlOutput());
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if (param.getFieldType().equals(EParameterFieldType.FILE) && param.getRepositoryValue().equals("FILE_IDOC_HTML")) { //$NON-NLS-1$
                if (iDocUnit != null) {
                    param.setValue(TalendTextUtils.addQuotes(iDocUnit.getHtmlFile()));
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
        }
    }

    /**
     * DOC zli Comment method "retrieveSAPIDocParams".
     *
     * @param elem
     * @param connection
     * @param param
     * @param sapFunctionName
     */
    public static void retrieveSAPIDocParams(final Element elem, final Connection connection, final IElementParameter param,
            final String sapFunctionName) {
        if (param.getRepositoryValue() == null) {
            return;
        }
        SAPIDocUnit iDocUnit = null;
        if (connection != null && sapFunctionName != null) {
            SAPConnection sapConnection = (SAPConnection) connection;
            iDocUnit = SAPConnectionUtils.findExistIDocUnit(sapConnection, sapFunctionName);
        }
        if (param.getRepositoryValue().equals("SAPIDOC")) { //$NON-NLS-1$
            if (connection != null && sapFunctionName != null) {
                param.setValue(TalendTextUtils.addQuotes(sapFunctionName));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getRepositoryValue().equals("GATEWAYSERVICE")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(TalendTextUtils.addQuotes(iDocUnit.getGatewayService()));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getRepositoryValue().equals("PROGRAMID")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(TalendTextUtils.addQuotes(iDocUnit.getProgramId()));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.CHECK) && param.getRepositoryValue().equals("FORMAT_XML")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(iDocUnit.isUseXmlOutput());
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.FILE) && param.getRepositoryValue().equals("FILE_IDOC_XML")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(TalendTextUtils.addQuotes(iDocUnit.getXmlFile()));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.CHECK) && param.getRepositoryValue().equals("FORMAT_HTML")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(iDocUnit.isUseHtmlOutput());
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
        if (param.getFieldType().equals(EParameterFieldType.FILE) && param.getRepositoryValue().equals("FILE_IDOC_HTML")) { //$NON-NLS-1$
            if (iDocUnit != null) {
                param.setValue(TalendTextUtils.addQuotes(iDocUnit.getHtmlFile()));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }

    }

    public static void setNoRepositoryIDocParams(final IElementParameter param) {
        if (param == null) {
            return;
        }
        if ((param.getFieldType().equals(EParameterFieldType.TEXT) && param.getName().equals("GATEWAYSERVICE")) //$NON-NLS-1$
                || (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getName().equals("PROGRAMID")) //$NON-NLS-1$
                || (param.getFieldType().equals(EParameterFieldType.CHECK) && (param.getName().equals("FORMAT_XML"))) || (param.getFieldType().equals(EParameterFieldType.FILE) && param.getName().equals("FILE_IDOC_XML")) //$NON-NLS-1$ //$NON-NLS-2$
                || (param.getFieldType().equals(EParameterFieldType.CHECK) && param.getName().equals("FORMAT_HTML")) || (param.getFieldType().equals(EParameterFieldType.FILE) && param.getName().equals("FILE_IDOC_HTML"))) { //$NON-NLS-1$
            param.setRepositoryValueUsed(false);
            param.setReadOnly(false);
        }
    }

    /**
     * DOC xye Comment method "retrieveSAPParams".
     *
     * @param param
     */
    @SuppressWarnings("unchecked")
    public static void retrieveSAPParams(final IElement elem, final Connection connection, final IElementParameter param,
            final String sapFunctionLabel) {
        if (param.getRepositoryValue() == null || !(connection instanceof SAPConnection)) {
            return;
        }
        if (param.getFieldType().equals(EParameterFieldType.TEXT) && param.getRepositoryValue().equals("SAP_FUNCTION")) { //$NON-NLS-1$
            if (connection != null && sapFunctionLabel != null) {
                SAPFunctionUnit unit = null;
                for (int i = 0; i < ((SAPConnection) connection).getFuntions().size(); i++) {
                    SAPFunctionUnit tmp = ((SAPConnection) connection).getFuntions().get(i);
                    if (tmp.getLabel().equals(sapFunctionLabel)) {
                        unit = tmp;
                        break;
                    }
                }
                if (unit == null) {
                    return;
                }
                param.setValue(TalendTextUtils.addQuotes(unit.getName()));
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        } else if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
            if (param.getRepositoryValue().equals("INPUT_PARAMS")) {//$NON-NLS-1$
                if (connection != null && sapFunctionLabel != null) {
                    List<Map<String, Object>> table = (List<Map<String, Object>>) elem.getPropertyValue(param.getName());
                    RepositoryToComponentProperty.getSAPInputAndOutputValue((SAPConnection) connection, table, sapFunctionLabel,
                            true);
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                } else {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
            if ("SAP_PROPERTIES".equals(param.getRepositoryValue())) {//$NON-NLS-1$
                Object value = RepositoryToComponentProperty.getValue(connection, "SAP_PROPERTIES", null);//$NON-NLS-1$
                param.setValue(value);
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);

            }
        } else if (param.getFieldType().equals(EParameterFieldType.TABLE) && param.getRepositoryValue().equals("OUTPUT_PARAMS")) { //$NON-NLS-1$
            if (connection != null && sapFunctionLabel != null) {
                List<Map<String, Object>> table = (List<Map<String, Object>>) elem.getPropertyValue(param.getName());
                RepositoryToComponentProperty.getSAPInputAndOutputValue((SAPConnection) connection, table, sapFunctionLabel,
                        false);
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        } else if (param.getRepositoryValue().equals("SAP_ITERATE_OUT_TYPE")) { //$NON-NLS-1$
            if (connection != null && sapFunctionLabel != null) {
                param.setValue(RepositoryToComponentProperty.getSAPValuesForFunction((SAPConnection) connection,
                        sapFunctionLabel, "SAP_ITERATE_OUT_TYPE")); //$NON-NLS-1$
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        } else if (param.getRepositoryValue().equals("SAP_ITERATE_OUT_TABLENAME")) { //$NON-NLS-1$
            if (connection != null && sapFunctionLabel != null) {
                param.setValue(RepositoryToComponentProperty.getSAPValuesForFunction((SAPConnection) connection,
                        sapFunctionLabel, "SAP_ITERATE_OUT_TABLENAME")); //$NON-NLS-1$
                param.setRepositoryValueUsed(true);
                param.setReadOnly(true);
            } else {
                param.setRepositoryValueUsed(false);
                param.setReadOnly(false);
            }
        }
    }

    public static void setNoRepositoryParams(final IElementParameter param) {
        if (param == null) {
            return;
        }
        if ((param.getFieldType().equals(EParameterFieldType.TABLE) && param.getName().equals("MAPPING_INPUT")) //$NON-NLS-1$
                || (param.getFieldType().equals(EParameterFieldType.TABLE) && param.getName().equals("MAPPING_OUTPUT")) //$NON-NLS-1$
                || (param.getName().equals("SAP_ITERATE_OUT_TYPE")) || param.getName().equals("SAP_ITERATE_OUT_TABLENAME") //$NON-NLS-1$ //$NON-NLS-2$
                || param.getName().equals("SAP_FUNCTION")) { //$NON-NLS-1$
            param.setRepositoryValueUsed(false);
            param.setReadOnly(false);
        }
    }
}
