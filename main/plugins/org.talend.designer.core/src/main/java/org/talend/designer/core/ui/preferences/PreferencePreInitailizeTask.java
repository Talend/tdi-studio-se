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
package org.talend.designer.core.ui.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.login.AbstractLoginTask;

/**
 *
 * DOC wchen class global comment. Detailled comment
 */
public class PreferencePreInitailizeTask extends AbstractLoginTask {

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        String pAutoCheck = System.getProperty("svn.update.info.check");
        if (pAutoCheck != null) {
            boolean enableAutoCheckSvn = Boolean.valueOf(pAutoCheck);
            store.setValue(ITalendCorePrefConstants.SVN_UPDATE_INFO_AUTO_CHECK, enableAutoCheckSvn);
        }
        String pInterval = System.getProperty("svn.update.info.interval");
        if (pInterval != null) {
            try {
                int svnCheckInterval = Integer.parseInt(pInterval);
                store.setValue(ITalendCorePrefConstants.SVN_UPDATE_INFO_AUTO_CHECK_TIME_INTERVAL, svnCheckInterval);
            } catch (NumberFormatException e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
