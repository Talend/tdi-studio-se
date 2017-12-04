// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.ConfigExternalLib.ConfigExternalLibWizard;

/**
 * An action used to import external jar. <br/>
 * 
 * $Id: ImportExternalJARAction.java Mar 16, 20074:20:34 PM bqian $
 * 
 */
public class ConfigRoutineLibraryAction extends AContextualAction {

    public ConfigRoutineLibraryAction() {
        super();
        String label = org.talend.repository.i18n.Messages.getString("ConfigRoutineLibraryAction.actionLabel"); //$NON-NLS-1$
        setText(label);
        setDescription(label);
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.IMPORT_JAR));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = selection.size() == 1;
        if (canWork) {
            for (Object o : selection.toArray()) {
                if (o instanceof RepositoryNode) {
                    RepositoryNode node = (RepositoryNode) o;
                    switch (node.getType()) {
                    case REPOSITORY_ELEMENT:
                        ERepositoryObjectType beanType = null;

                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                            ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                                    .getService(ICamelDesignerCoreService.class);
                            beanType = service.getBeansType();
                        }
                        if (beanType != null && node.getObjectType() == beanType) {
                            setText("Edit Bean Libraries");
                        } else if (ERepositoryObjectType.PIG_UDF == node.getObjectType()) {
                            String label = org.talend.repository.i18n.Messages
                                    .getString("ConfigRoutineLibraryAction.pigudf.actionLabel"); //$NON-NLS-1$
                            setText(label);
                        } else {
                            String label = org.talend.repository.i18n.Messages
                                    .getString("ConfigRoutineLibraryAction.actionLabel"); //$NON-NLS-1$
                            setText(label);
                        }

                        if (node.getObjectType() == ERepositoryObjectType.ROUTINES
                                || node.getObjectType() == ERepositoryObjectType.PIG_UDF
                                || (beanType != null && node.getObjectType() == beanType)) {
                            // IRepositoryViewObject repObj = node.getObject();
                            // IProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
                            // ERepositoryStatus status = repFactory.getStatus(repObj);
                            // boolean isEditable = status.isPotentiallyEditable() || status.isEditable();
                            canWork = true;
                        } else {
                            canWork = false;
                        }
                        break;
                    default:
                        canWork = false;
                        break;
                    }
                }
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.AContextualAction#isVisible()
     */
    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        ConfigExternalLibWizard wizard = new ConfigExternalLibWizard();
        IWorkbench workbench = getWorkbench();
        wizard.init(workbench, (IStructuredSelection) getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, wizard);
        dialog.open();
    }

}
