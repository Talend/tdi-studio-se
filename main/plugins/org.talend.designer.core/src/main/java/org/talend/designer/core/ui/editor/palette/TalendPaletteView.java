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
package org.talend.designer.core.ui.editor.palette;

import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.talend.repository.ui.actions.ShowFavoriteAction;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * DOC hwang class global comment. Detailled comment
 *
 * @deprecated don't extension this, and move to the method postWindowOpen of class ApplicationWorkbenchWindowAdvisor,
 * when open workbech.
 */
public class TalendPaletteView extends PaletteView {

    public static final String ID = "org.eclipse.gef.ui.palette_view"; //$NON-NLS-1$

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        IToolBarManager toolMana = getViewSite().getActionBars().getToolBarManager();
        ShowStandardAction showStandardAction = ShowStandardAction.getInstance();
        ShowFavoriteAction showFavoriteAction = ShowFavoriteAction.getInstance();
        toolMana.add(showStandardAction);
        toolMana.add(showFavoriteAction);
        if (ShowFavoriteAction.state) {
            showStandardAction.doSetEnable();
        }
    }

}
