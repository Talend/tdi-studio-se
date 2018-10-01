package org.talend.repository.generic.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.AbstractRepositoryContentHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.utils.RepositoryNodeManager;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataPackage;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.ui.GenericConnWizard;
import org.talend.repository.generic.ui.GenericSchemaWizard;
import org.talend.repository.generic.ui.common.GenericWizardDialog;
import org.talend.repository.generic.util.GenericConnectionUtil;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.StableRepositoryNode;

import orgomg.cwm.objectmodel.core.Package;

/**
 * 
 * created by ycbai on 2015年10月15日 Detailled comment
 *
 */
public class GenericRepositoryContentHandler extends AbstractRepositoryContentHandler {

    @Override
    public boolean isProcess(Item item) {
        if (item.eClass() == GenericMetadataPackage.Literals.GENERIC_CONNECTION_ITEM) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isRepObjType(ERepositoryObjectType type) {
        return GenericWizardServiceFactory.getGenericWizardService().isGenericType(type);
    }

    @Override
    public ERepositoryObjectType getProcessType() {
        return null;
    }

    @Override
    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException {
        Resource itemResource = null;
        if (isProcess(item)) {
            itemResource = create(project, (GenericConnectionItem) item, path);
        }

        return itemResource;
    }

    private Resource create(IProject project, GenericConnectionItem item, IPath path) throws PersistenceException {
        XmiResourceManager xmiResourceManager = ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider()
                .getResourceManager();
        ERepositoryObjectType repObjType = getRepositoryObjectType(item);
        Resource itemResource = null;
        if (repObjType != null) {
            itemResource = xmiResourceManager.createItemResource(project, item, path, repObjType, false);
            MetadataManager.addContents(item, itemResource);
            itemResource.getContents().add(item.getConnection());
        }

        return itemResource;
    }

    @Override
    public Resource save(Item item) throws PersistenceException {
        Resource itemResource = null;
        if (isProcess(item)) {
            if (ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
                GenericConnectionUtil.synNamePropertyWithItem((GenericConnectionItem) item);
            }
            itemResource = save((GenericConnectionItem) item);
        }

        return itemResource;
    }

    private Resource save(GenericConnectionItem item) {
        XmiResourceManager xmiResourceManager = ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider()
                .getResourceManager();
        Resource itemResource = xmiResourceManager.getItemResource(item);
        itemResource.getContents().clear();
        MetadataManager.addContents(item, itemResource);

        return itemResource;
    }

    @Override
    public IImage getIcon(ERepositoryObjectType type) {
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                    IGenericDBService.class);
        }
        if(dbService != null && dbService.getExtraTypes().contains(type)){
            return ECoreImage.METADATA_CONNECTION_ICON;
        }
        return null;
    }

    @Override
    public Item createNewItem(ERepositoryObjectType type) {
        GenericConnectionItem item = null;
        if (isRepObjType(type)) {
            item = GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();
            item.setTypeName(type.getType());
        }

        return item;
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(Item item) {
        ERepositoryObjectType type = null;
        if (isProcess(item)) {
            GenericConnectionItem connectionItem = (GenericConnectionItem) item;
            type = ERepositoryObjectType.valueOf(connectionItem.getTypeName());
        }

        return type;
    }

    @Override
    public boolean hasSchemas() {
        return true;
    }

    @Override
    public void addNode(ERepositoryObjectType type, RepositoryNode recBinNode, IRepositoryViewObject repositoryObject,
            RepositoryNode node) {
        if (isRepObjType(type) || isRepObjType(repositoryObject.getRepositoryObjectType())) {
            Item item = repositoryObject.getProperty().getItem();
            if (item instanceof ConnectionItem) {
                ConnectionItem connItem = (ConnectionItem) item;
                Connection connection = (Connection) connItem.getConnection();
                if(SchemaUtils.isExtraDBType(repositoryObject.getRepositoryObjectType())){
                    createTables(node, repositoryObject, connection);
                }else{
                    createSubNodes(node, repositoryObject, connection);
                }
            }
        }
    }
    
    private void createTables(RepositoryNode parentNode, IRepositoryViewObject repObj, Connection connection){
        List<MetadataTable> metadataTables = ConnectionHelper.getTablesWithOrders(connection);
        Map<String,RepositoryNode> nodeMap = createTableParentNode(parentNode, metadataTables);
        for (MetadataTable metadataTable : metadataTables) {
            RepositoryNode parent = parentNode;
            if(metadataTable.getTableType() != null){
                parent = nodeMap.get(metadataTable.getTableType());
            }else{
                parent = nodeMap.get(MetadataManager.TYPE_TABLE);
            }
            createTable(parent, repObj, metadataTable);
        }
    }
    
    private Map<String,RepositoryNode> createTableParentNode(RepositoryNode parentNode, List<MetadataTable> metadataTables){
        Map<String,RepositoryNode> nodeMap = new HashMap<String,RepositoryNode>();
        for(MetadataTable table:metadataTables){
            String type = table.getTableType();
            if(type != null && type.equals(MetadataManager.TYPE_TABLE) && nodeMap.get(type) == null){
                RepositoryNode tablesNode = new StableRepositoryNode(parentNode,
                        Messages.getString("ProjectRepositoryNode.tableSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
                parentNode.getChildren().add(tablesNode);
                nodeMap.put(type, tablesNode);
            }else if(type != null && type.equals(MetadataManager.TYPE_VIEW) && nodeMap.get(type) == null){
                RepositoryNode viewsNode = new StableRepositoryNode(parentNode,
                        Messages.getString("ProjectRepositoryNode.viewSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
                parentNode.getChildren().add(viewsNode);
                nodeMap.put(type, viewsNode);
            }
        }
        return nodeMap;
    }

    private void createSubNodes(RepositoryNode parentNode, IRepositoryViewObject repObj, Package pack) {
        List<SubContainer> subContainers = PackageHelper. getOwnedElements(pack, SubContainer.class);
        if (subContainers.size() > 0) {
            for (SubContainer subContainer : subContainers) {
                RepositoryNode subContainerNode = createSubContainerNode(parentNode, subContainer.getName());
                createSubNodes(subContainerNode, repObj, subContainer);
            }
        } else {
            List<MetadataTable> metadataTables = PackageHelper.getOwnedElements(pack, MetadataTable.class);
            for (MetadataTable metadataTable : metadataTables) {
                createTable(parentNode, repObj, metadataTable);
            }
        }
    }

    // FIXME: need to improve after...
    private RepositoryNode createSubContainerNode(RepositoryNode parentNode, String containerName) {
        RepositoryNode subContainerNode = new StableRepositoryNode(parentNode, containerName, ECoreImage.FOLDER_CLOSE_ICON);
        parentNode.getChildren().add(subContainerNode);
        return subContainerNode;
    }

    private void createTable(RepositoryNode parentNode, IRepositoryViewObject repObj, MetadataTable metadataTable) {
        if (!SubItemHelper.isDeleted(metadataTable)) {
            RepositoryNode tableNode = RepositoryNodeManager.createMetatableNode(parentNode, repObj, metadataTable);
            parentNode.getChildren().add(tableNode);
            if (metadataTable.getColumns().size() > 0) {
                RepositoryNodeManager.createColumns(tableNode, repObj, metadataTable);
            }
        }
    }

    @Override
    public IWizard newWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        if (node == null) {
            return null;
        }
        IWorkbench wb = workbench;
        if (wb == null) {
            wb = PlatformUI.getWorkbench();
        }

        ComponentWizard componentWizard = null;
        if (!creation) {
            componentWizard = GenericConnectionUtil.getEditWizard(node);
        }

        return new GenericConnWizard(wb, creation, componentWizard, node, existingNames);
    }

    @Override
    public IWizard newSchemaWizard(IWorkbench workbench, boolean creation, IRepositoryViewObject object,
            MetadataTable metadataTable, String[] existingNames, boolean forceReadOnly) {
        if (object == null) {
            return null;
        }
        IWorkbench wb = workbench;
        if (wb == null) {
            wb = PlatformUI.getWorkbench();
        }
        MetadataTable table = metadataTable;
        if (table == null && object instanceof MetadataTableRepositoryObject) {
            MetadataTableRepositoryObject metaTableRepObj = (MetadataTableRepositoryObject) object;
            table = metaTableRepObj.getTable();
        }
        if (table == null) {
            return null;
        }table.eContainer();
        ConnectionItem connectionItem = (ConnectionItem) object.getProperty().getItem();
        table = SchemaUtils.getMetadataTable(connectionItem.getConnection(), table, object);
        return new GenericSchemaWizard(wb, creation, object, connectionItem, table, forceReadOnly);
    }

    @Override
    public int openWizardDialog(IRepositoryNode repoNode, IWizard wizard) {
        WizardDialog wizardDialog = new GenericWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                wizard, GenericWizardServiceFactory.getGenericWizardInternalService().getComponentService());
        wizardDialog.setPageSize(780, 540);
        wizardDialog.create();
        return wizardDialog.open();
    }
}
