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
package org.talend.designer.core;

import org.eclipse.ui.IStartup;
import org.talend.designer.core.ui.UpdateHelpIndexJob;
import org.talend.designer.core.ui.preferences.PaletteSettingsPreferencePage;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class DesignerCorePreparation implements IStartup {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    @Override
    public void earlyStartup() {
        if (PaletteSettingsPreferencePage.isPaletteSearchFromHelp()) {
            UpdateHelpIndexJob updateHelpIndexJob = new UpdateHelpIndexJob();
            updateHelpIndexJob.schedule();
        }
    }

}
