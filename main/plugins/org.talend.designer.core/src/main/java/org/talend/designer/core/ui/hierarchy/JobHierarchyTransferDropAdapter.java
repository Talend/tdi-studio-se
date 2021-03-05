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
package org.talend.designer.core.ui.hierarchy;

import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.ui.IJobHierarchyViewPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC bqian class global comment. Detailled comment
 */
public class JobHierarchyTransferDropAdapter implements TransferDropTargetListener {

    private IJobHierarchyViewPart part;

    public JobHierarchyTransferDropAdapter(IJobHierarchyViewPart part) {
        this.part = part;
    }

    public Transfer getTransfer() {
        return LocalSelectionTransfer.getInstance();
    }

    public boolean isEnabled(DropTargetEvent event) {
        return true;
    }

    public void dragEnter(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public void dragLeave(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public void dragOperationChanged(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    private IStructuredSelection getSelection() {
        LocalSelectionTransfer transfer = (LocalSelectionTransfer) getTransfer();
        IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
        return selection;
    }

    private void validate(DropTargetEvent event) {
        IStructuredSelection selection = getSelection();
        if (selection.size() == 1) {

            if (selection.getFirstElement() instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) selection.getFirstElement();
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ProcessItem) {
                    return;
                }
            }
        }
        event.detail = DND.DROP_NONE;
    }

    public void dragOver(DropTargetEvent event) {
        validate(event);
    }

    public void drop(DropTargetEvent event) {
        IStructuredSelection selection = getSelection();
        if (selection.size() == 1) {
            if (selection.getFirstElement() instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) selection.getFirstElement();
                Item item = sourceNode.getObject().getProperty().getItem();

                if (item instanceof ProcessItem) {
                    // Process loadedProcess = new Process(((ProcessItem) item).getProperty());
                    // loadedProcess.loadXmlFile();
                    IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                    Process loadedProcess = (Process) designerCoreService.getProcessFromProcessItem((ProcessItem) item);

                    part.setInputProcess(loadedProcess);
                }
            }
        }
    }

    public void dropAccept(DropTargetEvent event) {

    }
}
