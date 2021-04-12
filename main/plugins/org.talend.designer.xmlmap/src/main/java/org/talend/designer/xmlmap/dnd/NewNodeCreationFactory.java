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
package org.talend.designer.xmlmap.dnd;

import org.eclipse.gef.requests.CreationFactory;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;

/**
 * wchen class global comment. Detailled comment
 */
public class NewNodeCreationFactory implements CreationFactory {

    private DropType dropType;

    private NodeType nodeType;

    public NewNodeCreationFactory(DropType dropType, NodeType nodeType) {
        this.dropType = dropType;
        this.nodeType = nodeType;
        if (nodeType == null) {
            nodeType = NodeType.ELEMENT;
        }
    }

    public AbstractNode getNewObject() {
        if (dropType != null) {
            switch (dropType) {
            case DROP_OUTPUT_DOC_CHILD:
                OutputTreeNode outputNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                outputNode.setNodeType(nodeType);
                return outputNode;
            case DROP_INSERT_INPUT:
                return XmlmapFactory.eINSTANCE.createTreeNode();
            case DROP_INSERT_OUTPUT:
                return XmlmapFactory.eINSTANCE.createOutputTreeNode();
            case DROP_INSERT_VAR:
                return XmlmapFactory.eINSTANCE.createVarNode();
            default:
                break;
            }
        }
        return null;
    }

    public DropType getObjectType() {
        return dropType;
    }

}
