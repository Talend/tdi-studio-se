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
package org.talend.sqlbuilder.erdiagram.ui.editor;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.sqlbuilder.erdiagram.ui.actions.CreateRelationAction;
import org.talend.sqlbuilder.erdiagram.ui.actions.ErDiagramItemDeleteAction;
import org.talend.sqlbuilder.erdiagram.ui.actions.TableAddAction;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramMenuProvider extends ContextMenuProvider {

    private ActionRegistry actionRegistry;

    private IWorkbenchPart part;

    public static final String ID = "org.talend.ErDiagramMenuProvider"; //$NON-NLS-1$

    private static final String RELATION = "Relation"; //$NON-NLS-1$

    public ErDiagramMenuProvider(IWorkbenchPart part, EditPartViewer viewer, ActionRegistry registry) {
        super(viewer);
        this.part = part;
        this.actionRegistry = registry;
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {
        menu.add(new Separator(RELATION));
        menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
        IAction action;
        action = getAction(ActionFactory.DELETE.getId());
        ((ErDiagramItemDeleteAction) action).update();
        if (action.isEnabled()) {
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        }
        CreateRelationAction createRelation;
        createRelation = new CreateRelationAction(part);
        createRelation.update();
        if (createRelation.isEnabled()) {
            menu.add(createRelation);
        }
        TableAddAction addAction = new TableAddAction(part);
        addAction.update();
        menu.add(addAction);

    }

    protected IAction getAction(final String actionId) {
        return actionRegistry.getAction(actionId);
    }

}
