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

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditor;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class OpenDiagramAction extends AContextualAction implements IIntroAction {

    private Properties params;

    public OpenDiagramAction() {
        super();
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.BUSINESS_PROCESS_ICON));
        setText(Messages.getString("OpenDiagramAction.EditBusinessModel")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        ISelection selection = getSelectedObject();
        if (selection == null) {
            return;
        }
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) obj;
            IRepositoryViewObject repositoryObject = repositoryNode.getObject();
            Property updatedProperty = null;
            if (repositoryObject instanceof RepositoryObject) {
                RepositoryViewObject abstractRepositoryObject = new RepositoryViewObject(repositoryObject.getProperty());
                updatedProperty = abstractRepositoryObject.getProperty();
            } else if (repositoryObject instanceof RepositoryViewObject) {
                updatedProperty = repositoryObject.getProperty();
            }
            if (updatedProperty != null) {
                BusinessProcessItem businessProcessItem = (BusinessProcessItem) updatedProperty.getItem();
                DiagramResourceManager diagramResourceManager = new DiagramResourceManager(getActivePage(),
                        new NullProgressMonitor());
                IFile file = diagramResourceManager.createDiagramFile();
                diagramResourceManager.updateResource(businessProcessItem, file);
                IEditorPart part = diagramResourceManager.openEditor(businessProcessItem, file, false);

                if (part instanceof BusinessDiagramEditor) {
                    ((BusinessDiagramEditor) part).setLastVersion(true);
                }
                // TDI-21143 : Studio repository view : remove all refresh call to repo view
                // IRepositoryView view = getViewPart();
                // if (view != null) {
                // view.refresh(repositoryNode);
                // }
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
                RepositoryNode repositoryNode = (RepositoryNode) object;
                ERepositoryObjectType nodeType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
                if (repositoryNode.getType() == RepositoryNode.ENodeType.REPOSITORY_ELEMENT) {
                    if (nodeType == ERepositoryObjectType.BUSINESS_PROCESS) {
                        enabled = true;
                    }
                }
                if (enabled
                        && (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()
                                || !ProjectManager.getInstance().isInCurrentMainProject(repositoryNode) || !isLastVersion(repositoryNode))) {
                    enabled = false;
                }
                RepositoryNode parent = repositoryNode.getParent();
                if (enabled && parent != null && parent.isBin()) {
                    enabled = false;
                }
            }
        }

        setEnabled(enabled);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
     */
    @Override
    public Class getClassForDoubleClick() {
        return BusinessProcessItem.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    public void run(IIntroSite site, Properties params) {
        this.params = params;
        PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
        doRun();

    }

    private ISelection getSelectedObject() {
        if (params == null) {
            return getSelection();
        } else {

            IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (null == workbenchWindow) {
                return null;
            }
            IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
            if (null == workbenchPage) {
                return null;
            }

            IPerspectiveDescriptor currentPerspective = workbenchPage.getPerspective();
            if (!IBrandingConfiguration.PERSPECTIVE_DI_ID.equals(currentPerspective.getId())) {
                // show di perspective
                try {
                    workbenchWindow.getWorkbench().showPerspective(IBrandingConfiguration.PERSPECTIVE_DI_ID, workbenchWindow);
                    workbenchPage = workbenchWindow.getActivePage();
                } catch (WorkbenchException e) {
                    ExceptionHandler.process(e);
                    return null;
                }
            }

            RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(params.getProperty("nodeId"), false);
            IRepositoryView viewPart = getViewPart();
            if (repositoryNode != null && viewPart != null) {
                RepositoryNodeUtilities.expandParentNode(viewPart, repositoryNode);
                return new StructuredSelection(repositoryNode);
            }
            return null;
        }
    }
}
