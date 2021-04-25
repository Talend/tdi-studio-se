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
package org.talend.designer.business.model.business.diagram.providers;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.providers.DiagramContextMenuProvider;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.core.ui.action.ShowJobSettingsViewAction;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BusinessDiagramActionProvider extends DiagramContextMenuProvider {

    /**
     * DOC Administrator BusinessDiagramActionProvider constructor comment.
     *
     * @param part
     * @param viewer
     */
    public BusinessDiagramActionProvider(IWorkbenchPart part, EditPartViewer viewer) {
        super(part, viewer);
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {
        ShowJobSettingsViewAction jobSettings = new ShowJobSettingsViewAction();
        menu.add(jobSettings);
        super.buildContextMenu(menu);
    }

    @Override
    public IContributionItem[] getItems() {
        IContributionItem[] ic = super.getItems();
        filterSystemPopMenu(ic);
        return ic;
    }

    private IContributionItem[] filterSystemPopMenu(IContributionItem[] items) {
        for (IContributionItem item : items) {
            if (ActionIds.ACTION_SHOW_PROPERTIES_VIEW.equals(item.getId())) {
                item.setVisible(false);
            } else if ("org.bonitasoft.studio.diagram.custom.actions.copy".equals(item.getId())) {
                item.setVisible(false);
            } else if ("org.bonitasoft.studio.diagram.custom.actions.paste".equals(item.getId())) {
                item.setVisible(false);
            }
        }

        return items;
    }
}
