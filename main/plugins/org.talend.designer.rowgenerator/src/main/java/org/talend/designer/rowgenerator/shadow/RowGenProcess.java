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
package org.talend.designer.rowgenerator.shadow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: RowGenProcess.java,v 1.5 2007/02/02 08:24:44 pub Exp $
 *
 */
public class RowGenProcess extends Element implements IProcess {

    private static final String LOGROW = "tLogRow"; //$NON-NLS-1$

    // properties

    protected List<Element> elem = new ArrayList<Element>();

    protected List<INode> nodes = new ArrayList<INode>();

    private boolean activate = true;

    private String name = "RowGenPreviewProcess"; //$NON-NLS-1$

    // list where is stored each unique name for the connections
    private List<String> uniqueConnectionNameList = new ArrayList<String>();

    // list where is stored each unique name for the nodes
    private List<String> uniqueNodeNameList = new ArrayList<String>();

    private boolean readOnly;

    private GraphicalViewer viewer = null;

    private IContextManager contextManager;

    private Property property;

    private boolean initDone = false;

    private ShadowConnection cnx;

    private LogRowNode logRowNode;

    private RowGeneratorComponent component;

    public RowGenProcess(RowGeneratorComponent component) {
        // NRO save the language in the process and load/test it.
        this.component = component;
        contextManager = new RowGenContextManager();
        nodes.add(component);

        logRowNode = new LogRowNode(LOGROW, component.getMetadataList().get(0));
        // nodes.add(logRowNode);

        cnx = new ShadowConnection(component, logRowNode);
        initOutCompnoent(component, cnx);
        logRowNode.setInCnx(cnx);

        component.setProcess(this);
        logRowNode.setProcess(this);
        createProcessParameters();
        setFlags(component);

    }

    /**
     * qzhang Comment method "setFlags".
     *
     * @param component
     */
    private void setFlags(RowGeneratorComponent component) {
        if (!component.isStart()) {
            component.setStart(true);
        }
        if (!component.isActivate()) {
            component.setActivate(true);
        }
        if (!component.isSubProcessStart()) {
            component.setSubProcessStart(true);
        }
        if (component.isReadOnly()) {
            component.setReadOnly(false);
        }
    }

    /**
     * qzhang Comment method "initOutCompnoent".
     *
     * @param component
     * @param cnx
     */
    private void initOutCompnoent(RowGeneratorComponent component, ShadowConnection cnx) {
        // final List<? extends IConnection> outgoingConnections = component.getOutgoingConnections();
        // boolean hasLogRow = false;
        // for (IConnection connection : outgoingConnections) {
        // INode node = connection.getTarget();
        // EmfComponent ec = (EmfComponent) node.getComponent();
        // if (LOGROW.equals(ec.getName())) {
        // hasLogRow = true;
        // }
        // }
        // if (outgoingConnections == null || outgoingConnections.size() == 0 || !hasLogRow) {
        ooutput = component.getOutgoingConnections();

        List<IConnection> cnxs = new ArrayList<IConnection>();
        cnxs.add(cnx);
        component.setOutgoingConnections(cnxs);
        // }

    }

    private List<? extends IConnection> ooutput;

    public RowGenProcess(Property property, RowGeneratorComponent component) {
        this(component);
        this.property = property;
        init();
    }

    private void init() {
        if (!initDone) {
            setId(property.getId());
            setLabel(property.getLabel());
            setVersion(property.getVersion());
            setAuthor(property.getAuthor());
            setStatusCode(property.getStatusCode());
            if (getStatusCode() == null) {
                setStatusCode(""); //$NON-NLS-1$
            }
            initDone = true;
        }
    }

    /*
     * (non-Java)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
        return result;
    }

    /*
     * (non-Java)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RowGenProcess other = (RowGenProcess) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    /**
     * Add all parameters for a process.
     */
    private void createProcessParameters() {
    }

    /**
     * Get the list of all elements, Node and Connection.
     *
     * @return
     */
    public List getElements() {
        return this.elem;
    }

    @Override
    public List<? extends INode> getGraphicalNodes() {
        return nodes;
    }

    @Override
    public List<? extends INode> getGeneratingNodes() {
        return nodes;
    }

