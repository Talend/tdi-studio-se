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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.core.PluginChecker;
import org.talend.core.runtime.preference.IProjectSettingPageTester;
import org.talend.repository.ProjectManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class CustomComponentProjectSettingPageTester implements IProjectSettingPageTester {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.runtime.preference.IProjectSettingPageTester#valid(org.eclipse.core.runtime.IConfigurationElement
     * , org.eclipse.jface.preference.IPreferenceNode)
     */
    @Override
    public boolean valid(IConfigurationElement element, IPreferenceNode node) {
        if (PluginChecker.isRemoteProviderPluginLoaded() && !ProjectManager.getInstance().getCurrentProject().isLocal()) {
            return true;
        }
        return false;
    }

}
