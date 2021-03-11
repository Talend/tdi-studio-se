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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.wizards.SaveAsProcessWizard;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * DOC xtan class global comment. <br/>
 */
public class SaveAsProcessAction extends Action {

    private final EditorPart editorPart;

    public SaveAsProcessAction(EditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public void run() {
        SaveAsProcessWizard processWizard = new SaveAsProcessWizard(editorPart);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), processWizard);
        if (dlg.open() == Window.OK) {

            try {

                // Set readonly to false since created job will always be editable.
                JobEditorInput newJobEditorInput = new ProcessEditorInput(processWizard.getProcess(), true, true, false);

                IWorkbenchPage page = getActivePage();

                IRepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(newJobEditorInput.getItem()
                        .getProperty().getId(), false);
                newJobEditorInput.setRepositoryNode(repositoryNode);

                // close the old editor
                page.closeEditor(((AbstractTalendEditor) this.editorPart).getParent(), false);

                // open the new editor, because at the same time, there will update the jobSetting/componentSetting view
                page.openEditor(newJobEditorInput, MultiPageTalendEditor.ID, true);

            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error",
                        "Job could not be saved" + " : " + e.getMessage());
                ExceptionHandler.process(e);
            }
        }
    }

    private IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

}
