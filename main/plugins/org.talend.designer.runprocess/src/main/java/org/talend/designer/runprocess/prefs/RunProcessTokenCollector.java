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
import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.core.ui.token.PrefTokenKey;
import org.talend.designer.runprocess.RunProcessPlugin;

import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public class RunProcessTokenCollector extends AbstractTokenCollector {

    public static final PrefTokenKey TOS_COUNT_RUNS = new PrefTokenKey("tos.count.runs", "tos_count_runs"); //$NON-NLS-1$  //$NON-NLS-2$

    public static final PrefTokenKey TOS_COUNT_DEBUG_RUNS = new PrefTokenKey("tos.count.debugRuns", "tos_count_debug_runs"); //$NON-NLS-1$  //$NON-NLS-2$

    /**
     * ggu RunProcessTokenCollector constructor comment.
     */
    public RunProcessTokenCollector() {
    }

    /* (non-Javadoc)
     * @see org.talend.core.ui.token.AbstractTokenCollector#collect()
     */
    @Override
    public JSONObject collect() throws Exception {
        JSONObject object = new JSONObject();

        IPreferenceStore preferenceStore = RunProcessPlugin.getDefault().getPreferenceStore();
        int numRun = preferenceStore.getInt(TOS_COUNT_RUNS.getPrefKey());
        object.put(TOS_COUNT_RUNS.getKey(), numRun);

        int numDebugRun = preferenceStore.getInt(TOS_COUNT_DEBUG_RUNS.getPrefKey());
        object.put(TOS_COUNT_DEBUG_RUNS.getKey(), numDebugRun);

        return object;
    }

}
