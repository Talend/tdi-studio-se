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
package org.talend.repository.resource.ui.action;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.resource.RouteResourceActivator;
import org.talend.repository.resource.i18n.Messages;
import org.talend.repository.resource.ui.util.RouteResourceEditorUtil;
import org.talend.repository.resource.ui.wizards.NewRouteResourceWizard;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * @author xpli
 *
 */
public class CreateRouteResourceAction extends AContextualAction implements ITreeContextualAction {

    public CreateRouteResourceAction() {
        setText(Messages.getString("CreateRouteResourceAction.Title")); //$NON-NLS-1$
        this.setImageDescriptor(RouteResourceActivator.createImageDesc("icons/create-route-resource.png"));
    }

    @Override
    protected void doRun() {

        IRepositoryNode node = null;
        NewRouteResourceWizard wizard = null;
        ISelection selection = getSelection();
        if (selection == null) {
            return;
        }
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        node = (IRepositoryNode) obj;
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IPath path = service.getRepositoryPath(node);
        if (RepositoryConstants.isSystemFolder(path.toString())) {
            // Not allowed to create in system folder.
            return;
        }

        wizard = new NewRouteResourceWizard(path);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
        int open = dlg.open();
        if (open == Window.OK) {
            ResourceItem item = wizard.getItem();
            IWorkbenchPage page = getActivePage();
            RouteResourceEditorUtil.openEditor(page, null, item);
        }
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case SIMPLE_FOLDER:
            case SYSTEM_FOLDER:
                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (nodeType != ERepositoryObjectType.RESOURCES) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

}
