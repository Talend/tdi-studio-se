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
package org.talend.designer.abstractmap.ui.listener;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DropTargetOperationListener.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class DropTargetOperationListener {

    int authorizedOperations = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;

    Transfer[] authorizedTransfers = new Transfer[] { TableEntriesTransfer.getInstance() };

    private DropTargetListener dropTargetListener;

    public DropTargetOperationListener(final AbstractMapperManager mapperManager) {
        super();
        dropTargetListener = new DefaultDropTargetListener(mapperManager);
    }

    /**
     * DOC amaumont Comment method "addControl".
     *
     * @param outputTablesZoneView
     */
    public void addControl(Control control) {
        DropTarget dropTarget = new DropTarget(control, authorizedOperations);
        dropTarget.setTransfer(authorizedTransfers);
        dropTarget.addDropListener(dropTargetListener);
    }

}
