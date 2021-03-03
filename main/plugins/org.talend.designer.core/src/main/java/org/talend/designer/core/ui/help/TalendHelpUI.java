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
package org.talend.designer.core.ui.help;

import org.eclipse.help.IContext;
import org.eclipse.help.ui.internal.DefaultHelpUI;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.talend.designer.core.utils.ComponentsHelpUtil;

public class TalendHelpUI extends DefaultHelpUI {

    @Override
    public void displayContext(IContext context, int x, int y) {
        if (ComponentsHelpUtil.isUseOnLineHelp() && isProcessNodeHelpContext()) {
            return;
        }
        super.displayContext(context, x, y);
    }

    private boolean isProcessNodeHelpContext() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell activeShell = Display.getCurrent().getActiveShell();
        if (window != null && isActiveShell(activeShell, window)) {
            Control control = window.getShell().getDisplay().getFocusControl();
            if (control != null) {
                Object value = control.getData(WorkbenchHelpSystem.HELP_KEY);
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.startsWith("org.talend.help.")) { //$NON-NLS-1$
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isActiveShell(Shell activeShell, IWorkbenchWindow window) {
        // Test if the active shell belongs to this window
        return activeShell != null && activeShell.equals(window.getShell());
    }
}
