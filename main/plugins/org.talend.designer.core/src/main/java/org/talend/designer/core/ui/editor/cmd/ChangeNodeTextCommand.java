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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;

/**
 * Command that change the label of a node. <br/>
 *
 * $Id$
 *
 */
public class ChangeNodeTextCommand extends Command {

    private String newName;

    private String oldName;

    private NodeLabel nodeLabel;

    /**
     * Initialisation of the command with the label of the node and the new text.
     *
     * @param nodeLabel Gef object that contains the label of the node.
     * @param newName new name of the node label
     */
    public ChangeNodeTextCommand(NodeLabel nodeLabel, String newName) {
        if (newName != null) {
            this.newName = newName;
        } else {
            this.newName = ""; //$NON-NLS-1$
        }
        this.nodeLabel = nodeLabel;
        setLabel(Messages.getString("ChangeNodeTextCommand.Label")); //$NON-NLS-1$
    }

    private void refreshPropertyView() {
        // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        // PropertySheet sheet = (PropertySheet) view;
        // TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) sheet.getCurrentPage();
        // tabbedPropertySheetPage.refresh();
    }

    public void execute() {
        oldName = (String) nodeLabel.getPropertyValue(EParameterName.LABEL.getName());
        nodeLabel.setLabelText(newName);
        nodeLabel.setPropertyValue(EParameterName.LABEL.getName(), newName);
        refreshPropertyView();
    }

    public void redo() {
        execute();
    }

    public void undo() {
        nodeLabel.setLabelText(oldName);
        nodeLabel.setPropertyValue(EParameterName.LABEL.getName(), oldName);
        refreshPropertyView();
    }
}
