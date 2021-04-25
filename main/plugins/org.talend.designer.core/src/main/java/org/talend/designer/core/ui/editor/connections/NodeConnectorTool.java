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
package org.talend.designer.core.ui.editor.connections;

import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class NodeConnectorTool {

    private NodePart nodePart;

    public NodeConnectorTool(NodePart nodePart) {
        this.nodePart = nodePart;
    }

    public INodeConnector getConnector() {
        Node node = (Node) nodePart.getModel();
        return NodeUtil.getValidConnector(node);
    }

}
