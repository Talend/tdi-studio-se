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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
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
        
        updateELTElementParameter(connection.getSource(),newName);
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

    public void updateELTElementParameter(INode iNode, String newName) {
        // TUP-29072 Solution :if link name equals the schema.tablename,when edit on the link,should update the default
        // schema/default table in the input component , then the table name will updated automatically in the sql.The
        // solution will only work for simple cases. For complex case with java code and global map in link/default
        // schema/default table , it won't work.
        if (iNode != null && iNode.isELTComponent()) {
            boolean update = false;
            String defaultSchemaName = null;
            String defaultTableName = null;
            if (StringUtils.isNotBlank(newName)) {
                String newNameTemp = newName;
                int newNameLength = newNameTemp.length();
                // Name cases:context.a.context.b /context.a.b /a.context.b /a.b /b
                if (ContextParameterUtils.isContainContextParam(newNameTemp)) {
                    if (newNameTemp.startsWith(ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX)) {
                        int contextPrefixLength = ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX.length();
                        int index = newNameTemp.indexOf(".", contextPrefixLength);//$NON-NLS-1$
                        if (index > contextPrefixLength) {
                            defaultSchemaName = newNameTemp.substring(0, index);
                            defaultTableName = newNameTemp.substring(index + 1, newNameLength);
                            update = true;
                        }
                    } else {
                        int index = newNameTemp.indexOf(".");//$NON-NLS-1$
                        if (index > 0) {
                            defaultSchemaName = newNameTemp.substring(0, index);
                            defaultTableName = newNameTemp.substring(index + 1, newNameLength);
                            update = true;
                        }
                    }
                } else {
                    String[] names = newNameTemp.split("\\.");//$NON-NLS-1$
                    if (names.length == 2) {
                        defaultSchemaName = names[0];
                        defaultTableName = names[1];
                        update = true;
                    }
                }
            }
            if (update) {
                IElementParameter schemaParam = iNode.getElementParameter("ELT_SCHEMA_NAME");//$NON-NLS-1$
                IElementParameter tableParam = iNode.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
                if (schemaParam != null && StringUtils.isNotBlank(defaultSchemaName)) {
                    schemaParam.setValue(TalendTextUtils.addQuotes(defaultSchemaName));
                }
                if (tableParam != null && StringUtils.isNotBlank(defaultTableName)) {
                    tableParam.setValue(TalendTextUtils.addQuotes(defaultTableName));
                }
            }
        }
    }
}
