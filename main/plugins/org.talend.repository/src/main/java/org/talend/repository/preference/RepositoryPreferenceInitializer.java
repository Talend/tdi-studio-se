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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.GlobalServiceRegister;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.constants.Log4jPrefsConstants;

/**
 * ggu class global comment. Detailled comment
 */
public class RepositoryPreferenceInitializer extends AbstractPreferenceInitializer {

    protected static final String EMPTY_STR = "";

    private static final String commonLogFilePath = "log/common-logging.properties_template"; //$NON-NLS-1$

    private static final String log4jFilePath = "log/log4j.properties_template"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        // preferenceStore.setDefault(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT, true);

        // for the log4j in the projectSettings
        preferenceStore.setDefault(Log4jPrefsConstants.COMMON_LOGGING_PROPERTIES_TEMPLATE, getTemplateTxt(commonLogFilePath));
        preferenceStore.setDefault(Log4jPrefsConstants.LOG4J_PROPERTIES_TEMPLATE, getTemplateTxt(log4jFilePath));
    }

    private String getTemplateTxt(String path) {
        IRunProcessService service = null;
        String templateString = "";
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        if (service != null) {
            templateString = service.getLogTemplate(path);
        }
        return templateString;
    }
}
