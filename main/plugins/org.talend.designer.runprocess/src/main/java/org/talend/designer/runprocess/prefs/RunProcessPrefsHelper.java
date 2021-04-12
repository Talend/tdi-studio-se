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
package org.talend.designer.runprocess.prefs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.runprocess.RunProcessPlugin;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public final class RunProcessPrefsHelper {

    private static RunProcessPrefsHelper instance;

    public static RunProcessPrefsHelper getInstance() {
        if (instance == null) {
            instance = new RunProcessPrefsHelper();
        }
        return instance;
    }

    /**
     * DOC amaumont RunRemoteProcessPrefsHelper constructor comment.
     */
    private RunProcessPrefsHelper() {
        super();
    }

    public IPreferenceStore getPreferenceStore() {
        return RunProcessPlugin.getDefault().getPreferenceStore();
    }

    public int getClientStatsPortBound1() {
        return getPreferenceStore().getInt(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND1);
    }

    public int getClientStatsPortBound2() {
        return getPreferenceStore().getInt(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND2);
    }

    public int getClientTracePortBound1() {
        return getPreferenceStore().getInt(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND1);
    }

    public int getClientTracePortBound2() {
        return getPreferenceStore().getInt(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND2);
    }

}
