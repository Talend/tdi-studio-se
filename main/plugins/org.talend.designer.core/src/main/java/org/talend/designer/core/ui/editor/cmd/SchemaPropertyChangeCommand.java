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

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will change the schema in the property. <br/>
 *
 * $Id$
 *
 * @deprecated
 */
public class SchemaPropertyChangeCommand extends PropertyChangeCommand {

    IMetadataTable repositoryMetadataTable;

    IMetadataTable nodeMetadataTable, oldNodeMetadataTable;

    Node node;

    public SchemaPropertyChangeCommand(Node node, String propName, Object propValue, IMetadataTable repositoryMetadataTable) {
        super(node, propName, propValue);
        this.node = node;
        this.repositoryMetadataTable = repositoryMetadataTable;
        nodeMetadataTable = node.getMetadataList().get(0);
        oldNodeMetadataTable = nodeMetadataTable.clone();
    }

    @Override
    public void execute() {
        nodeMetadataTable.setListColumns(repositoryMetadataTable.getListColumns());
        nodeMetadataTable.setComment(repositoryMetadataTable.getComment());
        super.execute();
        ((Process) node.getProcess()).checkProcess();
    }

    @Override
    public void undo() {
        nodeMetadataTable.setListColumns(oldNodeMetadataTable.getListColumns());
        nodeMetadataTable.setComment(oldNodeMetadataTable.getComment());
        super.undo();
        ((Process) node.getProcess()).checkProcess();
    }
}
