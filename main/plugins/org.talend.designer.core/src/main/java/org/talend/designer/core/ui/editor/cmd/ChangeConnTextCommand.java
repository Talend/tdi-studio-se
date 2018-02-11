// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.ConnectionListController;
import org.talend.designer.core.utils.UpgradeElementHelper;

/**
 * Command that change the label of a connection. <br/>
 * 
 * $Id$
 * 
 */
public class ChangeConnTextCommand extends Command {

    private String newName;

    private String oldName;

    // for elt component
    private String oldMetaName;

    private IConnection connection;

    /**
     * Initialisation of the command with the label of the connection and the new text.
     * 
     * @param connectionLabel Gef object that contains the label of the connection.
     * @param newName new name of the connection label
     */
    public ChangeConnTextCommand(IConnection connection, String newName) {
        this.connection = connection;
        if (newName != null) {
            this.newName = newName;
        } else {
            this.newName = ""; //$NON-NLS-1$
        }
        setLabel(Messages.getString("ChangeConnTextCommand.Label")); //$NON-NLS-1$
    }

    @Override
    public void execute() {
        oldName = connection.getName();
        connection.setName(newName);
        IElementParameter elementParameter = connection.getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (elementParameter != null) {
            // if ("TABLE".equals(connection.getConnectorName())) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.TABLE)) {
                oldMetaName = connection.getMetaName();
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), connection.getMetaName());
            } else {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), newName);
            }
        }
        // connection.setPropertyValue(EParameterName.LABEL.getName(), newName);

        if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
            connection.getSource().getProcess().removeUniqueConnectionName(oldName);
            connection.getSource().getProcess().addUniqueConnectionName(newName);
        }
        ConnectionListController.renameConnectionInElement(oldName, newName, connection.getSource());
        ConnectionListController.renameConnectionInElement(oldName, newName, connection.getTarget());

        IExternalNode externalNode = connection.getTarget().getExternalNode();
        if (externalNode != null) {
            externalNode.renameInputConnection(oldName, newName);
        }
        externalNode = connection.getSource().getExternalNode();
        if (externalNode != null) {
            externalNode.renameOutputConnection(oldName, newName);
        }

        UpgradeElementHelper.renameData(connection.getTarget(), oldName, newName);

        ((Process) connection.getSource().getProcess()).checkProcess();
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        connection.setName(oldName);
        IElementParameter elementParameter = connection.getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (elementParameter != null) {
            // if ("TABLE".equals(connection.getConnectorName())) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.TABLE)) {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), oldMetaName);
            } else {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), oldName);
            }
        }
        if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
            connection.getSource().getProcess().removeUniqueConnectionName(newName);
            connection.getSource().getProcess().addUniqueConnectionName(oldName);
        }
        ConnectionListController.renameConnectionInElement(newName, oldName, connection.getSource());

        IExternalNode externalNode = connection.getTarget().getExternalNode();
        if (externalNode != null) {
            externalNode.renameInputConnection(newName, oldName);
        }
        externalNode = connection.getSource().getExternalNode();
        if (externalNode != null) {
            externalNode.renameOutputConnection(newName, oldName);
        }
        UpgradeElementHelper.renameData(connection.getTarget(), newName, oldName);

        ((Process) connection.getSource().getProcess()).checkProcess();
    }
}
