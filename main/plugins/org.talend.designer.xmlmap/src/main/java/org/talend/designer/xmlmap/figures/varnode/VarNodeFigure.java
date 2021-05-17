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

import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;
import org.talend.designer.gefabstractmap.figures.var.VarEntityFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;

/**
 * created by Administrator on 2013-1-11 Detailled comment
 *
 */
public class VarNodeFigure extends VarEntityFigure {

    private VarNode varNode;

    public VarNodeFigure(VarEntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected VarEntityManager getEntityManager() {
        return (VarEntityManager) super.getEntityManager();
    }

    @Override
    protected void createEntityItems(TableEntityElement entityElement) {
        this.varNode = getEntityManager().getModel();
        super.createEntityItems(entityElement);
    }

    @Override
    public void setNullable(boolean isCheck) {
        getEntityManager().getModel().setNullable(isCheck);
    }

    @Override
    public boolean isNullable() {
        return getEntityManager().getModel().isNullable();
    }

    @Override
    public String getVarName() {
        return getEntityManager().getModel().getName();
    }

    @Override
    public String getExpressionText() {
        return getEntityManager().getModel().getExpression();
    }

    @Override
    public String getType() {
        return getEntityManager().getModel().getType();
    }

    public VarNode getVarNode() {
        return this.varNode;
    }
}
