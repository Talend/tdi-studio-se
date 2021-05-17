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
package org.talend.designer.tutorials;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.service.ITutorialsService;
import org.talend.designer.tutorials.dialog.TutorialsDialog;

/**
 * created by hcyi on Mar 20, 2017 Detailled comment
 *
 */
public class TutorialsService implements ITutorialsService {

    @Override
    public void openTutorialsDialog() {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (activePage != null) {
            TutorialsDialog dialog = new TutorialsDialog();
            dialog.open();
        }
    }
}
