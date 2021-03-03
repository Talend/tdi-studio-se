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
package org.talend.repository.preference;

import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.core.ui.token.PrefTokenKey;
import org.talend.repository.RepositoryPlugin;

import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public class ExportJobTokenCollector extends AbstractTokenCollector {

    public static final PrefTokenKey TOS_COUNT_JOB_EXPORTS = new PrefTokenKey("job.exports", "tos_count_job_exports"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * ggu ExportJobTokenCollector constructor comment.
     */
    public ExportJobTokenCollector() {
    }

    /* (non-Javadoc)
     * @see org.talend.core.ui.token.AbstractTokenCollector#collect()
     */
    @Override
    public JSONObject collect() throws Exception {
        JSONObject object = new JSONObject();
        int num = RepositoryPlugin.getDefault().getPreferenceStore().getInt(TOS_COUNT_JOB_EXPORTS.getPrefKey());
        object.put(TOS_COUNT_JOB_EXPORTS.getKey(), num);
        return object;
    }

}
