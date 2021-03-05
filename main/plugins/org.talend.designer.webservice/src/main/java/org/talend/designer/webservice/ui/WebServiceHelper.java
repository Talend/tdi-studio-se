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
package org.talend.designer.webservice.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.connection.WSDLParameter;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.service.IWebServiceTos;
import org.talend.core.ui.AbstractWebService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.webService.WebServiceTosSaveManager;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.WebServiceComponentMain;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WebServiceHelper implements IWebServiceTos {

    private WebServiceUI webServiceUI;

    WebServiceTosSaveManager manager = WebServiceTosSaveManager.getInstance();

    public WebServiceHelper() {
    }

    @Override
    public AbstractWebService getWebServiceUI(Composite uiParent, ConnectionItem connectionItem, INode node) {
        if (node != null) {
            IExternalNode enode = node.getExternalNode();
            if (enode instanceof WebServiceComponent) {
                WebServiceComponentMain b = new WebServiceComponentMain((WebServiceComponent) enode);
                enode.setComponent(b.getWebServiceComponent().getComponent());
                webServiceUI = new WebServiceUI(uiParent, b, connectionItem);
                webServiceUI.init();
                manager.addWebServiceSaveListener(this);
                return webServiceUI;
            }
        }
        return getWebServiceUI(uiParent, connectionItem);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.metadata.connection.wsdl.IWebService#getWebServiceUI(org.eclipse.swt.widgets
     * .Composite, org.talend.core.model.components.IComponent)
     */
    @Override
    public AbstractWebService getWebServiceUI(Composite uiParent, ConnectionItem connectionItem) {
        // TODO Auto-generated method stub
        WebServiceComponent wenCom = new WebServiceComponent();
        wenCom.initialize();
        IComponent iComponent = ComponentsFactoryProvider.getInstance().get("tWebService",
                ComponentCategory.CATEGORY_4_DI.getName());
        List<? extends IElementParameter> parameters = iComponent.createElementParameters(wenCom);
        wenCom.setElementParameters(parameters);
        if (connectionItem.getState() != null) {
            WSDLSchemaConnection connection = (WSDLSchemaConnection) connectionItem.getConnection();
            wenCom.getElementParameter("ENDPOINT").setValue(connection.getEndpointURI());
            String currentURL = connection.getWSDL();
            String method = connection.getMethodName();
            String currePortName = connection.getPortName();
            String nameSpace = connection.getServerNameSpace();
            String serverName = connection.getServerName();
            String portNameSpace = connection.getPortNameSpace();
            if (!"".equals(currentURL) && currentURL != null) {
                IElementParameter ENDPOINTPara = wenCom.getElementParameter("ENDPOINT");
                ENDPOINTPara.setValue(currentURL);
            }

            if (currePortName != null) {
                IElementParameter Port_Name = wenCom.getElementParameter("PORT_NAME");
                Port_Name.setValue(currePortName);
            }
            //
            if (method != null) {
                IElementParameter METHODPara = wenCom.getElementParameter("METHOD");
                METHODPara.setValue(method);
            }
            if (nameSpace != null) {
                IElementParameter Service_NS = wenCom.getElementParameter("SERVICE_NS");
                Service_NS.setValue(nameSpace);
            }
            if (serverName != null) {
                IElementParameter Service_Name = wenCom.getElementParameter("SERVICE_NAME");
                Service_Name.setValue(serverName);
            }
            if (portNameSpace != null) {
                IElementParameter Port_NS = wenCom.getElementParameter("PORT_NS");
                Port_NS.setValue(portNameSpace);
            }
            if (connection.getParameterValue() != null) {
                IElementParameter INPUT_PARAMSPara = wenCom.getElementParameter("INPUT_PARAMS");
                List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
                EList inputList = connection.getParameterValue();
                for (int i = 0; i < inputList.size(); i++) {
                    Map<String, String> inputMap = new HashMap<String, String>(2);
                    WSDLParameter parameter = (WSDLParameter) inputList.get(i);
                    if (parameter.getElement() != null) {
                        if (parameter.getExpression() != null) {
                            inputMap.put("EXPRESSION", parameter.getExpression());
                        }
                        inputMap.put("ELEMENT", parameter.getElement());
                        inputparaValue.add(inputMap);
                    }
                    if (parameter.getSource() != null) {
                        inputMap.put("SOURCE", parameter.getSource());
                        inputparaValue.add(inputMap);
                    }
                    if (parameter.getColumn() != null) {
                        inputMap.put("COLUMN", parameter.getColumn());
                        inputparaValue.add(inputMap);
                    }
                    if (parameter.getParameterInfo() != null) {
                        inputMap.put("PARAMETERINFO", parameter.getParameterInfo());
                        inputMap.put("PARAPARENT", parameter.getParameterInfoParent());
                        inputparaValue.add(inputMap);
                    }
                }

            }
            if (connection.getOutputParameter() != null) {
                IElementParameter OUTPUT_PARAMSPara = wenCom.getElementParameter("OUTPUT_PARAMS");
                List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
                EList inputList = connection.getOutputParameter();
                for (int i = 0; i < inputList.size(); i++) {
                    Map<String, String> eleMap = new HashMap<String, String>(3);
                    WSDLParameter parameter = (WSDLParameter) inputList.get(i);
                    if (parameter.getExpression() != null || parameter.getColumn() != null) {
                        if (parameter.getExpression() != null) {
                            eleMap.put("EXPRESSION", parameter.getExpression());
                        }
                        // if (parameter.getColumn() != null) {
                        eleMap.put("COLUMN", parameter.getColumn());
                        // }
                        outputMap.add(eleMap);
                    }
                    if (parameter.getElement() != null) {
                        eleMap.put("ELEMENT", parameter.getElement());
                        outputMap.add(eleMap);
                    }

                    if (parameter.getSource() != null) {
                        eleMap.put("SOURCE", parameter.getSource());
                        outputMap.add(eleMap);
                    }
                    if (parameter.getParameterInfo() != null) {
                        eleMap.put("PARAMETERINFO", parameter.getParameterInfo());
                        eleMap.put("PARAPARENT", parameter.getParameterInfoParent());
                        outputMap.add(eleMap);
                    }
                }
            }
        }
        MetadataTable inputMetadata = new MetadataTable();
        inputMetadata.setAttachedConnector("FLOW");
        inputMetadata.setLabel("Input");
        MetadataTable outputMetadaTable = null;
        List<org.talend.core.model.metadata.IMetadataColumn> newColumnList = new ArrayList<org.talend.core.model.metadata.IMetadataColumn>();
        List<org.talend.core.model.metadata.IMetadataColumn> newInputColumnList = new ArrayList<org.talend.core.model.metadata.IMetadataColumn>();
        if (connectionItem.getState() != null) {
            outputMetadaTable = new MetadataTable();
            outputMetadaTable.setAttachedConnector("OUTPUT");
            outputMetadaTable.setLabel("Output");
            WSDLSchemaConnection connection = (WSDLSchemaConnection) connectionItem.getConnection();
            Set<org.talend.core.model.metadata.builder.connection.MetadataTable> tables = ConnectionHelper.getTables(connection);
            //
            // EList<MetadataColumn> schemaMetadataColumn =
            // ((org.talend.core.model.metadata.builder.connection.MetadataTable) ConnectionHelper
            // .getTables(connection).toArray()[0]).getColumns();
            Iterator it = tables.iterator();
            while (it.hasNext()) {
                org.talend.core.model.metadata.builder.connection.MetadataTable metadatatable = (org.talend.core.model.metadata.builder.connection.MetadataTable) it
                        .next();
                if (metadatatable.getLabel().equals("Output")) {
                    for (int i = 0; i < metadatatable.getColumns().size(); i++) {
                        org.talend.core.model.metadata.builder.connection.MetadataColumn col = metadatatable.getColumns().get(i);
                        org.talend.core.model.metadata.MetadataColumn newColumn = new org.talend.core.model.metadata.MetadataColumn();
                        newColumn.setLabel(col.getLabel());
                        newColumn.setTalendType(col.getTalendType());
                        newColumn.setOriginalDbColumnName(col.getLabel());
                        newColumnList.add(newColumn);
                    }
                    outputMetadaTable.setListColumns(newColumnList);
                } else {
                    // EList inschemaMetadataColumn = ((org.talend.core.model.metadata.builder.connection.MetadataTable)
                    // ConnectionHelper
                    // .getTables(connection).toArray()[1]).getColumns();
                    for (int i = 0; i < metadatatable.getColumns().size(); i++) {
                        org.talend.core.model.metadata.builder.connection.MetadataColumn col = metadatatable.getColumns().get(i);
                        org.talend.core.model.metadata.MetadataColumn newColumn = new org.talend.core.model.metadata.MetadataColumn();
                        newColumn.setLabel(col.getLabel());
                        newColumn.setTalendType(col.getTalendType());
                        newColumn.setOriginalDbColumnName(col.getLabel());
                        newInputColumnList.add(newColumn);
                    }
                    inputMetadata.setListColumns(newInputColumnList);
                }
            }
        } else {
            outputMetadaTable = new MetadataTable();
            outputMetadaTable.setAttachedConnector("OUTPUT");
        }

        List<IMetadataTable> metadataTableList = new ArrayList<IMetadataTable>(4);
        metadataTableList.add(inputMetadata);
        metadataTableList.add(outputMetadaTable);
        wenCom.setMetadataList(metadataTableList);
        WebServiceComponentMain b = new WebServiceComponentMain(wenCom);
        wenCom.setComponent(b.getWebServiceComponent().getComponent());
        webServiceUI = new WebServiceUI(uiParent, b, connectionItem);
        webServiceUI.init();
        manager.addWebServiceSaveListener(this);
        return webServiceUI;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IWebService#getCurrentFunction()
     */
    @Override
    public Boolean getCurrentFunction() {
        boolean flag = true;
        if (webServiceUI.getCurrentFunction() == null) {
            return false;
        }
        return flag;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IWebService#getTabFolder()
     */
    public CTabFolder getTabFolder() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IWebService#getTable()
     */
    @Override
    public Table getTable() {
        // TODO Auto-generated method stub
        return webServiceUI.getTable();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.metadata.connection.wsdl.webService.tree.WebServiceSaveListener#saveValue()
     */
    @Override
    public void saveValue() {
        // TODO Auto-generated method stub
        webServiceUI.saveInputValue();
        webServiceUI.saveOutPutValue();
    }

    @Override
    public LabelledFileField getWSDLLabel(Boolean b) {
        return webServiceUI.getWSDLLabel(b);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IWebServiceTos#refreshUI()
     */
    @Override
    public void refreshUI(String url) {
        webServiceUI.refresh(url, true);
    }

}
