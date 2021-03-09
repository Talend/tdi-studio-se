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
package org.talend.designer.webservice;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.core.model.components.EParameterName;

/**
 * gcui class global comment. Detailled comment
 */
public class WebServiceComponent extends AbstractExternalNode {

    private WebServiceComponentMain webServiceComponentMain;

    private IMetadataTable inputMetadata;

    private void initWebServiceComponentMain() {
        webServiceComponentMain = new WebServiceComponentMain(this);
    }

    public WebServiceComponentMain getMultiSchemaMain() {
        return this.webServiceComponentMain;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#renameMetadataColumnName(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#getComponentDocumentation(java.lang.String, java.lang.String)
     */
    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#initialize()
     */
    public void initialize() {
        initWebServiceComponentMain();
        webServiceComponentMain.loadInitialParamters();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#open(org.eclipse.swt.widgets.Display)
     */
    public int open(Display display) { // button event
        initWebServiceComponentMain();
        this.getElementParameter(EParameterName.UPDATE_COMPONENTS.getName()).setValue(Boolean.TRUE);
        webServiceComponentMain.createUI(display);
        return webServiceComponentMain.getDialogResponse();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#open(org.eclipse.swt.widgets.Composite)
     */
    public int open(Composite parent) {// double click in job
        initWebServiceComponentMain();
        this.getElementParameter(EParameterName.UPDATE_COMPONENTS.getName()).setValue(Boolean.TRUE);
        webServiceComponentMain.createDialog(parent.getShell());
        return webServiceComponentMain.getDialogResponse();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#renameInputConnection(java.lang.String, java.lang.String)
     */
    public void renameInputConnection(String oldName, String newName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#renameOutputConnection(java.lang.String, java.lang.String)
     */
    public void renameOutputConnection(String oldName, String newName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#setExternalData(org.talend.core.model.process.IExternalData)
     */
    public void setExternalData(IExternalData persistentData) {
        // TODO Auto-generated method stub

    }

    public IExternalData getExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

    public IMetadataTable getInputMetadata() {
        return this.inputMetadata;
    }

    public void setInputMetadata(IMetadataTable inputMetadata) {
        this.inputMetadata = inputMetadata;
    }

    @Override
    public IODataComponentContainer getIODataComponents() {
        IODataComponentContainer inAndOut = new IODataComponentContainer();

        List<IODataComponent> outputs = inAndOut.getOuputs();
        for (IConnection currentConnection : getOutgoingConnections()) {
            if (currentConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent component = new IODataComponent(currentConnection, getMetadataList().get(1));
                outputs.add(component);
            }
        }
        List<IODataComponent> inputs = inAndOut.getInputs();
        for (IConnection currentConnection : getIncomingConnections()) {
            if (currentConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent component = new IODataComponent(currentConnection, inputMetadata);
                inputs.add(component);
            }
        }
        return inAndOut;
    }

    public IExternalData getTMapExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

    public String initInRoWName() {
        String inRowName = new String();
        List<? extends IConnection> inConnList = getIncomingConnections();
        // List<IMetadataTable> metaList = connector.getMetadataList();
        if (!inConnList.isEmpty()) {
            for (int i = 0; i < inConnList.size(); i++) {
                IConnection conn = inConnList.get(i);
                if (conn == null) {
                    continue;
                }
                inRowName = conn.getUniqueName();
            }
            return inRowName;
        } else {
            inRowName = "input";
            return inRowName;
        }
    }

}
