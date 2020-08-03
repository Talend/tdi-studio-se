// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.expressionbuilder;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.services.IExpressionBuilderDialogService;
import org.talend.expressionbuilder.ui.BatchExpressionBuilderDialog;
import org.talend.expressionbuilder.ui.ExpressionBuilderDialog;
import org.talend.expressionbuilder.ui.ExpressionBuilderDialogForElt;
import org.talend.expressionbuilder.ui.PigExpressionBuilderDialog;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionBuilderService.java 下午04:40:37 2007-8-1 +0000 (2007-8-1) yzhang $
 *
 */
public class ExpressionBuilderService implements IExpressionBuilderDialogService {

    /*
     * (non-Jsdoc)
     *
     * @see org.talend.core.ui.expressionbuilder.IExpressionBuilderDialogService#getExpressionBuilderInstance(Composite,
     * IExpressionDataBean, INode)
     */
    @Override
    public IExpressionBuilderDialogController getExpressionBuilderInstance(Composite parent, IExpressionDataBean dataBean,
            INode component) {
        return new ExpressionBuilderDialog(parent.getShell(), dataBean, component);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.expressionbuilder.IExpressionBuilderDialogService#getExpressionBuilderInstance(org.eclipse.swt.widgets
     * .Composite, org.talend.commons.ui.expressionbuilder.IExpressionDataBean, org.talend.core.model.process.INode,
     * java.util.List, java.lang.Boolean)
     */
    @Override
    public IExpressionBuilderDialogController getExpressionBuilderInstance(Composite parent, IExpressionDataBean dataBean,
            INode component, List<Variable> vars, boolean hasPigDataFuCategory) {
        return new PigExpressionBuilderDialog(parent.getShell(), dataBean, component, vars, hasPigDataFuCategory);
    }

    @Override
    public IExpressionBuilderDialogController getExpressionBuilderInstance(Composite parent, IExpressionDataBean dataBean,
            INode component, boolean isBatch) {
        return new BatchExpressionBuilderDialog(parent.getShell(), dataBean, component);
    }

    @Override
    public IExpressionBuilderDialogController getExpressionBuilderInstance(Composite parent, IExpressionDataBean dataBean,
            INode component, int ExpressionBuilderType) {
        switch (ExpressionBuilderType) {
        case 1:
            return new ExpressionBuilderDialogForElt(parent.getShell(), dataBean, component);
        }

        return null;
    }
}
