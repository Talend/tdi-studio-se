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
package org.talend.repository.json.node;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.AbstractRepositoryContentHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryTypeProcessor;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.utils.RepositoryNodeManager;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.repository.json.ui.wizards.FileJSONTableWizard;
import org.talend.repository.json.util.JSONImage;
import org.talend.repository.metadata.ui.actions.metadata.CreateTableAction;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.JSONFileConnectionItem;
import org.talend.repository.model.json.JsonFactory;
import org.talend.repository.model.json.JsonPackage;

import orgomg.cwm.foundation.businessinformation.BusinessinformationPackage;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONRepositoryContentHandler extends AbstractRepositoryContentHandler {

    private XmiResourceManager xmiResourceManager = new XmiResourceManager();

    @Override
    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException {
        Resource itemResource = null;
        ERepositoryObjectType type;
        switch (classifierID) {
        case JsonPackage.JSON_FILE_CONNECTION_ITEM:
            if (item != null && item instanceof JSONFileConnectionItem) {
                type = JSONRepositoryNodeType.JSON;
                itemResource = create(project, (JSONFileConnectionItem) item, path, type);
                return itemResource;
            }
        default:
            return itemResource;
        }
    }

    private Resource create(IProject project, JSONFileConnectionItem item, IPath path, ERepositoryObjectType type)
            throws PersistenceException {
        Resource itemResource = xmiResourceManager.createItemResource(project, item, path, type, false);
        MetadataManager.addContents(item, itemResource);

        return itemResource;
    }

    @Override
    public Resource save(Item item) throws PersistenceException {
        Resource itemResource = null;
        EClass eClass = item.eClass();
        if (eClass.eContainer() == JsonPackage.eINSTANCE) {
            switch (eClass.getClassifierID()) {
            case JsonPackage.JSON_FILE_CONNECTION_ITEM:
                itemResource = save((JSONFileConnectionItem) item);
                return itemResource;
            default:
                return null;
            }
        }
        return null;
    }

    private Resource save(JSONFileConnectionItem item) {
        Resource itemResource = xmiResourceManager.getItemResource(item);
        itemResource.getContents().clear();
        MetadataManager.addContents(item, itemResource);

        // add to the current resource all Document and Description instances because they are not reference in
        // containment references.
        Map<EObject, Collection<Setting>> externalCrossref = EcoreUtil.ExternalCrossReferencer.find(item.getConnection());
        Collection<Object> documents = EcoreUtil.getObjectsByType(externalCrossref.keySet(),
                BusinessinformationPackage.Literals.DOCUMENT);
        for (Object doc : documents) {
            itemResource.getContents().add((EObject) doc);
        }
        Collection<Object> descriptions = EcoreUtil.getObjectsByType(externalCrossref.keySet(),
                BusinessinformationPackage.Literals.DESCRIPTION);
        for (Object doc : descriptions) {
            itemResource.getContents().add((EObject) doc);
        }

        return itemResource;
    }

    @Override
    public Item createNewItem(ERepositoryObjectType type) {
        Item item = null;
        if (type == JSONRepositoryNodeType.JSON) {
            item = JsonFactory.eINSTANCE.createJSONFileConnectionItem();
        }

        return item;
    }

    @Override
    public boolean isRepObjType(ERepositoryObjectType type) {
        return type == JSONRepositoryNodeType.JSON;
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(Item item) {
        EClass eClass = item.eClass();
        if (eClass.eContainer() == JsonPackage.eINSTANCE) {
            switch (eClass.getClassifierID()) {
            case JsonPackage.JSON_FILE_CONNECTION_ITEM:
                return JSONRepositoryNodeType.JSON;
            default:
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean isProcess(Item item) {
        if (item instanceof JSONFileConnectionItem) {
            return true;
        }
        return false;
    }

    @Override
    public IImage getIcon(ERepositoryObjectType type) {
        if (type == JSONRepositoryNodeType.JSON) {
            return JSONImage.JSON_ICON;
        }
        return null;
    }

    @Override
    public void addNode(ERepositoryObjectType type, RepositoryNode recBinNode, IRepositoryViewObject repositoryObject,
            RepositoryNode node) {
        if (type == JSONRepositoryNodeType.JSON) {
            JSONFileConnection connection = (JSONFileConnection) ((JSONFileConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            Set<MetadataTable> tableset = ConnectionHelper.getTables(connection);
            for (MetadataTable metadataTable : tableset) {
                if (!SubItemHelper.isDeleted(metadataTable)) {
                    RepositoryNode tableNode = RepositoryNodeManager.createMetatableNode(node, repositoryObject, metadataTable);
                    node.getChildren().add(tableNode);
                    if (metadataTable.getColumns().size() > 0) {
                        RepositoryNodeManager.createColumns(tableNode, repositoryObject, metadataTable);
                    }
                }
            }
        }
    }

    @Override
    public ERepositoryObjectType getHandleType() {
        return JSONRepositoryNodeType.JSON;
    }

    @Override
    public boolean hasSchemas() {
        return true;
    }

    @Override
    public boolean hideAction(IRepositoryNode node, Class actionType) {
        boolean canHandle = false;
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (JSONRepositoryNodeType.JSON.equals(nodeType)) {
            canHandle = true;
        }
        if (!canHandle) {
            if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)
                    || ERepositoryObjectType.METADATA_CON_COLUMN.equals(nodeType)) {
                RepositoryNode parentNode = node.getParent();
                if (parentNode != null && JSONRepositoryNodeType.JSON.equals(parentNode.getProperties(EProperties.CONTENT_TYPE))) {
                    canHandle = true;
                }
            }
        }
        if (canHandle) {
            if (actionType == CreateTableAction.class) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isOwnTable(IRepositoryNode node, Class type) {
        if (type != JSONFileConnection.class) {
            return false;
        }
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (nodeType == ERepositoryObjectType.METADATA_CON_TABLE) {
            RepositoryNode repNode = node.getParent();
            nodeType = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
            if (nodeType == JSONRepositoryNodeType.JSON) {
                return true;
            }
        } else if (nodeType == ERepositoryObjectType.METADATA_CON_COLUMN) {
            RepositoryNode repNode = node.getParent().getParent().getParent();
            nodeType = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
            if (nodeType == JSONRepositoryNodeType.JSON) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IWizard newWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        if (node == null) {
            return null;
        }
        if (workbench == null) {
            workbench = PlatformUI.getWorkbench();
        }
        return null;
        // return new HDFSWizard(workbench, creation, node, existingNames);
    }

    @Override
    public IWizard newSchemaWizard(IWorkbench workbench, boolean creation, IRepositoryViewObject object,
            MetadataTable metadataTable, String[] existingNames, boolean forceReadOnly) {
        if (object == null) {
            return null;
        }
        if (workbench == null) {
            workbench = PlatformUI.getWorkbench();
        }

        return new FileJSONTableWizard(workbench, creation, (ConnectionItem) object.getProperty().getItem(),
                metadataTable, forceReadOnly);
    }

    @Override
    public ERepositoryObjectType getProcessType() {
        return JSONRepositoryNodeType.JSON;
    }

    @Override
    public IRepositoryTypeProcessor getRepositoryTypeProcessor(String repositoryType) {
        ERepositoryObjectType processType = getProcessType();
        if (repositoryType != null && processType != null && repositoryType.contains(processType.getType())) {
            return new JSONRepositoryTypeProcessor(repositoryType);
        }

        return null;
    }

}
