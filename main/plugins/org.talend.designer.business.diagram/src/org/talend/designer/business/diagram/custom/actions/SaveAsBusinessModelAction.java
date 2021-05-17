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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditor;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * DOC xtan class global comment. <br/>
 */
public class SaveAsBusinessModelAction extends Action {

    private final EditorPart editorPart;

    public SaveAsBusinessModelAction(EditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public void run() {
        SaveAsBusinessModelWizard businessModelWizard = new SaveAsBusinessModelWizard(editorPart);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), businessModelWizard);
        if (dlg.open() == Window.OK) {

            try {

                BusinessProcessItem businessProcessItem = businessModelWizard.getBusinessProcessItem();

                IRepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(businessProcessItem.getProperty()
                        .getId(), false);

                // because step1, the fresh will unload the resource(EMF), so, assign a new one...
                businessProcessItem = (BusinessProcessItem) repositoryNode.getObject().getProperty().getItem();

                IWorkbenchPage page = getActivePage();

                DiagramResourceManager diagramResourceManager = new DiagramResourceManager(page, new NullProgressMonitor());
                IFile file = businessModelWizard.getTempFile();
                // Set readonly to false since created job will always be editable.
                RepositoryEditorInput newBusinessModelEditorInput = new RepositoryEditorInput(file, businessProcessItem);

                newBusinessModelEditorInput.setRepositoryNode(repositoryNode);

                // here really do the normal save as function
                IDocumentProvider provider = ((BusinessDiagramEditor) this.editorPart).getDocumentProvider();

                provider.aboutToChange(newBusinessModelEditorInput);
                provider.saveDocument(null, newBusinessModelEditorInput, provider.getDocument(this.editorPart.getEditorInput()),
                        true);
                provider.changed(newBusinessModelEditorInput);

                // copy back from the *.business_diagram file to *.item file.
                // @see:BusinessDiagramEditor.doSave(IProgressMonitor progressMonitor)
                diagramResourceManager.updateFromResource(businessProcessItem, newBusinessModelEditorInput.getFile());

                // notice: here, must save it, save the item to disk, otherwise close the editor
                // without any modification, there won't save the
                // model again, so, will lost the graphic when reopen it.
                ProxyRepositoryFactory.getInstance().save(businessProcessItem);

                // close the old editor
                page.closeEditor(this.editorPart, false);

                // open the new editor, because at the same time, there will update the jobSetting/componentSetting view
                IEditorPart openEditor = page.openEditor(newBusinessModelEditorInput, BusinessDiagramEditor.ID, true);

            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Business model could not be saved"
                        + " : " + e.getMessage());
                ExceptionHandler.process(e);
            }
        }
    }

    private IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

}
