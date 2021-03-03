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
package org.talend.designer.xmlmap.parts;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.gefabstractmap.part.LookupConnectionPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.IConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapLookupConnectionPart extends LookupConnectionPart {

    protected int calculateConnOffset() {
        LookupConnection model = (LookupConnection) getModel();

        if (model.getSource() == null) {
            return 0;
        }
        TreeNode sourceTreeNode = (TreeNode) model.getSource();
        List<IConnection> outConns = new ArrayList<IConnection>();
        TreeNode inputTreeNodeRoot = XmlMapUtil.getTreeNodeRoot(sourceTreeNode);
        if (inputTreeNodeRoot != null) {
            InputXmlTree inputTree = (InputXmlTree) inputTreeNodeRoot.eContainer();
            outConns.addAll(XmlMapUtil.getAllNodeLookConnections(inputTree));
        }
        int indexOf = outConns.indexOf(model);
        if (indexOf != -1) {
            return -(indexOf + 1) * XmlMapUtil.DEFAULT_OFFSET;
        }
        return 0;
    }

}
