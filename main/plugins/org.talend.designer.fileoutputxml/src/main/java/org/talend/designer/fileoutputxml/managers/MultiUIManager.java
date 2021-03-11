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
package org.talend.designer.fileoutputxml.managers;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;

/**
 * wzhang class global comment. Detailled comment
 */
public class MultiUIManager extends UIManager {

    /**
     * wzhang MultiUIManager constructor comment.
     *
     * @param foxManager
     */
    public MultiUIManager(FOXManager foxManager) {
        super(foxManager);
    }

    protected void getMappableNode(Element node, List<FOXTreeNode> mappableNodes) {
        if (node.getElementChildren().size() == 0) {
            if (node.getColumn() == null) {
                mappableNodes.add(node);
            }
        }
        for (FOXTreeNode attri : node.getAttributeChildren()) {
            if (attri.getColumn() == null) {
                mappableNodes.add(attri);
            }
        }
        for (FOXTreeNode elem : node.getElementChildren()) {
            getMappableNode((Element) elem, mappableNodes);
        }
    }

    /**
     *
     * DOC wzhang Comment method "autoMap". for multiSchema.
     */
    public void autoMap() {
        List<FOXTreeNode> treeData = foxManager.getTreeData(foxManager.getCurrentSchema());
        if (treeData == null || treeData.size() < 1) {
            return;
        }
        FOXTreeNode root = treeData.get(0);
        List<FOXTreeNode> mappableNodes = new ArrayList<FOXTreeNode>();
        getMappableNode((Element) root, mappableNodes);

        IConnection connection = foxUI.getConnection();
        if (connection != null) {
            IMetadataTable metadataTable = connection.getMetadataTable();
            if (metadataTable != null) {
                List<IMetadataColumn> schemaData = metadataTable.getListColumns();
                for (FOXTreeNode node : mappableNodes) {
                    for (IMetadataColumn column : schemaData) {
                        if (node.getLabel().equals(column.getLabel())) {
                            node.setTable(metadataTable);
                            node.setColumn(column);
                            node.setDefaultValue(null);
                            node.setRow(foxManager.getCurrentSchema());
                            break;
                        }
                    }
                }
            }
        }

        this.foxUI.refreshXMLViewer(root);
        this.foxUI.redrawLinkers();
    }
}
