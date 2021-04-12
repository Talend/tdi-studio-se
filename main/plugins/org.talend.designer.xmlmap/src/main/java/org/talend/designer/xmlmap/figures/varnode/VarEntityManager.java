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
package org.talend.designer.xmlmap.figures.varnode;

import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class VarEntityManager extends TableEntityManager {

    public VarEntityManager(VarNode entityModel, VarNodeEditPart entityPart) {
        super(entityModel, entityPart);
    }

    @Override
    public VarNode getModel() {
        return (VarNode) super.getModel();
    }

    @Override
    public VarTable getMapperTable() {
        return (VarTable) super.getMapperTable();
    }

    @Override
    public boolean isTableMinimized() {
        return getMapperTable().isMinimized();
    }

}
