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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * cli class global comment. Detailled comment
 */
public class SynchronizeSchemaHelper {

    public static Command createCommand(Node node, IElementParameter param) {
        IMetadataTable meta = node.getMetadataFromConnector(param.getContext());
        IMetadataTable metaCopy = meta.clone(true);

        boolean inputFound = false;
        for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
            if (connec.isActivate() && connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                    || connec.getLineStyle().equals(EConnectionType.TABLE)
                    || connec.getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
                if (connec.getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
                    if (connec.getInputId() == 1) {
                        // MetadataTool.copyTable(connec.getMetadataTable().clone(), metaCopy);
                        MetadataToolHelper.copyTable(meta.getDbms(), connec.getMetadataTable().clone(), metaCopy);
                        inputFound = true;
                        break;
                    }
                } else {
                    // MetadataTool.copyTable(connec.getMetadataTable().clone(), metaCopy);
                    MetadataToolHelper.copyTable(meta.getDbms(), connec.getMetadataTable().clone(), metaCopy);
                    inputFound = true;
                }
            }
        }
        if (!inputFound) {
            List<IMetadataColumn> columnsToRemove = new ArrayList<IMetadataColumn>();
            for (IMetadataColumn column : metaCopy.getListColumns()) {
                if (!column.isCustom()) {
                    columnsToRemove.add(column);
                }
            }
            metaCopy.getListColumns().removeAll(columnsToRemove);
        }

        return new ChangeMetadataCommand(node, param, meta, metaCopy);
    }
}
