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
package org.talend.designer.runprocess.shadow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextListener;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Status;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.designer.runprocess.IProcessor;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id: FileinToXmlProcess.java 3737 2007-06-09 08:19:35 +0000 (星期六, 09 六月 2007) nrousseau $
 *
 * @param <K>
 */
public class FileinToDelimitedProcess<K extends FileInputNode> extends RepositoryObject implements IProcess {

    // private String name = "ShadowFileInputToXmlOutput"; //$NON-NLS-1$

    private String name = "ShadowFileInputToDelimitedOutput"; //$NON-NLS-1$

    private IContextManager contextManager;

    private K inNode;

    private FileOutputDelimitedNode outNode;

    private Prejob prejob;

    private SetProxy setProxy;

    /**
     * Constructs a new FileinToXmlProcess.
     */
    public FileinToDelimitedProcess(K inNode, FileOutputDelimitedNode outNode) {
        super();

        this.inNode = inNode;
        this.outNode = outNode;

        outNode.setColumnNumber(inNode.getColumnNumber());
        ShadowConnection cnx = new ShadowConnection(inNode, outNode);
        inNode.setOutCnx(cnx);
        outNode.setInCnx(cnx);

        inNode.setProcess(this);

        outNode.setProcess(this);

        contextManager = new EmptyContextManager();
    }

    public FileinToDelimitedProcess(K inNode, FileOutputDelimitedNode outNode, Prejob prejob, SetProxy setProxy) {
        this(inNode, outNode);
        this.prejob = prejob;
        this.setProxy = setProxy;

        setProxy.setColumnNumber(prejob.getColumnNumber());
        ShadowConnection ok1 = new ShadowConnection(prejob, setProxy, EConnectionType.ON_COMPONENT_OK, "OnComponentOk");
        prejob.setOutCnx(ok1);
        setProxy.setInCnx(ok1);

        inNode.setColumnNumber(inNode.getColumnNumber() + setProxy.getColumnNumber());
        ShadowConnection ok2 = new ShadowConnection(setProxy, inNode, EConnectionType.ON_SUBJOB_OK, "OnSubjobOk");
        setProxy.setOutCnx(ok2);
        inNode.setInCnx(ok2);

        prejob.setProcess(this);
        setProxy.setProcess(this);

    }

