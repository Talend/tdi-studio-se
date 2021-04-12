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
package org.talend.designer.filemultischemas;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasData;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasComponent extends AbstractExternalNode {

    private MultiSchemasMain multiSchemaMain;

    private ExternalMultiSchemasData externalMultiSchemaData;

    private void initMultiSchemaMain() {
        multiSchemaMain = new MultiSchemasMain(this);
    }

    public MultiSchemasMain getMultiSchemaMain() {
        return this.multiSchemaMain;
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
     * @see org.talend.core.model.process.IExternalNode#getExternalData()
     */
    public IExternalData getExternalData() {
        if (this.externalMultiSchemaData == null) {
            this.externalMultiSchemaData = new ExternalMultiSchemasData();
        }

        return this.externalMultiSchemaData;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#initialize()
     */
    public void initialize() {
        initMultiSchemaMain();
        multiSchemaMain.loadInitialParamters();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#open(org.eclipse.swt.widgets.Display)
     */
    public int open(Display display) { // button event
        initMultiSchemaMain();
        multiSchemaMain.createUI(display);
        return multiSchemaMain.getDialogResponse();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#open(org.eclipse.swt.widgets.Composite)
     */
    public int open(Composite parent) {// double click in job
        initMultiSchemaMain();
        multiSchemaMain.createDialog(parent.getShell());
        return multiSchemaMain.getDialogResponse();
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

    public IExternalData getTMapExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

}
