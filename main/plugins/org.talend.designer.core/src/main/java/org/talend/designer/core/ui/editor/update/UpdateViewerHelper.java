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
package org.talend.designer.core.ui.editor.update;

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateManagerHelper;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateViewerHelper {

    private UpdateDetectionDialog updateDialog;

    public UpdateViewerHelper(UpdateDetectionDialog updateDialog) {
        if (updateDialog == null) {
            throw new RuntimeException("The argument can't be null"); //$NON-NLS-1$
        }
        this.updateDialog = updateDialog;
    }

    private CheckboxTreeViewer getViewer() {
        return this.updateDialog.getViewer();
    }

    /**
     *
     * init view state.
     */
    public void initViewerState() {
        if (getViewer() == null) {
            return;
        }

        UpdateContentProvider contentProvider = (UpdateContentProvider) getViewer().getContentProvider();
        List<Job> treeViewerInput = contentProvider.getTreeViewerInput();
        for (Job job : treeViewerInput) {
            updateCheckedState(job, true);
        }
        if (treeViewerInput.size() == 1) {
            getViewer().expandAll();
        }
        refreshDialogStatus();
    }

    /**
     *
     * select all.
     *
     */
    public void selectAll(boolean state) {
        if (getViewer() == null) {
            return;
        }
        UpdateContentProvider contentProvider = (UpdateContentProvider) getViewer().getContentProvider();
        List<Job> treeViewerInput = contentProvider.getTreeViewerInput();
        for (Job job : treeViewerInput) {
            updateCheckedState(job, state);
        }        
        getViewer().refresh(false);
        refreshDialogStatus();
    }
    
    public void refreshDialogStatus() {
        refreshSelectButton();
        checkUpdateStatus();
    }

    public void updateCheckedState(Object obj, boolean state) {
        if (obj instanceof Job) {
            Job job = (Job) obj;
            for (Category category : job.getCategories()) {
                for (Item item : category.getItems()) {
                    checkItemResultState(item, state);
                }
            }
        }
        if (obj instanceof Category) {
            for (Item item : ((Category) obj).getItems()) {
                checkItemResultState(item, state);
            }
        }
        if (obj instanceof Item) {
            checkItemResultState((Item) obj, state);
        }
    }

    public void checkItemResultState(Item item, boolean state) {
        if (item == null) {
            return;
        }
        List<IProcess2> openedProcesses = UpdateManagerUtils.getOpenedProcess();
        boolean isOpened = openedProcesses.contains(item.getResultObject().getJob());
        boolean jobletUpdate = false;
        boolean jobletContext = false;
        if (item.getParent() != null && item.getParent().getNode() != null && (item.getParent().getNode() instanceof Node)
                && ((Node) item.getParent().getNode()).isJoblet()) {
            jobletUpdate = true;
        }
        IUpdateItemType jobletContextType = UpdateManagerProviderDetector.INSTANCE
                .getUpdateItemType(UpdateManagerHelper.TYPE_JOBLET_CONTEXT);
        if (jobletContextType != null && item.getParent() != null && jobletContextType.equals(item.getParent().getType())) {
            jobletContext = true;
        }
        // not checked
        if (!state && !item.getResultObject().isReadOnly()) {
            EUpdateResult resultType = item.getResultObject().getResultType();
            if (resultType == EUpdateResult.UPDATE || resultType == EUpdateResult.ADD || resultType == EUpdateResult.DELETE) {
                item.setChecked(false);
            } else if ((jobletUpdate || jobletContext) && !isOpened
                    && (resultType == EUpdateResult.JOBLET_UPDATE || resultType == EUpdateResult.RELOAD)) {
                item.setChecked(state);
            }
        } else if (item.getResultObject().isReadOnly() && jobletUpdate) {
            return;
        } else {
            // keep the checked
            item.setChecked(true);
        }
    }

    /**
     *
     * ggu Comment method "refreshSelectButton".
     *
     * refresh the label of select button.
     */
    @SuppressWarnings("restriction")
    public void refreshSelectButton() {
        List<UpdateResult> inputElements = updateDialog.getInputElements();
        if (inputElements == null) {
            return;
        }
        int num = 0;
        for (UpdateResult result : inputElements) {
            if (result.isChecked()) {
                num++;
            }
        }
        if (num == inputElements.size()) {
            // updateDialog.setSelectButtonLabel(WorkbenchMessages.SelectionDialog_deselectLabel);
            updateDialog.setSelectButtonLabel(Messages.getString("WorkbenchMessages.SelectionDialog_deselectLabel")); //$NON-NLS-1$
        } else {
            // updateDialog.setSelectButtonLabel(WorkbenchMessages.SelectionDialog_selectLabel);
            updateDialog.setSelectButtonLabel(Messages.getString("WorkbenchMessages.SelectionDialog_selectLabel")); //$NON-NLS-1$
        }

    }

    public void checkUpdateStatus() {
        List<UpdateResult> inputElements = updateDialog.getInputElements();
        if (inputElements != null) {
            for (UpdateResult result : inputElements) {

                if (result.isReadOnlyProcess()) {
                    updateDialog.updateReadOnlyJobWarnMessage();
                    return;
                }

                if (!result.isChecked()
                        && (result.getResultType() == EUpdateResult.UPDATE || result.getResultType() == EUpdateResult.DELETE)) {
                    updateDialog.updateWarnMessage();
                    return;
                }
            }
        }
        updateDialog.updateNomarlMessage();
    }
}

