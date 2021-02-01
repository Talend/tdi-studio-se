// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.repository.i18n.Messages;
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
        boolean canWork = false;
        if (selection.size() == 1) {
            Object o = selection.getFirstElement();
            if (o instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) o;
                switch (node.getType()) {
                case REPOSITORY_ELEMENT:
                    ERepositoryObjectType type = node.getObjectType();
                    String label = null;
                    if (type == ERepositoryObjectType.BEANS) {
                        label = "Edit Bean Libraries"; //$NON-NLS-1$
                    } else if (type == ERepositoryObjectType.BEANSJAR) {
                        label = "Edit Bean Jar Libraries"; //$NON-NLS-1$
                    } else if (type == ERepositoryObjectType.ROUTINESJAR) {
                        label = "Edit Routine Jar Libraries"; //$NON-NLS-1$
                    } else if (type == ERepositoryObjectType.ROUTINES) {
                        label = Messages.getString("ConfigRoutineLibraryAction.actionLabel"); //$NON-NLS-1$
                    }
                    setText(label);
                    if (ERepositoryObjectType.getAllTypesOfCodesJar().contains(type)) {
                        canWork = true;
                    } else if (ERepositoryObjectType.getAllTypesOfCodes().contains(type)) {
                        canWork = !RoutinesUtil.isInnerCodes(node.getObject().getProperty());
                    }
                    break;
                default:
                    canWork = false;
                    break;
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
