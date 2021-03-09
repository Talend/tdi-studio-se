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
package org.talend.designer.runprocess.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.talend.commons.exception.SystemException;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.runprocess.RunProcessPlugin;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class ShadowNode extends AbstractNode {

    private String componentName;

    private IConnection inCnx;

    private IConnection outCnx;

    private String pluginFullName;

    private IProcess process;

    private int columnNumber = 256;

    /**
     * DOC chuger ShadowNode constructor comment.
     */
    public ShadowNode(String componentName) {
        super();

        this.componentName = componentName;

        this.setElementParameters(new ArrayList<IElementParameter>());
        IComponentsFactory compFac = RunProcessPlugin.getDefault().getRepositoryService().getComponentsFactory();
        setComponent(compFac.get(componentName, ComponentCategory.CATEGORY_4_DI.getName()));
    }

    public ShadowNode(String componentName, int nbColumn) {
        super();

        this.componentName = componentName;

        this.setElementParameters(new ArrayList<IElementParameter>());
        IComponentsFactory compFac = RunProcessPlugin.getDefault().getRepositoryService().getComponentsFactory();
        setComponent(compFac.get(componentName, ComponentCategory.CATEGORY_4_DI.getName()));

        this.columnNumber = nbColumn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getComponentName()
     */
    @Override
    public String getComponentName() {
        return componentName;
    }

    protected void addParameter(IElementParameter param) {
        ((List<IElementParameter>) getElementParameters()).add(param);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getGeneratedCode()
     */
    public String getGeneratedCode() {
        String generatedCode;
        try {
            ICodeGeneratorService service = RunProcessPlugin.getDefault().getCodeGeneratorService();
            generatedCode = service.createCodeGenerator().generateComponentCode(this, ECodePart.MAIN);
        } catch (SystemException e) {
            generatedCode = null;
        }
        return generatedCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getIncomingConnections()
     */
    @Override
    public List<? extends IConnection> getIncomingConnections() {
        return Arrays.asList(inCnx != null ? new IConnection[] { inCnx } : new IConnection[0]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getMetaDataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        List<IMetadataTable> metadataList = super.getMetadataList();
        if (metadataList != null) {
            return metadataList;
        }
        List<IMetadataTable> metadatas = new ArrayList<IMetadataTable>();
        MetadataTable metadata = new MetadataTable();
        metadata.setTableName(this.getUniqueName());
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < columnNumber; i++) {
            MetadataColumn col = new MetadataColumn();
            col.setLabel("row" + i); //$NON-NLS-1$
            col.setTalendType("id_String"); //$NON-NLS-1$
            col.setType("String"); //$NON-NLS-1$
            columns.add(col);
        }
        metadata.setListColumns(columns);
        metadatas.add(metadata);
        return metadatas;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getMappingList()
     */
    public List<Map<String, String>> getMappingList() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getOutgoingConnections()
     */
    @Override
    public List<? extends IConnection> getOutgoingConnections() {
        return Arrays.asList(outCnx != null ? new IConnection[] { outCnx } : new IConnection[0]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getPluginFullName()
     */
    public String getPluginFullName() {
        return this.pluginFullName;
        // return "org.talend.designer.core";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getPluginFullName()
     */
    public void setPluginFullName(String pluginFullName) {
        this.pluginFullName = pluginFullName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getUniqueName()
     */
    @Override
    public String getUniqueName() {
        return getComponentName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isActivate()
     */
    @Override
    public boolean isActivate() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isSubProcessStart()
     */
    @Override
    public boolean isSubProcessStart() {
        return false;
    }

    /**
     * Getter for inCnx.
     *
     * @return the inCnx
     */
    public IConnection getInCnx() {
        return this.inCnx;
    }

    /**
     * Sets the inCnx.
     *
     * @param inCnx the inCnx to set
     */
    public void setInCnx(IConnection inCnx) {
        this.inCnx = inCnx;
    }

    /**
     * Getter for outCnx.
     *
     * @return the outCnx
     */
    public IConnection getOutCnx() {
        return this.outCnx;
    }

    /**
     * Sets the outCnx.
     *
     * @param outCnx the outCnx to set
     */
    public void setOutCnx(IConnection outCnx) {
        this.outCnx = outCnx;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#setMappingList(java.util.List)
     */
    public void setMappingList(List<Map<String, String>> mapping) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean hasConditionalOutputs() {
        return getComponent().hasConditionalOutputs();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isMultiplyingOutputs()
     */
    @Override
    public boolean isMultiplyingOutputs() {
        return getComponent().isMultiplyingOutputs();
    }

    @Override
    public boolean isExternalNode() {
        return false;
    }

    @Override
    public boolean isStart() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getReturns()
     */
    @Override
    public List<? extends INodeReturn> getReturns() {
        return new ArrayList<INodeReturn>();
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    @Override
    public IProcess getProcess() {
        return this.process;
    }

    /**
     * Sets the process.
     *
     * @param process the process to set
     */
    @Override
    public void setProcess(IProcess process) {
        this.process = process;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#renameMetadataColumnName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void metadataInputChanged(IODataComponent dataComponent, String connectionToApply) {
        // Nothing to do as it's shadow node
    }

    @Override
    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        // Nothing to do as it's shadow node
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
