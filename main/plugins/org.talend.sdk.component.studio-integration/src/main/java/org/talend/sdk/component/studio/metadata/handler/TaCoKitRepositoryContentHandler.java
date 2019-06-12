/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.metadata.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.AbstractRepositoryContentHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryTypeProcessor;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.studio.metadata.action.CreateTaCoKitConfigurationAction;
import org.talend.sdk.component.studio.metadata.action.EditTaCoKitConfigurationAction;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationItemModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.util.ETaCoKitImage;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

import orgomg.cwm.foundation.businessinformation.BusinessinformationPackage;

public class TaCoKitRepositoryContentHandler extends AbstractRepositoryContentHandler {

    private XmiResourceManager xmiResourceManager = new XmiResourceManager();

    @Override
    public Resource create(final IProject project, final Item item, final int classifierID, final IPath path)
            throws PersistenceException {
        Resource itemResource = null;
        if (item.eClass() == PropertiesPackage.Literals.CONNECTION_ITEM) {
            try {
                TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel((ConnectionItem) item);
                ERepositoryObjectType type = TaCoKitUtil
                        .getOrCreateERepositoryObjectType(itemModel.getConfigTypeNode());
                itemResource = create(project, (ConnectionItem) item, path, type);
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }

        return itemResource;
    }

    private Resource create(final IProject project, final ConnectionItem item, final IPath path,
            final ERepositoryObjectType type) throws PersistenceException {
        Resource itemResource = xmiResourceManager.createItemResource(project, item, path, type, false);
        itemResource.getContents().add(item.getConnection());

        return itemResource;
    }

    @Override
    public Resource save(final Item item) throws PersistenceException {
        Resource itemResource = null;
        if (item.eClass() == PropertiesPackage.Literals.CONNECTION_ITEM) {
            itemResource = save((ConnectionItem) item);
        }

        return itemResource;
    }

    private Resource save(final ConnectionItem item) {
        Resource itemResource = xmiResourceManager.getItemResource(item);
        itemResource.getContents().clear();
        MetadataManager.addContents(item, itemResource);

        // add to the current resource all Document and Description instances because they are not reference in
        // containment references.
        Map<EObject, Collection<Setting>> externalCrossref =
                EcoreUtil.ExternalCrossReferencer.find(item.getConnection());
        Collection<Object> documents =
                EcoreUtil.getObjectsByType(externalCrossref.keySet(), BusinessinformationPackage.Literals.DOCUMENT);
        for (Object doc : documents) {
            itemResource.getContents().add((EObject) doc);
        }
        Collection<Object> descriptions =
                EcoreUtil.getObjectsByType(externalCrossref.keySet(), BusinessinformationPackage.Literals.DESCRIPTION);
        for (Object doc : descriptions) {
            itemResource.getContents().add((EObject) doc);
        }

        return itemResource;
    }

    @Override
    public Item createNewItem(final ERepositoryObjectType type) {
        Item item = null;

        if (TaCoKitUtil.isTaCoKitType(type)) {
            item = PropertiesFactory.eINSTANCE.createConnectionItem();
        }

        return item;
    }

    @Override
    public boolean isRepObjType(final ERepositoryObjectType type) {
        return TaCoKitUtil.isTaCoKitType(type);
    }

    @Override
    public boolean isProcess(final Item item) {
        return TaCoKitUtil.isTaCoKitType(getRepositoryObjectType(item));
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(final Item item) {
        ERepositoryObjectType type = null;
        if (item.eClass() == PropertiesPackage.Literals.CONNECTION_ITEM) {
            try {
                TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel((ConnectionItem) item);
                type = TaCoKitUtil
                        .getOrCreateERepositoryObjectType(itemModel.getConfigTypeNode());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return type;
    }

    /**
     * Checks whether {@code repositoryType} belongs to TaCoKit and creates RepositoryTypeProcessor if it is true
     * RepositoryTypeProcessor implements repository tree filtering logic, which allows to show only repository nodes,
     * which are related to the component, in repository review dialog.
     *
     * @param repositoryType a String, which represents supported repository nodes types
     * @return RepositoryTypeProcessor or null, it repository type doesn't belong to TaCoKit
     */
    @Override
    public IRepositoryTypeProcessor getRepositoryTypeProcessor(final String repositoryType) {
        if (containsTaCoKitRepositoryType(repositoryType)) {
            if (repositoryType.contains("|")) {
                return new TaCoKitTypeProcessor(repositoryType.split("\\|"));
            } else {
                return new TaCoKitTypeProcessor(new String[] { repositoryType });
            }
        } else {
            return null;
        }
    }

    private boolean containsTaCoKitRepositoryType(final String repositoryTypes) {
        if (repositoryTypes == null) {
            return false;
        }
        String[] typeArray = repositoryTypes.split("\\|"); //$NON-NLS-1$
        for (String type : typeArray) {
            ERepositoryObjectType typeFromKey = ERepositoryObjectType.getTypeFromKey(type);
            if (typeFromKey != null) {
                if (TaCoKitUtil.isTaCoKitType(typeFromKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IImage getIcon(final ERepositoryObjectType type) {
        if (TaCoKitConst.METADATA_TACOKIT.equals(type)) {
            return ETaCoKitImage.TACOKIT_REPOSITORY_ICON;
        }
        return null;
    }

    @Override
    public ERepositoryObjectType getHandleType() {
        return TaCoKitConst.METADATA_TACOKIT;
    }

    @Override
    public void addNode(final ERepositoryObjectType type, final RepositoryNode parentNode,
            final IRepositoryViewObject repositoryObject, final RepositoryNode node) {
        if (TaCoKitUtil.isTaCoKitType(type)) {
            String configId = repositoryObject.getProperty().getId();
            Project project = new Project(ProjectManager.getInstance().getProject(node.getObject().getProperty()));
            List<ConnectionItem> items = new ArrayList<ConnectionItem>();
            try {
                List<IRepositoryViewObject> repObjs = ProxyRepositoryFactory.getInstance().getAll(project, type);
                for (IRepositoryViewObject repObj : repObjs) {
                    try {
                        if (repObj != null && repObj.getProperty() != null) {
                            ConnectionItem item = (ConnectionItem) repObj.getProperty().getItem();
                            if (!items.contains(item) && item.getTypeName().equals(configId)) {
                                items.add(item);
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

            if (items.size() == 0) {
                return;
            }
            for (ConnectionItem item : items) {
                IRepositoryViewObject viewObject = new RepositoryViewObject(item.getProperty());
                RepositoryNode childNode = new RepositoryNode(viewObject, node, ENodeType.REPOSITORY_ELEMENT);
                viewObject.setRepositoryNode(childNode);
                childNode.setProperties(EProperties.LABEL, viewObject.getLabel());
                ERepositoryObjectType repObjType = TaCoKitConst.METADATA_TACOKIT;
                try {
                    TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel(item);
                    repObjType = TaCoKitUtil
                            .getOrCreateERepositoryObjectType(itemModel.getConfigTypeNode());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                childNode.setProperties(EProperties.CONTENT_TYPE, repObjType);
                node.getChildren().add(childNode);
            }
        }
    }

    @Override
    protected void deleteNode(final Item item) throws Exception {
        TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel((ConnectionItem) item);
        ERepositoryObjectType repObjType =
                TaCoKitUtil.getOrCreateERepositoryObjectType(itemModel.getConfigTypeNode());
        RootContainer<String, IRepositoryViewObject> metadata =
                ProxyRepositoryFactory.getInstance().getMetadata(repObjType);
        Map<String, IRepositoryViewObject> idMap = new HashMap<>();
        buildIdMap(metadata, idMap);

        deleteNode((ConnectionItem) item, idMap);
    }

    private void deleteNode(final ConnectionItem item, final Map<String, IRepositoryViewObject> idMap)
            throws Exception {
        if (item == null) {
            return;
        }
        String itemId = item.getProperty().getId();
        if (!idMap.isEmpty()) {
            idMap.values().forEach(repoViewObj -> {
                try {
                    Property property = repoViewObj.getProperty();
                    ConnectionItem connItem = (ConnectionItem) property.getItem();
                    TaCoKitConfigurationModel configuration = new TaCoKitConfigurationModel(connItem.getConnection());
                    if (TaCoKitUtil.equals(itemId, configuration.getParentItemId())) {
                        deleteNode(connItem, idMap);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            });
        }
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IRepositoryViewObject repoViewObj = idMap.get(itemId);
        if (!repoViewObj.isDeleted()) {
            factory.deleteObjectLogical(repoViewObj);
        }
        factory.deleteObjectPhysical(repoViewObj);
    }

    private void buildIdMap(final Container<String, IRepositoryViewObject> metadata,
            final Map<String, IRepositoryViewObject> idMap) {
        if (metadata == null) {
            return;
        }
        List<IRepositoryViewObject> members = metadata.getMembers();
        if (members != null) {
            members.forEach(repViewObj -> idMap.put(repViewObj.getId(), repViewObj));
        }
        List<Container<String, IRepositoryViewObject>> subContainers = metadata.getSubContainer();
        if (subContainers != null) {
            subContainers.forEach(subContainer -> buildIdMap(subContainer, idMap));
        }
    }

    @Override
    public boolean hasSchemas() {
        return true;
    }

    @Override
    public IWizard newWizard(final IWorkbench workbench, final boolean creation, final RepositoryNode node,
            final String[] existingNames) {
        if (!(node instanceof ITaCoKitRepositoryNode)) {
            return null;
        }
        IWorkbench wb = workbench != null ? workbench : PlatformUI.getWorkbench();
        if (creation) {
            try {
                CreateTaCoKitConfigurationAction createTaCoKitConfigurationAction = new CreateTaCoKitConfigurationAction(
                        ITaCoKitRepositoryNode.class.cast(node).getConfigTypeNode());
                createTaCoKitConfigurationAction.init(node);
                return createTaCoKitConfigurationAction.createWizard(wb);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
        } else {
            EditTaCoKitConfigurationAction editTaCoKitConfigurationAction = new EditTaCoKitConfigurationAction();
            editTaCoKitConfigurationAction.init(node);
            return editTaCoKitConfigurationAction.createWizard(wb);
        }
    }

}
