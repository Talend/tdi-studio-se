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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: EditProcess.java 1495 2007-01-18 04:31:30Z nrousseau $
 *
 */
public class ReadProcess extends AbstractProcessAction {

    private static final String LABEL = Messages.getString("ReadProcess.label"); //$NON-NLS-1$

    public ReadProcess() {
        super();

        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();

        RepositoryNode node = (RepositoryNode) obj;
        ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();

        IWorkbenchPage page = getActivePage();

        try {
            ProcessEditorInput fileEditorInput = new ProcessEditorInput(processItem, true, null, true);
            checkUnLoadedNodeForProcess(fileEditorInput);
            IEditorPart editorPart = page.findEditor(fileEditorInput);

            if (editorPart == null) {
                fileEditorInput.setRepositoryNode(node);
                page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, true);
            } else {
                page.activate(editorPart);
            }
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            if (RepositoryManager.isOpenedItemInEditor(node.getObject())) {
                canWork = false;
            } else {
                switch (node.getType()) {
                case REPOSITORY_ELEMENT:
                    if (node.getObjectType() != ERepositoryObjectType.PROCESS) {
                        canWork = false;
                    }
                    break;
                default:
                    canWork = false;
                }
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.LOCK_BY_USER) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }
}
