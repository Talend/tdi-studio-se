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
package org.talend.designer.xmlmap.figures.treeNode;

import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.parts.GlobalMapNodeEditPart;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class GlobalMapNodeEntityManager extends TableEntityManager {

    public GlobalMapNodeEntityManager(GlobalMapNode globalMapNode, GlobalMapNodeEditPart nodePart) {
        super(globalMapNode, nodePart);
    }

    @Override
    public GlobalMapNode getModel() {
        return (GlobalMapNode) super.getModel();
    }

    @Override
    public AbstractInOutTree getMapperTable() {
        return (AbstractInOutTree) super.getMapperTable();
    }
}
