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
package org.talend.designer.rowgenerator.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.talend.commons.exception.SystemException;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.runprocess.shadow.TextElementParameter;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.rowgenerator.PluginUtils;
import org.talend.designer.rowgenerator.managers.UIManager;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: LogRowNode.java,v 1.4 2007/02/02 07:59:31 pub Exp $
 *
 */
public class LogRowNode extends AbstractNode {

    private String componentName;

    private IComponent component;

    private IConnection inCnx;

    private IConnection outCnx;

    private String pluginFullName;

    private IProcess process;

    private IMetadataTable metadataTable;

    /**
     * qzhang LogRowNode constructor comment.
     */
    public LogRowNode(String componentName, IMetadataTable list) {
        super();
        this.metadataTable = list;
        this.componentName = componentName;
        this.setElementParameters(new ArrayList<IElementParameter>());

        IComponentsFactory compFac = PluginUtils.getRepositoryService().getComponentsFactory();
        setComponent(compFac.get(componentName,ComponentCategory.CATEGORY_4_DI.getName()));
        TextElementParameter param = null;
        TextElementParameter param2 = null;
        TextElementParameter param3 = null;
        TextElementParameter param4 = null;
        TextElementParameter param5 = null;
        TextElementParameter param6 = null;
        if (UIManager.isJavaProject()) {
            param = new TextElementParameter("FIELDSEPARATOR", "\"|\""); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            param = new TextElementParameter("FIELDSEPARATOR", "\'|\'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        param2 = new TextElementParameter("TABLE_PRINT", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        param3 = new TextElementParameter("PRINT_HEADER", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        param4 = new TextElementParameter("PRINT_UNIQUE_NAME", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        param5 = new TextElementParameter("PRINT_COLNAMES", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        param6 = new TextElementParameter("USE_FIXED_LENGTH", "false"); //$NON-NLS-1$ //$NON-NLS-2$

        ((List<IElementParameter>) getElementParameters()).add(param);
        ((List<IElementParameter>) getElementParameters()).add(param2);
        ((List<IElementParameter>) getElementParameters()).add(param3);
        ((List<IElementParameter>) getElementParameters()).add(param4);
        ((List<IElementParameter>) getElementParameters()).add(param5);
        ((List<IElementParameter>) getElementParameters()).add(param6);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getIncomingConnections()
     */
    public List<? extends IConnection> getIncomingConnections() {
        return (List<? extends IConnection>) Arrays.asList(inCnx != null ? new IConnection[] { inCnx } : new IConnection[0]);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getMetadataList()
     */
    public List<IMetadataTable> getMetadataList() {
        List<IMetadataTable> metadatas = new ArrayList<IMetadataTable>();
        MetadataTable metadata = new MetadataTable();
        metadata.setTableName(this.getUniqueName());
        if (UIManager.isJavaProject()) {
            List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
            columns.addAll(metadataTable.getListColumns());
            metadata.setListColumns(columns);
        }
        metadatas.add(metadata);
        return metadatas;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getOutgoingConnections()
     */
    public List<? extends IConnection> getOutgoingConnections() {
        return (List<? extends IConnection>) Arrays.asList(outCnx != null ? new IConnection[] { outCnx } : new IConnection[0]);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getPluginFullName()
     */
    public String getPluginFullName() {
        return this.pluginFullName;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getPluginFullName()
     */
    public void setPluginFullName(String pluginFullName) {
        this.pluginFullName = pluginFullName;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getUniqueName()
     */
    public String getUniqueName() {
        return getComponentName();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#isActivate()
     */
    public boolean isActivate() {
        return true;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#isStart()
     */
    public boolean isStart() {
        return false;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#isSubProcessStart()
     */
    public boolean isSubProcessStart() {
        return true;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getGeneratedCode()
     */
    public String getGeneratedCode() {
        String generatedCode;
        try {
            ICodeGeneratorService service = PluginUtils.getCodeGeneratorService();
            generatedCode = service.createCodeGenerator().generateComponentCode(this, ECodePart.MAIN);
        } catch (SystemException e) {
            generatedCode = null;
        }
        return generatedCode;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public boolean hasConditionalOutputs() {
        return component.hasConditionalOutputs();
    }

    public boolean isMultiplyingOutputs() {
        return component.isMultiplyingOutputs();
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    public IProcess getProcess() {
        return this.process;
    }

    /**
     * Sets the process.
     *
     * @param process the process to set
     */
    public void setProcess(IProcess process) {
        this.process = process;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#renameMetadataColumnName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public void metadataInputChanged(IODataComponent dataComponent, String connectionToApply) {
        // Nothing to do as it's shadow node
    }

    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        // Nothing to do as it's shadow node
    }

    public boolean isReadOnly() {
        return false;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#getComponent()
     */
    public IComponent getComponent() {
        return component;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.INode#setComponent(org.talend.core.model.components.IComponent)
     */
    public void setComponent(IComponent component) {
        this.component = component;

    }

    public IConnection getInCnx() {
        return this.inCnx;
    }

    public void setInCnx(IConnection inCnx) {
        this.inCnx = inCnx;
    }

    public IConnection getOutCnx() {
        return this.outCnx;
    }

    public void setOutCnx(IConnection outCnx) {
        this.outCnx = outCnx;
    }

    public boolean isThereLinkWithMerge() {
        // TODO Auto-generated method stub
        return false;
    }

    public Map<INode, Integer> getLinkedMergeInfo() {
        // TODO Auto-generated method stub
        return null;
    }
}
