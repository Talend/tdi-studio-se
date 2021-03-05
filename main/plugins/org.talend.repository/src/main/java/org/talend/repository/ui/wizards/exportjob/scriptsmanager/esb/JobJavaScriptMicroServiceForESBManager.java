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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.util.Map;

import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;

/**
 * DOC sunchaoqun  class global comment. Detailled comment
 * <br/>
 *
 * $Id$
 *
 */
public class JobJavaScriptMicroServiceForESBManager extends JobJavaScriptsManager {

    /**
     * DOC sunchaoqun JobJavaScriptMicroServiceForESBManager constructor comment.
     *
     * @param exportChoiceMap
     * @param contextName
     * @param launcher
     * @param statisticPort
     * @param tracePort
     */
    public JobJavaScriptMicroServiceForESBManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#getRootFolderName(java.lang.String)
     */
    @Override
    public String getRootFolderName(String path) {
        return super.getRootFolderName(path);
    }
}
