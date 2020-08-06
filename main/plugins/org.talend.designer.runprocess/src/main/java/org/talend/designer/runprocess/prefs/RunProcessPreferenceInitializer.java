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
package org.talend.designer.runprocess.prefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.maven.template.AbstractMavenTemplateManager;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.utils.JobVMArgumentsUtil;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class RunProcessPreferenceInitializer extends AbstractPreferenceInitializer {

    protected static final String EMPTY_STR = ""; //$NON-NLS-1$

    private static final String commonLogFilePath = "log/common-logging.properties_template"; //$NON-NLS-1$

    private static final String log4jFilePath = "log/log4j.properties_template"; //$NON-NLS-1$

    public RunProcessPreferenceInitializer() {
        super();
    }

    private String defaultVM(){
        JSONObject root = new JSONObject();
        try {
            JSONArray args = new JSONArray();
            for (String arg : JobVMArgumentsUtil.DEFAULT_JVM_ARGS) {
                args.put(arg);
            }
            root.put("JOB_RUN_VM_ARGUMENTS", args);//$NON-NLS-1$
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return root.toString();
    }

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = getPluginPreferenceStore();
        prefs.setDefault(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND1, "3334"); //$NON-NLS-1$
        prefs.setDefault(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND2, "4333"); //$NON-NLS-1$
        prefs.setDefault(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND1, "4334"); //$NON-NLS-1$
        prefs.setDefault(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND2, "5333"); //$NON-NLS-1$

        // added by wzhang for feature 7428.
        prefs.setDefault(RunProcessPrefsConstants.ISCLEARBEFORERUN, true);
        prefs.setDefault(RunProcessPrefsConstants.ISSAVEBEFORERUN, true);
        prefs.setDefault(RunProcessPrefsConstants.ISSTATISTICSRUN, true);
        prefs.setDefault(RunProcessPrefsConstants.STRACESTIME, 1000);
        prefs.setDefault(RunProcessPrefsConstants.VMARGUMENTS, defaultVM()); //$NON-NLS-1$


        // for logs
        prefs.setDefault(RunProcessPrefsConstants.COMMON_LOGGING_PROPERTIES_TEMPLATE, getLogTemplate(commonLogFilePath));
        prefs.setDefault(RunProcessPrefsConstants.LOG4J_PROPERTIES_TEMPLATE, getLogTemplate(log4jFilePath));

        //
        AbstractMavenTemplateManager templateManager = MavenTemplateManager.getTemplateManagerMap().get(
                RunProcessPlugin.PLUGIN_ID);
        if (templateManager != null) {
            ProjectPreferenceManager projectPreferenceManager = templateManager.getProjectPreferenceManager();
            if (projectPreferenceManager != null) {
                IPreferenceStore projectSettingStore = projectPreferenceManager.getPreferenceStore();
                try {
                    InputStream shStream = templateManager
                            .readBundleStream(IProjectSettingTemplateConstants.PATH_RESOURCES_TEMPLATES + '/'
                                    + IProjectSettingTemplateConstants.JOB_RUN_SH_TEMPLATE_FILE_NAME);
                    String shContent = MavenTemplateManager.getContentFromInputStream(shStream);
                    if (shContent != null) {
                        projectSettingStore.setDefault(IProjectSettingPreferenceConstants.TEMPLATE_SH, shContent);
                    }

                    InputStream batStream = templateManager
                            .readBundleStream(IProjectSettingTemplateConstants.PATH_RESOURCES_TEMPLATES + '/'
                                    + IProjectSettingTemplateConstants.JOB_RUN_BAT_TEMPLATE_FILE_NAME);
                    String batContent = MavenTemplateManager.getContentFromInputStream(batStream);
                    if (batContent != null) {
                        projectSettingStore.setDefault(IProjectSettingPreferenceConstants.TEMPLATE_BAT, batContent);
                    }

                    InputStream psStream = templateManager
                            .readBundleStream(IProjectSettingTemplateConstants.PATH_RESOURCES_TEMPLATES + '/'
                                    + IProjectSettingTemplateConstants.JOB_RUN_PS_TEMPLATE_FILE_NAME);
                    String psContent = MavenTemplateManager.getContentFromInputStream(psStream);
                    if (psContent != null) {
                        projectSettingStore.setDefault(IProjectSettingPreferenceConstants.TEMPLATE_PS, psContent);
                    }

                    InputStream jobInfoStream = templateManager
                            .readBundleStream(IProjectSettingTemplateConstants.PATH_RESOURCES_TEMPLATES + '/'
                                    + IProjectSettingTemplateConstants.JOB_INFO_TEMPLATE_FILE_NAME);
                    String jobInfoContent = MavenTemplateManager.getContentFromInputStream(jobInfoStream);
                    if (jobInfoContent != null) {
                        projectSettingStore.setDefault(IProjectSettingPreferenceConstants.TEMPLATE_JOB_INFO, jobInfoContent);
                    }

                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        // RunProcessPlugin.getDefault().getProjectPreferenceManager().getPreferenceStore()
        // .setDefault(ITalendCorePrefConstants.COMMAND_STR, ITalendCorePrefConstants.DEFAULT_COMMAND_STR);
    }

    /**
     * DOC amaumont Comment method "getPluginPreferenceStore".
     *
     * @return
     */
    public static IPreferenceStore getPluginPreferenceStore() {
        IPreferenceStore prefs = RunProcessPlugin.getDefault().getPreferenceStore();
        return prefs;
    }

    protected String getLogTemplate(String logPath) {
        IRunProcessService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        if (service == null) {
            return EMPTY_STR;
        }

        File templateFile = new File(service.getResourceFilePath(logPath));
        if (!templateFile.exists()) {
            return EMPTY_STR;
        }

        return getLogTemplateString(templateFile);
    }

    protected String getLogTemplateString(File templateScriptFile) {
        if (templateScriptFile != null && templateScriptFile.exists()) {
            try {
                return new Scanner(templateScriptFile).useDelimiter("\\A").next(); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                ExceptionHandler.process(e);
            }
        }
        return EMPTY_STR;
    }

}
