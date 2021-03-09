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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.services.IUIRefresher;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.resource.RouteResourceActivator;
import org.talend.repository.resource.editors.input.RouteResourceInput;
import org.talend.repository.resource.ui.wizards.OpenAnotherVersionResrouceWizard;
import org.talend.repository.ui.actions.EditPropertiesAction;

/**
 *
 * @author xpli
 *
 */
public class OpenAnotherVersionResourceAction extends EditPropertiesAction {

	public OpenAnotherVersionResourceAction() {
        setText("Open another version");
        setToolTipText("Open another version");
        setImageDescriptor(RouteResourceActivator.createImageDesc("icons/open-another-version.png"));
	}

    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1
            && !ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject();
        if (canWork) {
            Object o = selection.getFirstElement();
            if (o instanceof IRepositoryNode) {
                final IRepositoryNode node = (IRepositoryNode) o;
                canWork = node.getType() == ENodeType.REPOSITORY_ELEMENT
                        && node.getObjectType() == ERepositoryObjectType.RESOURCES
                    && node.getObject().getRepositoryStatus() != ERepositoryStatus.DELETED
                    && ProjectManager.getInstance().isInCurrentMainProject(node)
                    && isLastVersion(node);
            }
        }
        setEnabled(canWork);
    }

	@Override
	protected void doRun() {
		final IRepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();

		IPath path = RepositoryNodeUtilities.getPath(node);
		String originalName = node.getObject().getLabel();

		RepositoryObject repositoryObj = new RepositoryObject(node.getObject()
				.getProperty());
		repositoryObj.setRepositoryNode(node.getObject().getRepositoryNode());
		OpenAnotherVersionResrouceWizard wizard = new OpenAnotherVersionResrouceWizard(
				repositoryObj);
		WizardDialog dialog = new WizardDialog(
				Display.getCurrent().getActiveShell(), wizard);
		dialog.setPageSize(300, 250);
		dialog.setTitle("Open another version"); //$NON-NLS-1$
		if (dialog.open() == Dialog.OK) {
			refresh(node);
			// refresh the corresponding editor's name
			IEditorPart part = getCorrespondingEditor(node);
			if (part != null && part instanceof IUIRefresher) {
				((IUIRefresher) part).refreshName();
			} else {
				processRoutineRenameOperation(originalName, node, path);
			}
		}

	}

	@Override
	public Class<?> getClassForDoubleClick() {
        return ResourceItem.class;
	}

	protected IEditorPart getCorrespondingEditor(RepositoryNode node) {
		for (IEditorReference ref : getActivePage().getEditorReferences()) {
			try {
				IEditorInput input = ref.getEditorInput();
				if (!(input instanceof RouteResourceInput)) {
					continue;
				}

				RouteResourceInput repositoryInput = (RouteResourceInput) input;
				if (repositoryInput.getItem().equals(
						node.getObject().getProperty().getItem())) {
					return ref.getEditor(false);
				}
			} catch (PartInitException e) {
				continue;
			}
		}
		return null;
	}

}
