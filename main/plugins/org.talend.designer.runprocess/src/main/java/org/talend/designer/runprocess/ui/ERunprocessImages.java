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
package org.talend.designer.runprocess.ui;

import org.talend.commons.ui.runtime.image.IImage;
import org.talend.designer.runprocess.RunProcessPlugin;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public enum ERunprocessImages implements IImage {
    RUN_PROCESS_ACTION("/icons/process_run.gif"), //$NON-NLS-1$
    PAUSE_PROCESS_ACTION("/icons/process_pause.gif"), //$NON-NLS-1$
    DEBUG_PROCESS_ACTION("/icons/process_debug.gif"), //$NON-NLS-1$
    DEBUG_TRACE_ACTION("/icons/trace_on.png"), //$NON-NLS-1$
    KILL_PROCESS_ACTION("/icons/process_kill.gif"),
    PREVIOUS_ROW_ACTION("/icons/left.gif"),
    NEXT_ROW_ACTION("/icons/right.gif"),
    NEXT_BREAKPOINT_ACTION("/icons/rightx.png"); //$NON-NLS-1$

    private String path;

    ERunprocessImages(String path) {
        this.path = path;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for clazz.
     *
     * @return the clazz
     */
    public Class getLocation() {
        return RunProcessPlugin.class;
    }

}
