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
package org.talend.designer.core.ui.views;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * DOC wzhang class global comment. Detailled comment
 */
public final class RefreshView {

    public static void refreshAll() {
        IWorkbenchWindow workBenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workBenchWindow == null) {
            return;
        }
        IWorkbenchPage workBenchPage = workBenchWindow.getActivePage();
        if (workBenchPage == null) {
            return;
        }

        for (IViewDescriptor desc : PlatformUI.getWorkbench().getViewRegistry().getViews()) {
            IViewPart viewPart = workBenchPage.findView(desc.getId());
            // show the view again in order to see the change
            if (viewPart != null) {
                workBenchPage.hideView(viewPart);
            }
        }

        workBenchPage.resetPerspective();
    }
}
