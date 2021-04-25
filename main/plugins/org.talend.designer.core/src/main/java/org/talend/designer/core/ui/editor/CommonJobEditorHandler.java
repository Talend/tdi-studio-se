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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.editor.IJobEditorHandler;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.MultiPageTalendEditor;

/**
 * DOC marvin class global comment. Detailled comment
 */
public class CommonJobEditorHandler implements IJobEditorHandler {

    /**
     * DOC marvin CommonJobEditorInputFactory constructor comment.
     */
    public CommonJobEditorHandler() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.IJobEditorInputFactory#createJobEditorInput(org.talend.core.model.properties
     * .Item, boolean)
     */
    @Override
    public JobEditorInput createJobEditorInput(Item item, boolean flag) throws PersistenceException {
        JobEditorInput jobEditorInput = null;
        if (item != null && item instanceof ProcessItem) {
            jobEditorInput = new ProcessEditorInput((ProcessItem) item, flag);
        }
        return jobEditorInput;
    }

    @Override
    public void openJobEditor(JobEditorInput jobEditorInput) throws PersistenceException, PartInitException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (jobEditorInput != null) {
            IEditorPart editorPart = page.findEditor(jobEditorInput);

            if (editorPart == null) {
                page.openEditor(jobEditorInput, MultiPageTalendEditor.ID, true);
            } else {
                page.activate(editorPart);
            }
        }
    }

}
