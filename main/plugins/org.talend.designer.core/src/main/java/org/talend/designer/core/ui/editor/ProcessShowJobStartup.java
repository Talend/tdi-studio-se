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
package org.talend.designer.core.ui.editor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.ui.editor.AbstractShowJobStartup;
import org.talend.designer.core.ui.MultiPageTalendEditor;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ProcessShowJobStartup extends AbstractShowJobStartup {

    @Override
    protected boolean enableForType(ERepositoryObjectType type) {
        // only process the process job.
        return ERepositoryObjectType.PROCESS != null && ERepositoryObjectType.PROCESS.equals(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.editor.AbstractShowJobStartup#processJob(org.talend.core.model.repository.IRepositoryViewObject
     * )
     */
    @Override
    protected void processJob(final IRepositoryViewObject viewObject) {
        if (viewObject == null) {
            return;
        }
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                Item item = viewObject.getProperty().getItem();
                if (!(item instanceof ProcessItem)) {
                    return;
                }
                IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (activeWorkbenchWindow == null) {
                    return;
                }
                IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
                if (page == null) {
                    return;
                }

                try {
                    final ProcessEditorInput fileEditorInput = new ProcessEditorInput((ProcessItem) item, true, true);
                    // find the existed editor
                    IEditorPart editorPart = page.findEditor(fileEditorInput);
                    if (editorPart == null) {
                        fileEditorInput.setRepositoryNode(null);
                        editorPart = page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, true);
                    } else {
                        ((MultiPageTalendEditor) editorPart).setReadOnly(fileEditorInput.setForceReadOnly(false));
                        page.activate(editorPart);
                    }
                } catch (PartInitException e) {
                    MessageBoxExceptionHandler.process(e);
                } catch (PersistenceException e) {
                    MessageBoxExceptionHandler.process(e);
                }

            }

        });
    }

}
