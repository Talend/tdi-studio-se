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
package org.talend.designer.xmlmap.ui.tabs.table.toolbar;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedControlViewer;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.designer.xmlmap.ui.tabs.table.TreeSchemaTableEditor;

/**
 * created by Administrator on 2013-1-25 Detailled comment
 *
 */
public class NextPageButton extends ExtendedPushButton {

    /**
     * DOC Administrator NextPageButton constructor comment.
     *
     * @param parent
     * @param extendedViewer
     * @param tooltip
     * @param image
     */
    public NextPageButton(Composite parent, AbstractExtendedControlViewer extendedViewer) {
        super(parent, extendedViewer, "Next Page", ImageProvider.getImage(EImage.RIGHTX_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton#getCommandToExecute()
     */
    @Override
    protected Command getCommandToExecute() {
        return null;
    }

    @Override
    public AbstractExtendedTableViewer getExtendedControlViewer() {
        return (AbstractExtendedTableViewer) super.getExtendedControlViewer();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton#handleSelectionEvent(org.eclipse.swt
     * .widgets.Event)
     */
    @Override
    protected void handleSelectionEvent(Event event) {
        TreeSchemaTableEditor extendedControlModel = (TreeSchemaTableEditor) getExtendedControlViewer().getExtendedControlModel();
        // avoid the selection event ,or it will be slow
        getExtendedControlViewer().getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(false);
        extendedControlModel.setCurrentPage(extendedControlModel.getCurrentPage() + 1);
        getExtendedControlViewer().getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(true);
        // only select the first column if change page
        getExtendedControlViewer().getTableViewerCreator().getSelectionHelper().setSelection(new int[] { 0 });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton#getEnabledState()
     */
    @Override
    public boolean getEnabledState() {
        TreeSchemaTableEditor extendedControlModel = (TreeSchemaTableEditor) getExtendedControlViewer().getExtendedControlModel();
        if (extendedControlModel == null) {
            return false;
        }
        if (extendedControlModel.getPageCount() < 2) {
            getButton().setVisible(false);
        } else {
            getButton().setVisible(true);
        }
        if (extendedControlModel != null && extendedControlModel.getCurrentPage() < extendedControlModel.getPageCount()) {
            return true;
        }
        return false;
    }

    @Override
    protected void handleModelChange() {
        super.handleModelChange();
        TreeSchemaTableEditor extendedControlModel = (TreeSchemaTableEditor) getExtendedControlViewer().getExtendedControlModel();
        if (extendedControlModel.getPageCount() < 2) {
            getButton().setVisible(false);
        }
    }

}
