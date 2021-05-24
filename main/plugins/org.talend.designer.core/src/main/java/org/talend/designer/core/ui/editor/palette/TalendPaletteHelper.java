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
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.core.TalendPartAdapter2;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.repository.ui.actions.OpenPaletteFilterAction;

/**
 * ggu class global comment. Detailled comment
 */
public final class TalendPaletteHelper {

    private static TalendPartAdapter2 talendPartListener;

    public static void checkAndInitToolBar() {
        if (talendPartListener == null) {
            talendPartListener = new TalendPartAdapter2() {

                @Override
                public void partActivated(IWorkbenchPartReference partRef) {
                    checkAndInitToolBar();
                }

                @Override
                public void partOpened(IWorkbenchPartReference partRef) {
                    checkAndInitToolBar();
                }
            };
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                IPartService partService = activeWorkbenchWindow.getPartService();
                partService.addPartListener(talendPartListener);
            }
        }
        // check every time
        checkAndInitToolBar2();
    }

    private static void checkAndInitToolBar2() {
        //
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null || activeWorkbenchWindow.getActivePage() == null) {
            return;
        }
        boolean isTalendEditor = false;
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        IEditorPart activePart = activePage.getActiveEditor();
        if (activePart != null && activePart instanceof AbstractMultiPageTalendEditor) {
            isTalendEditor = true;
        }
        IViewPart paletteViewPart = activePage.findView(PaletteView.ID);
        if (paletteViewPart != null) {
            // find existing or not, and add it.
            IActionBars actionBars = paletteViewPart.getViewSite().getActionBars();

            // ShowStandardAction showStandardAction = ShowStandardAction.getInstance();
            // updatePaletteActions(actionBars, showStandardAction, isTalendEditor);
            //
            // ShowFavoriteAction showFavoriteAction = ShowFavoriteAction.getInstance();
            // updatePaletteActions(actionBars, showFavoriteAction, isTalendEditor);
            //
            // if (ShowFavoriteAction.state) {
            // showStandardAction.doSetEnable();
            // }

            OpenPaletteFilterAction openPaletteFilterAction = OpenPaletteFilterAction.getInstance();
            updatePaletteActions(actionBars, openPaletteFilterAction, isTalendEditor);

            actionBars.updateActionBars();
        }
    }

    private static void updatePaletteActions(IActionBars actionBars, IAction action, boolean isTalendEditor) {
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        IContributionItem cItem = toolBarManager.find(action.getClass().getCanonicalName());
        if (cItem == null) {
            if (isTalendEditor) {
                toolBarManager.add(action);
            }
        } else {
            if (!isTalendEditor) {
                toolBarManager.remove(new ActionContributionItem(action));
            }
        }
    }
}
