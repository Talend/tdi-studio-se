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
package org.talend.designer.core.ui.projectsetting;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.core.PluginChecker;
import org.talend.core.runtime.preference.IProjectSettingPageTester;

/**
 * 
 * created by hcyi on Jul 28, 2020 Detailled comment
 *
 */
public class RuntimeLineagePageTester implements IProjectSettingPageTester {

    @Override
    public boolean valid(IConfigurationElement element, IPreferenceNode node) {
        return PluginChecker.isTIS() && Boolean.valueOf(System.getProperty("runtime.lineage", Boolean.FALSE.toString())); //$NON-NLS-1$
    }
}
