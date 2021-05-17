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

import org.talend.designer.gefabstractmap.part.FilterConnectionPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.IConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapFilterConnectionPart extends FilterConnectionPart {

    protected int calculateConnOffset() {
        FilterConnection model = (FilterConnection) getModel();

        if (model.getSource() == null) {
            return 0;
        }
        TreeNode sourceTreeNode = (TreeNode) model.getSource();
        AbstractInOutTree inputTreeNodeRoot = XmlMapUtil.getAbstractInOutTree(sourceTreeNode);
        List<IConnection> outConns = new ArrayList<IConnection>();
        if (inputTreeNodeRoot instanceof InputXmlTree) {
            outConns.addAll(XmlMapUtil.getInputTreeFilterConnections((InputXmlTree) inputTreeNodeRoot));
        }
        int indexOf = outConns.indexOf(model);
        if (indexOf != -1) {
            return -(indexOf + 1) * XmlMapUtil.DEFAULT_OFFSET_FILTER;
        }
        return 0;
    }

}
