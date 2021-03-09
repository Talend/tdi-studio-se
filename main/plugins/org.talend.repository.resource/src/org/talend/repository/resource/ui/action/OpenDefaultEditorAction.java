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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.resource.RouteResourceActivator;
import org.talend.repository.resource.i18n.Messages;
import org.talend.repository.resource.ui.util.RouteResourceEditorUtil;
import org.talend.repository.ui.actions.AContextualAction;

public class OpenDefaultEditorAction extends AContextualAction {

//	private Properties params;

	public OpenDefaultEditorAction() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.action.Action#run()
	 */
	protected void doRun() {
		ISelection selection = getSelection();
		if (selection == null) {
			return;
		}
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null) {
			return;
		}
		RepositoryNode node = (RepositoryNode) obj;
		opendTextEditor(node);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Class getClassForDoubleClick() {
        return ResourceItem.class;
	}

//	private ISelection getSelectedObject() {
//		if (params == null) {
//			return getSelection();
//		}
//		else {
//			RepositoryNode repositoryNode = RepositoryNodeUtilities
//					.getRepositoryNode(params.getProperty("nodeId"), false); //$NON-NLS-1$
//			IRepositoryView viewPart = getViewPart();
//			if (repositoryNode != null && viewPart != null) {
//				RepositoryNodeUtilities.expandParentNode(viewPart,
//						repositoryNode);
//				return new StructuredSelection(repositoryNode);
//			}
//			return null;
//		}
//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse
	 * .jface.viewers.TreeViewer,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(TreeViewer viewer, IStructuredSelection selection) {
		boolean canWork = !selection.isEmpty() && selection.size() == 1;
		if (canWork) {
			Object o = selection.getFirstElement();
			RepositoryNode node = (RepositoryNode) o;
			switch (node.getType()) {
			case REPOSITORY_ELEMENT:
                if (node.getObjectType() != ERepositoryObjectType.RESOURCES) {
					canWork = false;
				} else {
					IRepositoryService service = DesignerPlugin.getDefault()
							.getRepositoryService();
					IProxyRepositoryFactory repFactory = service
							.getProxyRepositoryFactory();
					IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
					if (!factory.isUserReadOnlyOnCurrentProject() && repFactory.isPotentiallyEditable(node.getObject())) {
						this.setText(Messages.getString("OpenDefaultEditorAction_title")); //$NON-NLS-1$
					} else {
						this.setText(Messages.getString("OpenDefaultEditorAction_title_read")); //$NON-NLS-1$
					}
				}
				break;
			default:
				canWork = false;
			}
			RepositoryNode parent = node.getParent();
			if (canWork && parent != null
					&& parent instanceof BinRepositoryNode) {
				canWork = false;
			}

			// If the editProcess action canwork is true, then detect that the
			// job version is the latest verison or not.
			if (canWork) {
				canWork = isLastVersion(node);
			}

		}
		setEnabled(canWork);

		this.setToolTipText(Messages.getString("OpenDefaultEditorAction_tooltip")); //$NON-NLS-1$
		this.setImageDescriptor(RouteResourceActivator
				.createImageDesc("icons/edit-resource.png")); //$NON-NLS-1$
	}

	/**
	 * Open or bind RouteResourceEditor
	 *
	 * @param node
	 */
	private void opendTextEditor(RepositoryNode node) {

		Property property = (Property) node.getObject().getProperty();
        ResourceItem item = null;
		if (property != null) {

            Assert.isTrue(property.getItem() instanceof ResourceItem);
            item = (ResourceItem) property.getItem();
			IWorkbenchPage page = getActivePage();
			RouteResourceEditorUtil.openDefaultEditor(page, node, item);
		}

	}
}
