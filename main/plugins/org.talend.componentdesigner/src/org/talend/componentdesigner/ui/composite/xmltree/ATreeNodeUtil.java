// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.composite.xmltree;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.talend.core.model.ModelPlugin;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.datatools.xml.utils.SchemaPopulationUtil;

/**
 * DOC rli class global comment. Detailled comment
 */
public class ATreeNodeUtil {

    private static ATreeNode rootTreeNode = null;

    static {
        try {
            URL url = ModelPlugin.getDefault().getBundle().getResource("/model/Component.xsd"); //$NON-NLS-1$
            url = FileLocator.toFileURL(url);
            String fileAbsolutePath = url.getFile();
            rootTreeNode = SchemaPopulationUtil.getSchemaTree(fileAbsolutePath, true, 10);
        } catch (Exception e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }
    }

    public static ATreeNode getTreeNodeByPath(String xPath) {
        String[] nodeNameSeq = xPath.split("/"); //$NON-NLS-1$

        ATreeNode resultNode = rootTreeNode;

        // root node is COMPONENT in our model.
        for (int i = 1; i < nodeNameSeq.length; i++) {
            String nodeName = nodeNameSeq[i];
            resultNode = findTreeNode(nodeName, resultNode);
            if (resultNode == null) {
                break;
            }
        }
        return resultNode;
    }

    private static ATreeNode findTreeNode(String nodeName, ATreeNode rootTreeNode) {
        Object[] childNodes = rootTreeNode.getChildren();

        ATreeNode aTreeNode = null;
        for (Object node : childNodes) {
            if (nodeName.equals(((ATreeNode) node).getValue())) {
                return (ATreeNode) node;
            } else {
                continue;
            }
        }
        return aTreeNode;

    }

    public static String[] getChildNodeNames(ATreeNode aTreeNode) {
        return null;
    }

}
