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
package org.talend.designer.business.diagram.custom.util;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ShellUtil {

    /**
     * Returns the currently active workbench window or <code>null</code> if none.
     *
     * @return the currently active workbench window or <code>null</code>
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * Returns the currently active workbench window shell or <code>null</code> if none.
     *
     * @return the currently active workbench window shell or <code>null</code>
     */
    public static Shell getShell() {
        if (getActiveWorkbenchWindow() != null) {
            return getActiveWorkbenchWindow().getShell();
        }
        return null;
    }

}
