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
package org.talend.designer.business.diagram.custom.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditor;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: OpenDiagramAction.java 2145 2007-02-23 16:23:19Z mhelleboid $
 *
 */
public class ReadDiagramAction extends AContextualAction {

    private RepositoryNode repositoryNode;

    public ReadDiagramAction() {
        super();
        setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
        setText(Messages.getString("OpenDiagramAction.ReadBusinessModel")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    protected void doRun() {
        if (repositoryNode == null && getSelection() != null) {
            Object firstElement = ((IStructuredSelection) getSelection()).getFirstElement();
            if (firstElement instanceof RepositoryNode) {
                repositoryNode = (RepositoryNode) firstElement;
            }
        }
        RepositoryNode node = repositoryNode;
        if (node != null) {
            IRepositoryViewObject repositoryObject = node.getObject();

            Property updatedProperty = repositoryObject.getProperty();
            if (updatedProperty != null) {
                BusinessProcessItem businessProcessItem = (BusinessProcessItem) updatedProperty.getItem();
                DiagramResourceManager diagramResourceManager = new DiagramResourceManager(getActivePage(),
                        new NullProgressMonitor());
                IFile file = diagramResourceManager.createDiagramFile();
                diagramResourceManager.updateResource(businessProcessItem, file);
                IEditorPart part = diagramResourceManager.openEditor(businessProcessItem, file, true);

                if (part instanceof BusinessDiagramEditor) {
                    ((BusinessDiagramEditor) part).setLastVersion(true);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean enabled = false;

        if (!selection.isEmpty() && selection.size() == 1) {
            Object object = selection.getFirstElement();
            if (object instanceof RepositoryNode) {
                this.repositoryNode = (RepositoryNode) object;
                ERepositoryObjectType nodeType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
                if (repositoryNode.getType() == RepositoryNode.ENodeType.REPOSITORY_ELEMENT) {
                    if (nodeType == ERepositoryObjectType.BUSINESS_PROCESS) {
                        enabled = true;
                    }
                }
            }
        }
        setEnabled(enabled);
    }
}
