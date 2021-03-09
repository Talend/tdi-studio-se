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
package org.talend.repository.ui.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IElement;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryTypeProcessor;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class MultiTypesProcessor implements IRepositoryTypeProcessor {

    protected String[] repositoryTypes;

    protected List<ERepositoryObjectType> typesToShow;

    protected Map<String, Object> attributesMap;

    public MultiTypesProcessor(String[] repositoryTypes) {
        super();
        this.repositoryTypes = repositoryTypes;
    }

    protected String[] getRepositoryTypes() {
        return repositoryTypes;
    }

    protected abstract List<ERepositoryObjectType> getTypes();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryTypeProcessor#getShowTypes()
     */
    @Override
    public List<ERepositoryObjectType> getShowRootTypes() {
        if (typesToShow == null) {
            List<ERepositoryObjectType> types = getTypes();
            typesToShow = new ArrayList<ERepositoryObjectType>();
            // add fixed type to typesToShow
            typesToShow.add(ERepositoryObjectType.REFERENCED_PROJECTS);
            // add types and it's parent type to typesToShow
            for (ERepositoryObjectType type : types) {
                if (type != null && !typesToShow.contains(type)) {
                    typesToShow.add(type);
                    addParentTypes(typesToShow, type);
                }
            }
        }

        return typesToShow;
    }

    private void addParentTypes(List<ERepositoryObjectType> typesToShow, ERepositoryObjectType typeToCheck) {
        if (typeToCheck.getParentTypesArray() != null) {
            for (ERepositoryObjectType type : typeToCheck.getParentTypesArray()) {
                if (!typesToShow.contains(type)) {
                    typesToShow.add(type);
                }
                addParentTypes(typesToShow, type);
            }
        }
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        Object nodeType = node.getProperties(EProperties.CONTENT_TYPE);
        List<ERepositoryObjectType> types = getTypes();
        if (types != null) {
            for (ERepositoryObjectType type : types) {
                if (nodeType == type) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ViewerFilter makeFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (parentElement instanceof TreePath) {
                    parentElement = ((TreePath) parentElement).getFirstSegment();
                }
                if (parentElement instanceof RepositoryNode && element instanceof RepositoryNode) {
                    RepositoryNode repNode = (RepositoryNode) element;
                    if (repNode.getParent() instanceof ProjectRepositoryNode) {
                        // filter root nodes by type:
                        ERepositoryObjectType type = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
                        if (!getShowRootTypes().contains(type)) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                    return selectRepositoryNode(viewer, (RepositoryNode) parentElement, (RepositoryNode) element);
                } else {
                    return false;
                }
            }
        };
    }

    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node == null) {
            return false;
        }
        IRepositoryViewObject object = node.getObject();
        if (object != null) {
            // column
            if (object instanceof MetadataColumnRepositoryObject) {
                return false;
            }
        }
        // hide the column folder
        if (object == null && node.getParent() != null && node.getParent().getObject() != null
                && node.getParent().getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
            return false;
        }
        // cdc
        ICDCProviderService cdcService = null;
        if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_CDC) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        if (PluginChecker.isCDCPluginLoaded()) {
            cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
            if (cdcService != null && cdcService.isSubscriberTableNode(node)) {
                return false;
            }
        }
        return true;
    }

    protected final boolean isCDCConnection(RepositoryNode node) {
        ICDCProviderService service = null;
        if (PluginChecker.isCDCPluginLoaded()) {
            service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
        }
        if (node != null && node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            List<IRepositoryNode> children = node.getChildren();
            if (children != null) {
                for (IRepositoryNode child : children) {
                    if (service != null && service.isSystemSubscriberTable((RepositoryNode) child)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    @Override
    public String getDialogTitle() {
        return null;
    }

    @Override
    public ILabelProvider getLabelProvider(IElement elem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
        this.attributesMap = attributes;
    }

}
