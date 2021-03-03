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
package org.talend.designer.xmlmap.figures.layout;

import java.util.List;

import org.talend.designer.gefabstractmap.figures.layout.ZoneContentLayout;
import org.talend.designer.gefabstractmap.figures.table.AbstractTableContainer;
import org.talend.designer.xmlmap.figures.InputXmlTreeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.IConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * created by Administrator on 2013-1-15 Detailled comment
 *
 */
public class XmlMapTreeContainerLayout extends ZoneContentLayout {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.layout.TreeContainerLayout#getLookupConnectionSize()
     */
    @Override
    protected int getLookupConnectionSize(AbstractTableContainer tableContainer) {
        if (tableContainer instanceof InputXmlTreeFigure) {
            InputXmlTree inputModel = ((InputXmlTreeFigure) tableContainer).getInputXmlTree();

            List<TreeNode> nodeList = inputModel.getNodes();
            int maxSize = 0;
            List<IConnection> lookConnections = XmlMapUtil.getAllNodeLookConnections(inputModel);
            List<IConnection> filterConnections = XmlMapUtil.getInputTreeFilterConnections(inputModel);
            if (lookConnections.size() < filterConnections.size()) {
                maxSize = filterConnections.size();
            } else {
                maxSize = lookConnections.size();
            }

            return maxSize;
        }
        return 0;
    }

}