    public String getTechnicalName() {
        return name;
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
     * @see org.talend.core.model.process.IProcess#getNodes()
     */
    @Override
    public List<? extends INode> getGraphicalNodes() {
        if (prejob != null && setProxy != null) {
            return Arrays.asList(new INode[] { inNode, outNode, prejob, setProxy });
        }
        return Arrays.asList(new INode[] { inNode, outNode });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#getAuthor()
     */
    @Override
    public User getAuthor() {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        return repositoryContext.getUser();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#getId()
     */
    @Override
    public String getId() {
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#getLabel()
     */
    @Override
    public String getLabel() {
        return name;
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#getStatus()
     */
    public Status getStatus() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#getVersion()
     */
    @Override
    public String getVersion() {
        return VersionUtils.DEFAULT_VERSION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#setAuthor(org.talend.core.model.general.User)
     */
    @Override
    public void setAuthor(User author) {
        // Read only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#setId(int)
     */
    public void setId(int id) {
        // Read only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label) {
        // Read only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#setStatus(org.talend.core.model.process.EProcessStatus)
     */
    public void setStatus(Status status) {
        // Read only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryProcess#setVersion(org.talend.core.model.version.Version)
     */
    @Override
    public void setVersion(String version) {
        // Read only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.temp.IXmlSerializable#getXmlStream()
     */
    public InputStream getXmlStream() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.temp.IXmlSerializable#setXmlStream(java.io.InputStream)
     */
    public void setXmlStream(InputStream xmlStream) {
        // Do nothing
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
     * @see org.talend.core.model.repository.IRepositoryObject#getComment()
     */
    public String getComment() {
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#setComment(java.lang.String)
     */
    public void setComment(String comment) {
        // Read-only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getType()
     */
    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.PROCESS;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#addUniqueConnectionName(java.lang.String)
     */
    @Override
    public void addUniqueConnectionName(String uniqueConnectionName) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#checkValidConnectionName(java.lang.String)
     */
    @Override
    public boolean checkValidConnectionName(String connectionName) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#generateUniqueConnectionName()
     */
    @Override
    public String generateUniqueConnectionName(String baseName) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#removeUniqueConnectionName(java.lang.String)
     */
    @Override
    public void removeUniqueConnectionName(String uniqueConnectionName) {
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
     * @see org.talend.core.model.process.IElement#setElementParameters(java.util.List)
     */
    @Override
    public void setElementParameters(List<? extends IElementParameter> elementsParameters) {
    }

    /**
     * Empty IContext implementation. <br/>
     *
     * $Id: FileinToXmlProcess.java 3737 2007-06-09 08:19:35 +0000 (星期六, 09 六月 2007) nrousseau $
     *
     */
    private static class EmptyContext implements IContext, Cloneable {

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#getContextParameterList()
         */
        @Override
        public List<IContextParameter> getContextParameterList() {
            return new ArrayList<IContextParameter>();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#getName()
         */
        @Override
        public String getName() {
            return "Shadow"; //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#isConfirmationNeeded()
         */
        @Override
        public boolean isConfirmationNeeded() {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#setConfirmationNeeded(boolean)
         */
        @Override
        public void setConfirmationNeeded(boolean confirmationNeeded) {
            // Read-only
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#setContextParameterList(java.util.List)
         */
        @Override
        public void setContextParameterList(List<IContextParameter> contextParameterList) {
            // Read-only
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#setName(java.lang.String)
         */
        @Override
        public void setName(String name) {
            // Read-only
        }

        @Override
        public IContext clone() {
            return this;
        }

        @Override
        public boolean sameAs(IContext context) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public IContextParameter getContextParameter(String parameterName) {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContext#getContextParameter(java.lang.String, java.lang.String)
         */

        @Override
        public IContextParameter getContextParameter(String sourceId, String paraName) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean containsSameParameterIgnoreCase(String parameterName) {
            // TODO Auto-generated method stub
            return false;
        }
    }

    /**
     * DOC nrousseau FileinDelimitedToXmlProcess class global comment. Detailled comment <br/>
     *
     * $Id: FileinToXmlProcess.java 3737 2007-06-09 08:19:35 +0000 (星期六, 09 六月 2007) nrousseau $
     *
     */
    private static class EmptyContextManager implements IContextManager, Cloneable {

        /*
         * (non-Javadoc)
         *
         * @see
         * org.talend.core.model.process.IContextManager#addContextListener(org.talend.core.model.process.IContextListener
         * )
         */
        @Override
        public void addContextListener(IContextListener listener) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContextManager#fireContextsChangedEvent()
         */
        @Override
        public void fireContextsChangedEvent() {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContextManager#getDefaultContext()
         */
        @Override
        public IContext getDefaultContext() {
            return new EmptyContext();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContextManager#getListContext()
         */
        @Override
        public List<IContext> getListContext() {
            return Arrays.asList(new IContext[] { getDefaultContext() });
        }

        /*
         * (non-Javadoc)
         *
         * @seeorg.talend.core.model.process.IContextManager#removeContextListener(org.talend.core.model.process.
         * IContextListener)
         */
        @Override
        public void removeContextListener(IContextListener listener) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContextManager#setDefaultContext(org.talend.core.model.process.IContext)
         */
        @Override
        public void setDefaultContext(IContext context) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.process.IContextManager#setListContext(java.util.List)
         */
        @Override
        public void setListContext(List<IContext> listContext) {
        }

        @Override
        public IContext getContext(String name) {
            return null;
        }

        @Override
        public void saveToEmf(EList contextTypeList) {
        }

        @Override
        public void loadFromEmf(EList contextTypeList, String defaultContextName) {
        }

        @Override
        public boolean sameAs(IContextManager contextManager) {
            return false;
        }

        @Override
        public boolean checkValidParameterName(String oldParameterName, String newParameterName) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void saveToEmf(EList contextTypeList, boolean useInternalId) {
            // TODO Auto-generated method stub

        }
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    /**
     * Return all Nodes of Component type componentName.
     *
     * @param componentName the component name
     * @return all the activated matching nodes in the process
     */
    @Override
    public List<? extends INode> getNodesOfType(String componentName) {
        List<ShadowNode> matchingNodes = new ArrayList<ShadowNode>();
        if ((prejob != null) && (prejob.getComponentName() != null) && (prejob.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(prejob);
        }
        if ((setProxy != null) && (setProxy.getComponentName() != null)
                && (setProxy.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(setProxy);
        }
        if ((inNode != null) && (inNode.getComponentName() != null) && (inNode.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(inNode);
        }
        if ((outNode != null) && (outNode.getComponentName() != null)
                && (outNode.getComponentName().compareTo(componentName) == 0)) {
            matchingNodes.add(outNode);
        }
        return matchingNodes;
    }

    @Override
    public boolean checkValidConnectionName(String connectionName, boolean checkExists) {
        return false;
    }

    public IProcessor getProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProcessor(IProcessor processor) {
        // TODO Auto-generated method stub

    }

    @Override
    public IElementParameter getElementParameter(String name) {
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
        Set<IConnection> matchingNodes = new HashSet<IConnection>();
        if (prejob != null && prejob.getComponentName() != null) {
            matchingNodes.addAll(prejob.getIncomingConnections());
            matchingNodes.addAll(prejob.getOutgoingConnections());
        }
        if (setProxy != null && setProxy.getComponentName() != null) {
            matchingNodes.addAll(setProxy.getIncomingConnections());
            matchingNodes.addAll(setProxy.getOutgoingConnections());
        }
        if ((inNode != null) && (inNode.getComponentName() != null)) {
            matchingNodes.addAll(inNode.getIncomingConnections());
            matchingNodes.addAll(inNode.getOutgoingConnections());
        }
        if ((outNode != null) && (outNode.getComponentName() != null)) {
            matchingNodes.addAll(outNode.getIncomingConnections());
            matchingNodes.addAll(outNode.getOutgoingConnections());
        }
        return matchingNodes.toArray(new IConnection[matchingNodes.size()]);
    }

    @Override
    public Set<String> getNeededLibraries(int options) {
        // TODO Auto-generated method stub
        return null;
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
     * @see org.talend.core.model.process.IProcess#isProcessModified()
     */
    public boolean isProcessModified() {
        return true;
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
}