    public void setViewer(GraphicalViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Returns true if the grid is enabled.
     *
     * @return
     */
    public boolean isGridEnabled() {
        if (viewer == null) {
            if (viewer == null) {
                return (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            }
        } else {
            return (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
        }
        return false;
    }

    /**
     * Returns true if the SnapToGeometry is enabled.
     *
     * @return
     */
    public boolean isSnapToGeometryEnabled() {
        if (viewer == null) {
            if (viewer == null) {
                return (Boolean) viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            }
        } else {
            return (Boolean) viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
        }
        return false;
    }

    /**
     * Checks if there are unloaded nodes.If there are some nodes unloaded, throws PersistenceException.
     *
     * @throws PersistenceException PersistenceException
     */
    public void checkLoadNodes() throws PersistenceException {

    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void checkReadOnly() {
        this.setReadOnly(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getName() {
        return this.getProperty().getLabel();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getAuthor()
     */
    public User getAuthor() {
        return getProperty().getAuthor();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getId()
     */
    @Override
    public String getId() {
        return getProperty().getId();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getLabel()
     */
    public String getLabel() {
        return getProperty().getLabel();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getStatus()
     */
    public String getStatusCode() {
        return getProperty().getStatusCode();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getVersion()
     */
    @Override
    public String getVersion() {
        return getProperty().getVersion();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setAuthor(org.talend.core.model.temp.User)
     */
    public void setAuthor(User author) {
        getProperty().setAuthor(author);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setId(int)
     */
    public void setId(String id) {
        getProperty().setId(id);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        getProperty().setLabel(label);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setStatus(org.talend.core.model.process.EProcessStatus)
     */
    public void setStatusCode(String statusCode) {
        // setPropertyValue(EParameterName.STATUS.getName(), statusCode);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setVersion(int)
     */
    public void setVersion(String version) {
        getProperty().setVersion(version);
    }

    public boolean isActivate() {
        return activate;
    }

    @Override
    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    /**
     * Check if the given name will be unique in the process. If another link already exists with that name, false will
     * be returned.
     *
     * @param uniqueName
     * @param checkEsists
     * @return true if the name is unique
     */
    @Override
    public boolean checkValidConnectionName(String connectionName, boolean checkExists) {
        return true;
    }

    /**
     * Check if the given name will be unique in the process. If another link already exists with that name, false will
     * be returned.
     *
     * @param uniqueName
     * @return true if the name is unique
     */
    @Override
    public boolean checkValidConnectionName(String connectionName) {
        return checkValidConnectionName(connectionName, true);
    }

    /**
     * Manage to find a unique name with the given name.
     *
     * @param titleName
     */
    @Override
    public String generateUniqueConnectionName(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException(Messages.getString("RowGenProcess.BeseName.BeNull")); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidConnectionName(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    @Override
    public void addUniqueConnectionName(String uniqueConnectionName) {
        if (uniqueConnectionName != null) {
            if (checkValidConnectionName(uniqueConnectionName)) {
                uniqueConnectionNameList.add(uniqueConnectionName);
            } else {
                throw new IllegalArgumentException(
                        Messages.getString("RowGenProcess.ConnectionName.NotValid") + uniqueConnectionName); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void removeUniqueConnectionName(String uniqueConnectionName) {
        if (uniqueConnectionName != null) {
            uniqueConnectionNameList.remove(uniqueConnectionName);
        }
    }

    public String generateUniqueNodeName(INode node) {
        String baseName = node.getComponent().getName();
        if (baseName == null) {
            throw new IllegalArgumentException(Messages.getString("RowGenProcess.ComponentName.BeNull")); //$NON-NLS-1$
        }
        String uniqueName = baseName + "_" + 1; //$NON-NLS-1$

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = uniqueNodeNameList.contains(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + "_" + counter++; //$NON-NLS-1$
        }
        return uniqueName;
    }

    /**
     * This function will take a unique name and update the list with the given name. This function should be private
     * only and should be called only when the xml file is loaded.
     *
     * @param uniqueName
     */
    public void addUniqueNodeName(final String uniqueName) {
        if (!uniqueNodeNameList.contains(uniqueName)) {
            uniqueNodeNameList.add(uniqueName);
        }
    }

    public void removeUniqueNodeName(final String uniqueName) {
        if (!uniqueName.equals("")) { //$NON-NLS-1$
            uniqueNodeNameList.remove(uniqueName);
        }
    }

    /**
     * nrousseau Comment method "checkProcess".
     *
     * @param propagate
     */

    @Override
    public String toString() {
        return "Process:" + getLabel(); //$NON-NLS-1$
    }

    public ERepositoryObjectType getType() {
        return ERepositoryObjectType.PROCESS;
    }

    @Override
    public IContextManager getContextManager() {
        return contextManager;
    }

    // MHE remove
    public Date getCreationDate() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Date getModificationDate() {
        return null;
    }

    public String getPurpose() {
        return null;
    }

    public void setCreationDate(Date value) {
    }

    public void setDescription(String value) {
    }

    public void setModificationDate(Date value) {
    }

    public void setPurpose(String value) {
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getChildren()
     */
    public List<IRepositoryViewObject> getChildren() {
        return null;
    }

    /**
     * Return all Nodes of Component type componentName.
     *
     * @param componentName the component name
     * @return all the activated matching nodes in the process
     */
    @Override
    public List<? extends INode> getNodesOfType(String componentName) {
        return new ArrayList<INode>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getAllConnections(java.lang.String)
     */
    @Override
    public IConnection[] getAllConnections(String filter) {
        return new IConnection[0];
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return name;
    }

    public void reconnection() {
        component.getOutgoingConnections().remove(cnx);
        component.setOutgoingConnections(ooutput);
    }

    public IProcessor getProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProcessor(IProcessor processor) {
        // TODO Auto-generated method stub

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
    public void setRepositoryNode(IRepositoryNode node) {
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
