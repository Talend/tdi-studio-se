// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.resources.util.EMavenBuildScriptProperties;

/**
 * created by nrousseau on Sep 25, 2012 Detailled comment
 * 
 */
public class OSGIJavaScriptForESBWithMavenManager extends JavaScriptForESBWithMavenManager {

    /**
     * DOC nrousseau JobJavaScriptOSGIForESBWithMavenManager constructor comment.
     * 
     * @param exportChoiceMap
     * @param contextName
     * @param launcher
     * @param statisticPort
     * @param tracePort
     */
    public OSGIJavaScriptForESBWithMavenManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    @Override
    protected void addMavenBuildScripts(ExportFileResource[] processes, List<URL> scriptsUrls,
            Map<String, String> mavenPropertiesMap) {
        if (!PluginChecker.isPluginLoaded(PluginChecker.EXPORT_JOB_PLUGIN_ID)) {
            return;
        }
        try {
            //
            Item item = processes[0].getItem();
            File templateFile = null;
            if (item != null) {
                IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(item.getProperty());
                IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(item.getProperty());
                if (itemLocationPath != null && objectTypeFolder != null) {
                    IPath itemRelativePath = itemLocationPath.removeLastSegments(1)
                            .makeRelativeTo(objectTypeFolder.getLocation());
                    templateFile = PomUtil.getTemplateFile(objectTypeFolder, itemRelativePath,
                            IProjectSettingTemplateConstants.OSGI_POM_FILE_NAME);
                }

            }
            String mavenScript = MavenTemplateManager.getTemplateContent(templateFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_OSGI_BUNDLE_POM, PluginChecker.MAVEN_JOB_PLUGIN_ID,
                    IProjectSettingTemplateConstants.PATH_OSGI_BUNDLE + '/'
                            + IProjectSettingTemplateConstants.POM_JOB_TEMPLATE_FILE_NAME);

            // String mavenScript = mavenUiService
            // .getProjectSettingValue(IProjectSettingPreferenceConstants.TEMPLATE_OSGI_BUNDLE_POM);
            if (mavenScript == null) {
                return;
            }
            File mavenBuildFile = new File(getTmpFolder() + PATH_SEPARATOR + IProjectSettingTemplateConstants.POM_FILE_NAME);

            FileOutputStream mavenBuildFileOutputStream = null;
            try {
                mavenBuildFileOutputStream = new FileOutputStream(mavenBuildFile);
                mavenBuildFileOutputStream.write(mavenScript.getBytes());
            } finally {
                if (mavenBuildFileOutputStream != null) {
                    mavenBuildFileOutputStream.close();
                }
            }
            updateMavenBuildFileContent(mavenBuildFile, mavenPropertiesMap, true, false);
            scriptsUrls.add(mavenBuildFile.toURL());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JavaScriptForESBWithMavenManager#
     * getMenifestMavenProperties(java.net.URL, java.util.Map)
     */
    @Override
    protected void getMenifestMavenProperties(URL manifestURL, Map<String, String> mavenPropertiesMap) {
        super.getMenifestMavenProperties(manifestURL, mavenPropertiesMap);
        Set<String> theProvidedModules = getExcludedModuleNames();

        // reset the Bundle-Classpath to remove the routines jar
        String bundleClasspathName = EMavenBuildScriptProperties.BundleConfigBundleClasspath.getVarScript();
        String bundleClasspath = mavenPropertiesMap.get(bundleClasspathName);
        if (bundleClasspath == null) {
            return; //
        }
        String[] classpathes = bundleClasspath.split(Character.toString(MANIFEST_ITEM_SEPARATOR));
        StringBuffer sb = new StringBuffer(200);
        for (int i = 0; i < classpathes.length; i++) {
            String path = classpathes[i];
            Path libPath = new Path(path);
            String libName = libPath.lastSegment(); // lib name with jar extension
            // because export the routine/bean resource codes to build by maven, so no need routine/ben jar.
            if (isRoutines(path) || isBeans(path) || theProvidedModules.contains(libName)) {
                continue;
            }
            sb.append(path);
            if (i < classpathes.length - 1) { // the last one don't add it
                sb.append(MANIFEST_ITEM_SEPARATOR);
            }
        }
        mavenPropertiesMap.put(bundleClasspathName, sb.toString());
    }

    protected boolean isRoutines(String value) {
        if (value != null && (value.endsWith(SYSTEMROUTINE_JAR) || value.endsWith(USERROUTINE_JAR))) {
            return true;
        }
        return false;
    }

    protected boolean isBeans(String value) {
        if (value != null && value.endsWith(USERBEANS_JAR)) {
            return true;
        }
        return false;
    }
}
