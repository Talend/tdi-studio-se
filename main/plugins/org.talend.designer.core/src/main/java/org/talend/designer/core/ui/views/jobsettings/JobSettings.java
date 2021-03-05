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
package org.talend.designer.core.ui.views.jobsettings;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobSettings {

    /**
     *
     * DOC ggu Comment method "switchToCurJobSettingsView".
     */
    public static void switchToCurJobSettingsView() {
        JobSettingsView jobSettingsView = getView();
        if (jobSettingsView != null) {
            jobSettingsView.refresh(true, null);
        }
    }

    public static void switchToCurJobSettingsView(Object object) {
        JobSettingsView jobSettingsView = getView();
        if (jobSettingsView != null) {
            jobSettingsView.refresh(true, object);
        }
    }

    /**
     * ggu Comment method "refreshView".
     */
    private static JobSettingsView getView() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return null;
        }
        IWorkbenchWindow benchWindow = workbench.getActiveWorkbenchWindow();
        if (benchWindow == null) {
            return null;
        }
        IWorkbenchPage page = benchWindow.getActivePage();
        if (page == null) {
            return null;
        }
        IViewPart view = page.findView(JobSettingsView.ID);
        if (view == null) {
            try {
                // view = page.showView(JobSettingsView.ID);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        if (view != null && view instanceof JobSettingsView) {
            return (JobSettingsView) view;
        }
        return null;

    }

    /**
     * ggu Comment method "clearAll".
     */
    public static void cleanDisplay() {
        JobSettingsView jobSettingsView = getView();
        if (jobSettingsView != null) {
            jobSettingsView.cleanDisplay();
        }
    }

}
