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
package org.talend.designer.dbmap.external.converter;

import java.util.List;

import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalNode;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: ExternalUtilities.java 1366 2007-01-10 08:40:28Z nrousseau $
 *
 */
public class ExternalNodeUtils {

    /**
     * DOC amaumont Comment method "prepareExternalNodeReadyToOpen".
     *
     * @param node
     * @param externalNode
     * @return
     */
    public static IExternalNode prepareExternalNodeReadyToOpen(IExternalNode externalNode) {

        IODataComponentContainer inAndOut = new IODataComponentContainer();

        List<IODataComponent> inputs = inAndOut.getInputs();
        if (externalNode.getIncomingConnections() != null) {
            for (IConnection currentConnection : externalNode.getIncomingConnections()) {
                IODataComponent component = new IODataComponent(currentConnection);
                inputs.add(component);
            }
        }

        List<IODataComponent> outputs = inAndOut.getOuputs();
        if (externalNode.getOutgoingConnections() != null) {

            for (IConnection currentConnection : externalNode.getOutgoingConnections()) {
                IODataComponent component = new IODataComponent(currentConnection);
                outputs.add(component);
            }
        }

        externalNode.setIODataComponents(inAndOut);

        return externalNode;
    }
}
