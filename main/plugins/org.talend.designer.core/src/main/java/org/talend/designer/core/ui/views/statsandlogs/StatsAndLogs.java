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
package org.talend.designer.core.ui.views.statsandlogs;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;

/**
 * ftang class global comment. Detailed comment <br/>
 *
 */
public class StatsAndLogs {

    private static String currentTitle = ""; //$NON-NLS-1$

    private static String newTitle = ""; //$NON-NLS-1$

    public static void switchToCurStatsAndLogsView() {
        StatsAndLogsView statsAndLogsView = getView();
        if (statsAndLogsView == null) {
            return;
        }
        if (!newTitle.equals(currentTitle)) {
            statsAndLogsView.setPartName(newTitle);
            currentTitle = newTitle;
        }
        refreshView(statsAndLogsView);
    }

    /**
     * ftang Comment method "refreshView".
     */
    private static StatsAndLogsView getView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view = page.findView(StatsAndLogsView.ID); //$NON-NLS-1$
        if (view == null) {
            try {
                view = page.showView(StatsAndLogsView.ID); //$NON-NLS-1$
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
        }
        else if (view instanceof StatsAndLogsView) {
            return (StatsAndLogsView) view;
        }
        return null;

    }

    /**
     * ftang Comment method "refreshView".
     *
     * @param view
     */
    private static void refreshView(StatsAndLogsView view) {
        if (view != null) {
            if (!newTitle.equals(currentTitle)) {
                view.setPartName(newTitle);
                currentTitle = newTitle;
            }
            view.createStatsAndLogsView();
        }
    }

    /**
     * ftang Comment method "refreshView".
     *
     * @param view
     */
    private static void emptyView(StatsAndLogsView view) {
        if (view != null) {
            if (!newTitle.equals(currentTitle)) {
                view.setPartName(newTitle);
                currentTitle = newTitle;
            }
            view.emptyView();
        }
    }

    public static void setTitle(String title) {
        newTitle = title;
    }

    /**
     * ftang Comment method "clearAll".
     */
    public static void clearAll() {
        StatsAndLogsView statsAndLogsView = getView();
        emptyView(statsAndLogsView);
    }

}
