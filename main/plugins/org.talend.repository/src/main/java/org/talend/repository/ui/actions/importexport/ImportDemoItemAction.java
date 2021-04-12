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
package org.talend.repository.ui.actions.importexport;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.actions.importproject.DemoProjectBean;
import org.talend.repository.ui.actions.importproject.ImportDemoProjectItemsWizard;
import org.talend.repository.ui.actions.importproject.ImportProjectsUtilities;
import org.talend.repository.ui.views.IRepositoryView;

public class ImportDemoItemAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    private static final String IMPORT_DEMO = Messages.getString("ImportDemoAction.Label"); //$NON-NLS-1$

    private boolean toolbarAction = true;

    public ImportDemoItemAction() {
        super();
        this.setText(IMPORT_DEMO);
        this.setToolTipText(IMPORT_DEMO);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.IMPORT_ICON));
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        toolbarAction = false;
        boolean canWork = false;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (selection.size() == 1) {
            if (selection.getFirstElement() instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) selection.getFirstElement();
                if (factory.isUserReadOnlyOnCurrentProject()
                        || !ProjectManager.getInstance().isInCurrentMainProject(repositoryNode)) {
                    setEnabled(false);
                    return;
                }
                if (repositoryNode.getType().equals(ENodeType.SYSTEM_FOLDER)
                        || repositoryNode.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    canWork = true;
                }
                if (repositoryNode.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
                    canWork = false;
                }
                if (repositoryNode.getContentType() == ERepositoryObjectType.SVN_ROOT) {
                    canWork = false;
                }
                if (repositoryNode.getObject() != null
                        && repositoryNode.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED) {
                    canWork = false;
                }
            }
        }
        setEnabled(canWork);

    }

    @Override
    protected void doRun() {
        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            return;
        }

        // qli modified to fix the bug "6999".
        IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
        if (repositoryView != null && repositoryView.getViewer() instanceof TreeViewer) {
            ((TreeViewer) repositoryView.getViewer()).getTree().setFocus();
        }

        ISelection selection = this.getSelection();
        RepositoryNode rNode = null;
        if (toolbarAction) {
            if (repositoryView != null) {
                selection = repositoryView.getViewer().getSelection();
            }
        } else if ((selection instanceof IStructuredSelection)
                && (((IStructuredSelection) selection).getFirstElement() instanceof RepositoryNode)) {
            Object o = ((IStructuredSelection) selection).getFirstElement();
            if (o instanceof RepositoryNode) {
                rNode = (RepositoryNode) o;
            }
        }
        final List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();
        ImportDemoProjectItemsWizard wizard = new ImportDemoProjectItemsWizard(demoProjectList);
        wizard.setWindowTitle(IMPORT_DEMO);
        wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, wizard);
        dialog.open();

    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    @Override
    public void run(IAction action) {
        doRun();

    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void init(IWorkbenchWindow window) {

    }

}
