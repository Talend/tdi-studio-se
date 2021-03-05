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
package org.talend.designer.core.model.components;

import java.util.List;

import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IExternalNode;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ExternalUtilities {

    public static IExternalNode getExternalNodeReadyToOpen(Node node) {
        IExternalNode externalNode = node.getExternalNode();

        if (externalNode == null) {
            return null;
        }
        externalNode.setExternalData(node.getExternalData());
        externalNode.setOriginalNode(node);
        IODataComponentContainer inAndOut = new IODataComponentContainer();

        List<IODataComponent> inputs = inAndOut.getInputs();
        for (IConnection currentConnection : node.getIncomingConnections()) {
            if (currentConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent component = new IODataComponent(currentConnection);
                inputs.add(component);
            }
        }
        List<IODataComponent> outputs = inAndOut.getOuputs();
        for (IConnection currentConnection : node.getOutgoingConnections()) {
            if (currentConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent component = new IODataComponent(currentConnection, currentConnection.getMetadataTable().clone(
                        true));
                outputs.add(component);
            }
        }

        externalNode.setIODataComponents(inAndOut);

        return externalNode;
    }
}
