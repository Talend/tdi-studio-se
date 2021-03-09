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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.talend.core.model.properties.Status;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.RepositoryPreferenceStore;

/**
 */
public class StatusPreferenceInitializer extends AbstractPreferenceInitializer {

    public StatusPreferenceInitializer() {
        super();
    }

    @Override
    public void initializeDefaultPreferences() {
        RepositoryPreferenceStore preferenceStore = new RepositoryPreferenceStore(ProxyRepositoryFactory.getInstance());
        try {
            preferenceStore.load();
            String statusString = preferenceStore.getString(Status.TECHNICAL_STATUS)
                    + preferenceStore.getString(Status.DOCUMENTATION_STATUS);
            if (statusString.equals("")) { //$NON-NLS-1$
                preferenceStore.setToDefault(Status.TECHNICAL_STATUS);
                preferenceStore.setToDefault(Status.DOCUMENTATION_STATUS);
                preferenceStore.save();
            }
        } catch (Exception e) {
            IStatus status = new org.eclipse.core.runtime.Status(IStatus.WARNING, RepositoryPlugin.PLUGIN_ID,
                    IStatus.OK, e.getMessage(), e);
            RepositoryPlugin.getDefault().getLog().log(status);
        }

    }

}
