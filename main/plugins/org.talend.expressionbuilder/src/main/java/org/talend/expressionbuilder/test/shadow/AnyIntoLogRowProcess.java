// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.expressionbuilder.test.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.rowgenerator.shadow.LogRowNode;
import org.talend.designer.rowgenerator.shadow.RowGenContextManager;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.shadow.ShadowConnection;
import org.talend.designer.runprocess.shadow.ShadowNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: AnyIntoLogRowProcess.java 下午04:13:24 2007-8-20 +0000 (2007-8-20) yzhang $
 *
 * @param <K>
 */
public class AnyIntoLogRowProcess<K extends ShadowNode> implements IProcess {

    private String name = "IntoLogRow"; //$NON-NLS-1$

    private K inNode;

    private LogRowNode outNode;

    private IContextManager contextManager;

    /**
     * yzhang PerlLogRowProcess constructor comment.
     */
    public AnyIntoLogRowProcess(K inNode, LogRowNode outNode) {

        this.inNode = inNode;
        this.outNode = outNode;

        ShadowConnection cnx = new ShadowConnection(inNode, outNode);
        inNode.setOutCnx(cnx);
        outNode.setInCnx(cnx);

        inNode.setProcess(this);
        outNode.setProcess(this);

        contextManager = new RowGenContextManager();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#addUniqueConnectionName(java.lang.String)
     */
    @Override
    public void addUniqueConnectionName(String uniqueConnectionName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#checkValidConnectionName(java.lang.String)
     */
    @Override
    public boolean checkValidConnectionName(String connectionName) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#checkValidConnectionName(java.lang.String, boolean)
     */
    @Override
    public boolean checkValidConnectionName(String connectionName, boolean checkExists) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#generateUniqueConnectionName(java.lang.String)
     */
    @Override
    public String generateUniqueConnectionName(String baseName) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getAllConnections(java.lang.String)
     */
    @Override
    public IConnection[] getAllConnections(String filter) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getContextManager()
     */
    @Override
    public IContextManager getContextManager() {
        return contextManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getGeneratingNodes()
     */
    @Override
    public List<? extends INode> getGeneratingNodes() {
        return getGraphicalNodes();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getGraphicalNodes()
     */
    @Override
    public List<? extends INode> getGraphicalNodes() {

        return Arrays.asList(new INode[] { inNode, outNode });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getNeededLibraries(boolean)
     */
    @Override
    public Set<String> getNeededLibraries(int options) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getNodesOfType(java.lang.String)
     */
    @Override
    public List<? extends INode> getNodesOfType(String componentName) {
        List<INode> matchingNodes = new ArrayList<INode>();
        if ((inNode != null) && (inNode.getComponentName() != null) && (inNode.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(inNode);
        }
        if ((outNode != null) && (outNode.getComponentName() != null)
                && (outNode.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(outNode);
        }
        return matchingNodes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getProcessor()
     */
    public IProcessor getProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#removeUniqueConnectionName(java.lang.String)
     */
    @Override
    public void removeUniqueConnectionName(String uniqueConnectionName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#setProcessor(org.talend.designer.runprocess.IProcessor)
     */
    public void setProcessor(IProcessor processor) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getAuthor()
     */
    public User getAuthor() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getChildren()
     */
    public List<IRepositoryViewObject> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getCreationDate()
     */
    public Date getCreationDate() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getDescription()
     */
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getId()
     */
    @Override
    public String getId() {
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getLabel()
     */
    public String getLabel() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getModificationDate()
     */
    public Date getModificationDate() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getProperty()
     */
    public Property getProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getPurpose()
     */
    public String getPurpose() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getStatusCode()
     */
    public String getStatusCode() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getType()
     */
    public ERepositoryObjectType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getVersion()
     */
    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setAuthor(org.talend.core.model.properties.User)
     */
    public void setAuthor(User author) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setCreationDate(java.util.Date)
     */
    public void setCreationDate(Date value) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setDescription(java.lang.String)
     */
    public void setDescription(String value) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setId(java.lang.String)
     */
    public void setId(String id) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setModificationDate(java.util.Date)
     */
    public void setModificationDate(Date value) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setProperty(org.talend.core.model.properties.Property)
     */
    public void setProperty(Property property) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setPurpose(java.lang.String)
     */
    public void setPurpose(String value) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setStatusCode(java.lang.String)
     */
    public void setStatusCode(String statusCode) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setVersion(java.lang.String)
     */
    public void setVersion(String version) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#getElementParameter(java.lang.String)
     */
    @Override
    public IElementParameter getElementParameter(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#getElementParameters()
     */
    @Override
    public List<? extends IElementParameter> getElementParameters() {
        return new ArrayList<IElementParameter>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setElementParameters(java.util.List)
     */
    @Override
    public void setElementParameters(List<? extends IElementParameter> elementsParameters) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getMergelinkOrder(org.talend.core.model.process.INode)
     */
    @Override
    public int getMergelinkOrder(INode node) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#isThereLinkWithHash(org.talend.core.model.process.INode)
     */
    @Override
    public boolean isThereLinkWithHash(INode node) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#disableRunJobView()
     */
    public boolean disableRunJobView() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getNodesWithImport()
     */
    @Override
    public List<INode> getNodesWithImport() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends IElementParameter> getElementParametersWithChildrens() {
        return getElementParameters();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getLastRunContext()
     */
    public IContext getLastRunContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#setLastRunContext(org.talend.core.model.process.IContext)
     */
    public void setLastRunContext(IContext context) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getRepositoryNode()
     */
    public RepositoryNode getRepositoryNode() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.repository.IRepositoryObject#setRepositoryNode(org.talend.repository.model.RepositoryNode)
     */
    public void setRepositoryNode(RepositoryNode node) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#isNeedRegenerateCode()
     */
    @Override
    public boolean isNeedRegenerateCode() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#setNeedRegenerateCode(boolean)
     */
    @Override
    public void setNeedRegenerateCode(boolean regenerateCode) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getOutputMetadataTable()
     */
    @Override
    public IMetadataTable getOutputMetadataTable() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getInformationStatus()
     */
    public ERepositoryStatus getInformationStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPath()
     */
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getProjectLabel()
     */
    public String getProjectLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryStatus()
     */
    public ERepositoryStatus getRepositoryStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#isDeleted()
     */
    public boolean isDeleted() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.repository.IRepositoryViewObject#setRepositoryNode(org.talend.repository.model.IRepositoryNode
     * )
     */
    public void setRepositoryNode(IRepositoryNode node) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#getElementName()
     */
    @Override
    public String getElementName() {
        return getLabel();
    }

    @Override
    public void checkStartNodes() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDuplicate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setActivate(boolean activate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContextManager(IContextManager contextManager) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDuplicate(boolean duplicate) {
        // TODO Auto-generated method stub

    }

    public void setProcessModified(boolean modified) {
        // TODO Auto-generated method stub

    }

    @Override
    public IElementParameter getElementParameterFromField(EParameterFieldType dbtable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IElementParameter getElementParameterFromField(EParameterFieldType propertyType, EComponentCategory category) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getPropertyValue(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getPropertyValue(String name, String paramName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPropertyValue(String name, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> getNeededRoutines() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getComponentsType()
     */
    @Override
    public String getComponentsType() {
        return ComponentCategory.CATEGORY_4_DI.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isForceReadOnly()
     */
    @Override
    public boolean isForceReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setForceReadOnly(boolean)
     */
    @Override
    public void setForceReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.talend.core.model.process.IProcess#getNeededModules(boolean)
     */
    @Override
    public Set<ModuleNeeded> getNeededModules(int options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public INode getNodeByUniqueName(String uniqueName) {
        // TODO Auto-generated method stub
        return null;
    }

}
