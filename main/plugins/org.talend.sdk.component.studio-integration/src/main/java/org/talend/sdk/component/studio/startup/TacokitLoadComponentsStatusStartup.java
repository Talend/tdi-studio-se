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
package org.talend.sdk.component.studio.startup;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.sdk.component.studio.Lookups;

/**
 * created by hcyi on Sep 11, 2019
 * Detailled comment
 *
 */
public class TacokitLoadComponentsStatusStartup implements IStartup {

    @Override
    public void earlyStartup() {
        if (CommonsPlugin.isHeadless()) {
            return;
        }
        Exception ex = Lookups.manager().getLoadException();
        if (ex == null) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                MessageBoxExceptionHandler.process(ex, DisplayUtils.getDefaultShell(false));
            }
        });
    }
}
