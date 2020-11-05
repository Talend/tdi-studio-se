// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.util.EnumMap;
import java.util.Map;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptMicroServiceForESBManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.OSGIJavaScriptForESBWithMavenManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class JobScriptsManagerFactory {

    public static JobScriptsManager createManagerInstance(Map<ExportChoice, Object> exportChoiceMap, String contextName,
            String launcher, int statisticPort, int tracePort, JobExportType jobExportType) {
        switch (jobExportType) {
        case POJO:
            Object jobType = exportChoiceMap.get(ExportChoice.jobType);
            if (jobType != null && jobType instanceof ERepositoryObjectType) {
                ERepositoryObjectType type = (ERepositoryObjectType) jobType;
                if (type.equals(ERepositoryObjectType.PROCESS_MR)) {
                    return new MapReduceJobJavaScriptsManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
                }
            }
            return new JobJavaScriptsManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
        case OSGI:
            if (exportChoiceMap.containsKey(ExportChoice.needMavenScript)
                    && exportChoiceMap.get(ExportChoice.needMavenScript) == Boolean.TRUE) {
                return new OSGIJavaScriptForESBWithMavenManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
            } else {
                // TESB-16721 [Export Job] On export job context always "Default"
                return new JobJavaScriptOSGIForESBManager(exportChoiceMap, null, launcher, statisticPort, tracePort);
            }
        case MSESB:
            ProcessorUtilities.setExportJobAsMicroSerivce(true);
            return new JobJavaScriptMicroServiceForESBManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
        case MSESB_IMAGE:
            ProcessorUtilities.setExportJobAsMicroSerivce(true);
            return new JobJavaScriptMicroServiceForESBManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
        default:
            //
        }
        return new JobJavaScriptsManager(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    public static Map<ExportChoice, Object> getDefaultExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        exportChoiceMap.put(ExportChoice.needDependencies, true);
        return exportChoiceMap;
    }

}
