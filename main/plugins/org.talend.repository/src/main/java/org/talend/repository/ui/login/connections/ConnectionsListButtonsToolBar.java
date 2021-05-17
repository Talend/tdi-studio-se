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
package org.talend.repository.ui.login.connections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.general.ConnectionBean;
import org.talend.repository.i18n.Messages;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ConnectionsListButtonsToolBar extends ExtendedToolbarView {

    private static final String NEW_CONNECTION_LABEL = Messages.getString("ConnectionsListButtonsToolBar.newConnectionLabel"); //$NON-NLS-1$

    private static final String COPY_CONNECTION_LABEL_PREFIX = Messages
            .getString("ConnectionsListButtonsToolBar.copyLabelPrefix"); //$NON-NLS-1$

    public ConnectionsListButtonsToolBar(Composite parent, int style, AbstractExtendedTableViewer extendedTableViewer) {
        super(parent, style, extendedTableViewer);
    }

    @Override
    protected AddPushButton createAddPushButton() {
        return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            @Override
            protected Object getObjectToAdd() {
                ConnectionBean newConnection = new ConnectionBean();
                newConnection.setName(NEW_CONNECTION_LABEL);
                return newConnection;
            }

        };
    }

    @Override
    protected PastePushButton createPastePushButton() {
        return new PastePushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, Integer indexWhereInsert) {
                return new ConnectionPasteCommand(extendedTableModel, indexWhereInsert);
            }

        };
    }

    /**
     * DOC smallet ConnectionsListButtonsToolBar class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
     *
     */
    public class ConnectionPasteCommand extends ExtendedTablePasteCommand {

        public ConnectionPasteCommand(ExtendedTableModel extendedTable, Integer indexStartAdd) {
            super(extendedTable, indexStartAdd);
        }

        public ConnectionPasteCommand(ExtendedTableModel extendedTable) {
            super(extendedTable);
        }

        @Override
        public List createPastableBeansList(ExtendedTableModel extendedTable, List copiedObjectsList) {
            ArrayList list = new ArrayList();
            for (Object current : copiedObjectsList) {
                if (current instanceof ConnectionBean) {
                    ConnectionBean copy;
                    try {
                        copy = ((ConnectionBean) current).clone();
                        copy.setName(COPY_CONNECTION_LABEL_PREFIX + ((ConnectionBean) current).getName());
                        list.add(copy);
                    } catch (CloneNotSupportedException e) {
                        MessageBoxExceptionHandler.process(e);
                    }
                }
            }
            return list;
        }

    }
}
