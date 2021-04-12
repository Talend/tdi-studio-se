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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ISQLPatternSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.StandAloneTalendJavaEditor;
import org.talend.designer.core.ui.wizards.SaveAsSQLPatternWizard;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * DOC xtan class global comment. <br/>
 */
public class SaveAsSQLPatternAction extends Action {

    private final EditorPart editorPart;

    public SaveAsSQLPatternAction(EditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public void run() {
        SaveAsSQLPatternWizard processWizard = new SaveAsSQLPatternWizard(editorPart);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), processWizard);
        if (dlg.open() == Window.OK) {

            try {

                SQLPatternItem sqlpatternItem = processWizard.getSQLPatternItem();

                // get the IFile
                ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                        ICodeGeneratorService.class);
                // only for talend java version
                ISQLPatternSynchronizer sqlPatternSynchronizer = service.getSQLPatternSynchronizer();
                IFile file = sqlPatternSynchronizer.getSQLPatternFile(sqlpatternItem);
                if (file == null) {
                    return;
                }
                // Set readonly to false since created job will always be editable.
                RepositoryEditorInput repositoryEditorInput = new RepositoryEditorInput(file, sqlpatternItem);

                IWorkbenchPage page = getActivePage();

                IRepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(repositoryEditorInput.getItem()
                        .getProperty().getId(), false);
                repositoryEditorInput.setRepositoryNode(repositoryNode);

                // here really do the normal save as function
                IDocumentProvider provider = ((StandAloneTalendJavaEditor) this.editorPart).getDocumentProvider();
                provider.aboutToChange(repositoryEditorInput);
                provider.saveDocument(null, repositoryEditorInput, provider.getDocument(this.editorPart.getEditorInput()), true);
                provider.changed(repositoryEditorInput);

                // copy back from the *.java file to *.item file.
                // @see:StandAloneTalendJavaEditor.doSave(IProgressMonitor monitor)
                ByteArray byteArray = sqlpatternItem.getContent();
                byteArray.setInnerContentFromFile(repositoryEditorInput.getFile());
                IProxyRepositoryFactory repFactory = DesignerPlugin.getDefault().getRepositoryService()
                        .getProxyRepositoryFactory();
                repFactory.save(sqlpatternItem);

                // close the old editor
                page.closeEditor(this.editorPart, false);

                // open the new editor, because at the same time, there will update the jobSetting/componentSetting view
                page.openEditor(repositoryEditorInput, StandAloneTalendJavaEditor.ID, true);

            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "SQLTemplate could not be saved" + " : "
                        + e.getMessage());
                ExceptionHandler.process(e);
            }
        }
    }

    private IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

}
