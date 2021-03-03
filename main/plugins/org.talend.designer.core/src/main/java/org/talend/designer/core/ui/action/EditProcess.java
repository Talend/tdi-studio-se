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
package org.talend.designer.core.ui.action;

import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class EditProcess extends AbstractProcessAction implements IIntroAction {

    private static final String EDIT_LABEL = Messages.getString("EditProcess.editJob"); //$NON-NLS-1$

    private static final String OPEN_LABEL = Messages.getString("EditProcess.openJob"); //$NON-NLS-1$

    private Properties params;

    public EditProcess() {
        super();
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            final IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
            if (RepositoryManager.isOpenedItemInEditor(node.getObject())) {
                canWork = false;
            } else {
                if (IRepositoryNode.ENodeType.REPOSITORY_ELEMENT == node.getType() && node.getObjectType() == getProcessType()) {
                    final IProxyRepositoryFactory repFactory = DesignerPlugin.getDefault().getRepositoryService()
                            .getProxyRepositoryFactory();
                    this.setText(getLabel(repFactory.isPotentiallyEditable(node.getObject())) + " : " + node.getLabel());
                } else {
                    canWork = false;
                }
                if (canWork && node.getObject() != null
                        && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
                    canWork = false;
                }
                if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                    canWork = false;
                }

                // If the editProcess action canwork is true, then detect that the job version is the latest verison or
                // not.
                if (canWork) {
                    canWork = isLastVersion(node);
                }
            }
        }
        setEnabled(canWork);
    }

    protected ERepositoryObjectType getProcessType() {
        return ERepositoryObjectType.PROCESS;
    }

    protected String getLabel(boolean editable) {
        return editable ? EDIT_LABEL : OPEN_LABEL;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        IRepositoryNode node = getSelectedObject();
        if (node == null) {
            return;
        }
        Property property = node.getObject().getProperty();
        if (property != null) {
            Assert.isTrue(property.getItem() instanceof ProcessItem);

            ProcessItem processItem = (ProcessItem) property.getItem();

            IWorkbenchPage page = getActivePage();

            try {
                final JobEditorInput fileEditorInput = getEditorInput(processItem);
                checkUnLoadedNodeForProcess(fileEditorInput);

                final IEditorPart editorPart = page.findEditor(fileEditorInput);
                if (editorPart == null) {
                    fileEditorInput.setRepositoryNode(node);
                    page.openEditor(fileEditorInput, getEditorId(), true);
                } else {
                    ((AbstractMultiPageTalendEditor) editorPart).setReadOnly(fileEditorInput.setForceReadOnly(false));
                    page.activate(editorPart);
                }
            } catch (PartInitException e) {
                MessageBoxExceptionHandler.process(e);
            } catch (PersistenceException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
    }

    protected JobEditorInput getEditorInput(final ProcessItem processItem) throws PersistenceException {
        // return new ProcessEditorInput(processItem, true, true,null); //same
        return new ProcessEditorInput(processItem, true, true);
    }

    protected String getEditorId() {
        return MultiPageTalendEditor.ID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
     */
    @Override
    public Class getClassForDoubleClick() {
        return ProcessItem.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    @Override
    public void run(IIntroSite site, Properties params) {
        this.params = params;
        PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
        doRun();
    }

    protected String getPerspectiveId() {
        return IBrandingConfiguration.PERSPECTIVE_DI_ID;
    }

    private IRepositoryNode getSelectedObject() {
        if (params == null) {
            ISelection selection = getSelection();
            if (selection != null) {
                return (IRepositoryNode) ((IStructuredSelection) selection).getFirstElement();
            }
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
            if (!getPerspectiveId().equals(currentPerspective.getId())) {
                // show di perspective
                try {
                    workbenchWindow.getWorkbench().showPerspective(getPerspectiveId(), workbenchWindow);
                    workbenchPage = workbenchWindow.getActivePage();
                } catch (WorkbenchException e) {
                    ExceptionHandler.process(e);
                    return null;
                }
            }

            IRepositoryNode repositoryNode = RepositorySeekerManager.getInstance().searchRepoViewNode(
                    params.getProperty("nodeId"), false);
            IRepositoryView viewPart = getViewPart();
            if (repositoryNode != null && viewPart != null) {
                RepositoryNodeUtilities.expandParentNode(viewPart, repositoryNode);
                return repositoryNode;
            }
        }
        return null;
    }
}
