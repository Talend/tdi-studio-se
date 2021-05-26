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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 */
/**
 * DOC Administrator class global comment. Detailed comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public final class ExportItemAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    private static final String EXPORT_ITEM = Messages.getString("ExportItemAction.Label"); //$NON-NLS-1$

    private boolean toolbarAction = false;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        toolbarAction = false;
        boolean visible = false;
        if (selection.isEmpty()) {
            visible = false;
        } else if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            visible = false;
        } else {
            for (Object object : (selection).toArray()) {

                // Avoid to show this action on Node "Generated"/"Jobs" and Node JOB_DOC, JOBLET_DOC.
                RepositoryNode node = (RepositoryNode) object;

                Object nodProperty = node.getProperties(EProperties.CONTENT_TYPE);
                ERepositoryObjectType contentType = node.getContentType();

                if (nodProperty == null
                        || (!nodProperty.equals(ERepositoryObjectType.JOB_DOC)
                                && !nodProperty.equals(ERepositoryObjectType.JOBLET_DOC)
                                && !nodProperty.equals(ERepositoryObjectType.GENERATED)
                                && !nodProperty.equals(ERepositoryObjectType.SQLPATTERNS)
                                && !nodProperty.equals(ERepositoryObjectType.METADATA_CON_CDC)
                                && !nodProperty.equals(ERepositoryObjectType.METADATA_CON_TABLE)
                                && !nodProperty.equals(ERepositoryObjectType.METADATA_CON_QUERY)
                                && !nodProperty.equals(ERepositoryObjectType.SVN_ROOT)
                                && !nodProperty.equals(ERepositoryObjectType.SERVICESOPERATION)
                                && !nodProperty.equals(ERepositoryObjectType.SERVICESPORT)
                                && !nodProperty.equals(ERepositoryObjectType.REFERENCED_PROJECTS))) {
                    visible = true;
                }

                if (node.getObject() != null && ERepositoryObjectType.getAllTypesOfCodes().contains(node.getObjectType())
                        && RoutinesUtil.isInnerCodes(node.getObject().getProperty())) {
                    visible = false;
                }

                if (nodProperty != null
                        && GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    if (nodProperty.equals(camelService.getRouteDocsType()) || nodProperty.equals(camelService.getRouteDocType())) {
                        visible = false;
                    }
                }

                if (nodProperty != null
                        && GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                    ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                            .getDefault().getService(ITestContainerProviderService.class);
                    if ((testContainerService != null) && (nodProperty instanceof ERepositoryObjectType)) {
                        if (testContainerService.isTestContainerType((ERepositoryObjectType) nodProperty)) {
                            visible = false;
                        }
                    }
                }

                // for cdc
                RepositoryNode parent = node.getParent();
                if (ENodeType.STABLE_SYSTEM_FOLDER.equals(node.getType())) {
                    if (parent != null) {
                        RepositoryNode pNode = parent;
                        if (ENodeType.STABLE_SYSTEM_FOLDER.equals(parent.getType())) {
                            pNode = parent.getParent();
                            if (pNode != null && ENodeType.REPOSITORY_ELEMENT.equals(pNode.getType())) {
                                ERepositoryObjectType nodeType = (ERepositoryObjectType) pNode
                                        .getProperties(EProperties.CONTENT_TYPE);
                                if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(nodeType) && pNode.getObject() != null) {
                                    DatabaseConnection connection = (DatabaseConnection) ((DatabaseConnectionItem) pNode
                                            .getObject().getProperty().getItem()).getConnection();
                                    if (connection != null) {
                                        CDCConnection cdcConns = connection.getCdcConns();
                                        if (cdcConns != null) {
                                            visible = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // for validation rule
                if (ERepositoryObjectType.METADATA_VALIDATION_RULES != null
                        && ERepositoryObjectType.METADATA_VALIDATION_RULES.equals(nodProperty)
                        && !isUnderValidationRuleConnection(node)) {
                    visible = false;
                }
                // if (visible && parent != null && parent instanceof BinRepositoryNode) {
                // visible = false;
                // }
            }
        }
        setEnabled(visible);
    }

    public ExportItemAction() {
        super();
        this.setText(EXPORT_ITEM);
        this.setToolTipText(EXPORT_ITEM);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_ICON));
    }

    @Override
    protected void doRun() {
        if (toolbarAction) {
            this.setWorkbenchPart(null);
        }
        IRepositoryView repositoryView = getViewPart();
        if (repositoryView != null && repositoryView.getViewer() instanceof TreeViewer) {
            ((TreeViewer) repositoryView.getViewer()).getTree().setFocus();
        }
        ExportItemWizard wizard = new ExportItemWizard((repositoryView != null ? repositoryView.getViewSite().getId() : null));
        IWorkbench workbench = getWorkbench();
        wizard.setWindowTitle(EXPORT_ITEM);
        if (!toolbarAction) {
            wizard.init(workbench, (IStructuredSelection) this.getSelection());
        } else {
            if (repositoryView != null) {
                IStructuredSelection selection = (IStructuredSelection) repositoryView.getViewer().getSelection();
                wizard.init(workbench, selection);
            }
        }

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, wizard);
        dialog.setBlockOnOpen(false);
        dialog.open();
        if (wizard.isCanceled()) dialog.close();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void run(IAction action) {
        toolbarAction = true;
        run();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
