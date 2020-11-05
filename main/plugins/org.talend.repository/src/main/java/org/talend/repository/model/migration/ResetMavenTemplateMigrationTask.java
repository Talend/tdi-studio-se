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
package org.talend.repository.model.migration;

import static org.talend.core.PluginChecker.EXPORT_JOB_PLUGIN_ID;
import static org.talend.core.PluginChecker.EXPORT_ROUTE_PLUGIN_ID;
import static org.talend.core.PluginChecker.MAVEN_JOB_PLUGIN_ID;
import static org.talend.core.PluginChecker.isPluginLoaded;
import static org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants.*;
import static org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.maven.template.MavenTemplateManager;

public class ResetMavenTemplateMigrationTask extends AbstractProjectMigrationTask {

    private Map<String, String> defaultContents = new HashMap<>();

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 8, 2, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Project project) {
        try {
            // project pom
            resetMavenTemplate(TEMPLATE_PROJECT_POM, "org.talend.designer.maven.ui"); //$NON-NLS-1$
            // job pom and assembly
            resetMavenTemplate(TEMPLATE_STANDALONE_JOB_POM, MAVEN_JOB_PLUGIN_ID);
            resetMavenTemplate(TEMPLATE_STANDALONE_JOB_ASSEMBLY, MAVEN_JOB_PLUGIN_ID);

            // osgi
            if (isPluginLoaded(EXPORT_JOB_PLUGIN_ID)) {
                resetMavenTemplate(TEMPLATE_OSGI_BUNDLE_POM, EXPORT_JOB_PLUGIN_ID);
            }

            if (isPluginLoaded(EXPORT_ROUTE_PLUGIN_ID)) {
                // route
                resetMavenTemplate(TEMPLATE_ROUTES_KARAF_POM, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_ROUTES_KARAF_BUNDLE, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_ROUTES_KARAF_FEATURE, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_ROUTES_KARAF_PARENT, EXPORT_ROUTE_PLUGIN_ID);
                // service
                resetMavenTemplate(TEMPLATE_SERVICES_KARAF_POM, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_SERVICES_KARAF_BUNDLE, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_SERVICES_KARAF_FEATURE, EXPORT_ROUTE_PLUGIN_ID);
                resetMavenTemplate(TEMPLATE_SERVICES_KARAF_PARENT, EXPORT_ROUTE_PLUGIN_ID);
            }
            IProject fsProject = ResourceUtils.getProject(project);

            resetJobCustomTemplateFile(fsProject, ERepositoryObjectType.PROCESS);
            if (PluginChecker.isMapReducePluginLoader()) {
                resetJobCustomTemplateFile(fsProject, ERepositoryObjectType.PROCESS_MR);
            }
            if (isPluginLoaded(EXPORT_ROUTE_PLUGIN_ID)) {
                resetServiceCustomTemplateFile(fsProject);
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    private void resetMavenTemplate(String key, String pluginId) {
        ProjectPreferenceManager manager = new ProjectPreferenceManager(pluginId);
        IPreferenceStore preferenceStore = manager.getPreferenceStore();
        if (!preferenceStore.isDefault(key)) {
            preferenceStore.setToDefault(key);
            manager.save();
        }
        defaultContents.put(key, preferenceStore.getDefaultString(key));
    }

    private void resetJobCustomTemplateFile(IProject project, ERepositoryObjectType type) throws Exception {
        IFolder folder = ResourceUtils.getFolder(project, type.getFolder(), false);
        File processFolder = folder.getLocation().toFile();
        List<File> allTemplates = new ArrayList<>();
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                return file.isDirectory() || fileName.equals(POM_FILE_NAME) || fileName.equals(ASSEMBLY_FILE_NAME)
                        || (type == ERepositoryObjectType.PROCESS && fileName.equals(OSGI_POM_FILE_NAME));
            }

        };
        getAllCustomTemplates(processFolder.listFiles(filter), allTemplates, filter);
        for (File file : allTemplates) {
            resetJobFileContentToDefault(file);
        }
    }

    private void resetServiceCustomTemplateFile(IProject project) throws Exception {
        IFolder folder = ResourceUtils.getFolder(project, "services", false); //$NON-NLS-1$
        File serviceFolder = folder.getLocation().toFile();
        List<File> allTemplates = new ArrayList<>();
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (file.isDirectory() || fileName.equals(POM_FILE_NAME) || fileName.equals(MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME)
                        || fileName.equals(MAVEN_KARAF_BUILD_FEATURE_FILE_NAME)
                        || fileName.equals(MAVEN_KARAF_BUILD_PARENT_FILE_NAME)) {
                    return true;
                }
                return false;
            }

        };
        getAllCustomTemplates(serviceFolder.listFiles(filter), allTemplates, filter);
        for (File file : allTemplates) {
            resetServiceFileContentToDefault(file);
        }

    }

    private void getAllCustomTemplates(File[] files, List<File> allTemplates, FileFilter filter) {
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getAllCustomTemplates(file.listFiles(filter), allTemplates, filter);
                } else {
                    allTemplates.add(file);
                }
            }
        }
    }

    private void resetJobFileContentToDefault(File file) throws Exception {
        String defaultContent = null;
        switch (file.getName()) {
        case POM_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_STANDALONE_JOB_POM);
            break;
        case ASSEMBLY_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_STANDALONE_JOB_ASSEMBLY);
            break;
        case OSGI_POM_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_OSGI_BUNDLE_POM);
        }
        if (defaultContent != null) {
            MavenTemplateManager.saveContent(file, defaultContent, true);
        }
    }

    private void resetServiceFileContentToDefault(File file) throws Exception {
        String defaultContent = null;
        switch (file.getName()) {
        case POM_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_SERVICES_KARAF_POM);
            break;
        case MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_SERVICES_KARAF_BUNDLE);
            break;
        case MAVEN_KARAF_BUILD_FEATURE_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_SERVICES_KARAF_FEATURE);
            break;
        case MAVEN_KARAF_BUILD_PARENT_FILE_NAME:
            defaultContent = defaultContents.get(TEMPLATE_ROUTES_KARAF_PARENT);
            break;
        }
        if (defaultContent != null) {
            MavenTemplateManager.saveContent(file, defaultContent, true);
        }
    }

}
