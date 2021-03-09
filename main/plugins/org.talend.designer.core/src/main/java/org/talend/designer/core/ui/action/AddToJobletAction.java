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

import java.util.List;
import java.util.Map;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Action to remove a component the joblet. hwang class global comment. Detailled comment
 */
public abstract class AddToJobletAction extends SelectionAction {

    public AddToJobletAction(IWorkbenchPart part) {
        super(part);
        // TODO Auto-generated constructor stub
    }

    public abstract List<Node> getJobletNodeList();

    public abstract Map<INode, IConnection> getJobletNodeMap();
}
