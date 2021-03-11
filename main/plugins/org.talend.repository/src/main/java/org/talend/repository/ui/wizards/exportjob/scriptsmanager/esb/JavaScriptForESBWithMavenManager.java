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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.constants.FileConstants;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.IMavenProperties;
import org.talend.resources.util.EMavenBuildScriptProperties;

/**
 *
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public abstract class JavaScriptForESBWithMavenManager extends JobJavaScriptOSGIForESBManager {

    public JavaScriptForESBWithMavenManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager#getExportResources
     * (org.talend.repository.documentation.ExportFileResource[], java.lang.String[])
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] processes, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> list = super.getExportResources(processes, codeOptions);

        Map<String, String> mavenPropertiesMap = getMainMavenProperties(processes[0].getItem());

        Iterator<ExportFileResource> it = list.iterator();
        while (it.hasNext()) {
            ExportFileResource resource = it.next();
            if ("".equals(resource.getDirectoryName())) { //$NON-NLS-1$
                Map<String, Set<URL>> newResourcesMap = new HashMap<String, Set<URL>>();
                for (String path : resource.getRelativePathList()) {
                    if (path.startsWith("OSGI") || path.startsWith(FileConstants.META_INF_FOLDER_NAME)|| path.startsWith("TALEND-INF") || path.startsWith("MAVEN-INF")) { //$NON-NLS-1$
                        Set<URL> urls = resource.getResourcesByRelativePath(path);
                        // put OSGI_INF to /src/main/resources/
                        newResourcesMap.put(IMavenProperties.MAIN_RESOURCES_PATH + path, urls);
                    }
                }
                resource.removeAllMap();
                for (String path : newResourcesMap.keySet()) {
                    resource.addResources(path, new ArrayList<URL>(newResourcesMap.get(path)));
                }
            } else if (LIBRARY_FOLDER_NAME.equals(resource.getDirectoryName())) {
                resource.setDirectoryName(IMavenProperties.MAIN_RESOURCES_LIB_PATH);
            } else if (PROVIDED_LIB_FOLDER.equals(resource.getDirectoryName())) {
                resource.setDirectoryName(IMavenProperties.MAIN_RESOURCES_PROVIDED_LIB_PATH);
            } else if (FileConstants.META_INF_FOLDER_NAME.equals(resource.getDirectoryName())) {
                // it.remove();
                if (!resource.getAllResources().isEmpty()) {
                    Set<URL> urls = resource.getAllResources().iterator().next();
                    if (!urls.isEmpty()) {
                        URL manifestURL = urls.iterator().next();
                        getMenifestMavenProperties(manifestURL, mavenPropertiesMap);
                    }
                }
            }
        }

        // addSourceCodeToExport(list, processes, codeOptions);
        //
        // addMavenScriptToExport(list, processes, mavenPropertiesMap);

        return list;
    }

    @Override
    protected void addRoutinesResources(ExportFileResource[] processes, ExportFileResource libResource) {
        // There have been source codes for routines for maven. so no routines jar
        if (!isOptionChoosed(ExportChoice.needMavenScript)) {
            super.addRoutinesResources(processes, libResource);
        }
    }

    /**
     * DOC ggu Comment method "getMenifestProperties".
     *
     * @param manifestURL
     * @param mavenPropertiesMap
     */
    protected void getMenifestMavenProperties(URL manifestURL, Map<String, String> mavenPropertiesMap) {
        try {
            File manifestFile = new File(manifestURL.toURI());
            FileInputStream fis = new FileInputStream(manifestFile);
            Manifest manifest = new Manifest(fis);

            setBundleManifestAttribute(mavenPropertiesMap, manifest, "Private-Package", //$NON-NLS-1$
                    EMavenBuildScriptProperties.BundleConfigPrivatePackage);
            setBundleManifestAttribute(mavenPropertiesMap, manifest, "Export-Package", //$NON-NLS-1$
                    EMavenBuildScriptProperties.BundleConfigExportPackage);
            setBundleManifestAttribute(mavenPropertiesMap, manifest, "Import-Package", //$NON-NLS-1$
                    EMavenBuildScriptProperties.BundleConfigImportPackage);
            setBundleManifestAttribute(mavenPropertiesMap, manifest, "Export-Service", //$NON-NLS-1$
                    EMavenBuildScriptProperties.BundleConfigExportService);
            setBundleManifestAttribute(mavenPropertiesMap, manifest, "Bundle-Classpath", //$NON-NLS-1$
                    EMavenBuildScriptProperties.BundleConfigBundleClasspath);

            fis.close();
        } catch (URISyntaxException e) {
            ExceptionHandler.process(e);
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private void setBundleManifestAttribute(Map<String, String> mavenPropertiesMap, Manifest manifest, String attrName,
            EMavenBuildScriptProperties prop) {
        if (manifest == null || prop == null || mavenPropertiesMap == null) {
            return;
        }
        String attrValue = manifest.getMainAttributes().getValue(attrName);
        if (attrValue == null || "".equals(attrValue.trim())) { //$NON-NLS-1$
            return;
        }
        mavenPropertiesMap.put(prop.getVarScript(), attrValue);
    }

    /**
     * DOC nrousseau Comment method "addSourceCodeToExport".
     *
     * @param list
     * @param processes
     */
    private void addSourceCodeToExport(List<ExportFileResource> list, ExportFileResource[] processes, String... codeOptions) {
        List<URL> noUseJustForSimplifyChange = new ArrayList<URL>();
        exportChoice.put(ExportChoice.needSourceCode, Boolean.TRUE);
        exportChoice.put(ExportChoice.needContext, Boolean.TRUE);
        exportChoice.put(ExportChoice.contextName, contextName);
        exportChoice.put(ExportChoice.needMavenScript, Boolean.FALSE);
        processes[0].removeAllMap();
        posExportResource(processes, exportChoice, contextName, launcher, statisticPort, tracePort, 0, (IProcess) null,
                (ProcessItem) processes[0].getItem(), processes[0].getItem().getProperty().getVersion(),
                noUseJustForSimplifyChange, codeOptions);
        Map<String, Set<URL>> newResourcesMap = new HashMap<String, Set<URL>>();
        for (String path : processes[0].getRelativePathList()) {
            Set<URL> urls = processes[0].getResourcesByRelativePath(path);
            // put OSGI_INF to /src/main/resources/
            if (path.startsWith(IMavenProperties.SRC_PATH)) {
                newResourcesMap.put(path.replace(IMavenProperties.SRC_PATH, IMavenProperties.MAIN_JAVA_PATH), urls);
            } else {
                newResourcesMap.put(IMavenProperties.MAIN_RESOURCES_PATH + path, urls);
            }
        }
        ExportFileResource sourceCodeResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        for (String path : newResourcesMap.keySet()) {
            sourceCodeResource.addResources(path, new ArrayList<URL>(newResourcesMap.get(path)));
        }
        list.add(sourceCodeResource);
    }

    @Override
    protected ExportFileResource getProvidedLibExportFileResource(ExportFileResource[] processes) {
        Set<String> providedModuleNames = getExcludedModuleNames();
        if (providedModuleNames.isEmpty()) {
            return null; // if empty, won't add the privided lib folder
        }
        ExportFileResource libResource = new ExportFileResource(null, PROVIDED_LIB_FOLDER);

        List<URL> providedUrls = getNeededModuleURLs(providedModuleNames);
        if (providedUrls.isEmpty()) {
            return null; // if empty, won't add the privided lib folder
        }
        libResource.addResources(providedUrls);
        return libResource;
    }

    /**
     * DOC nrousseau Comment method "addMavenScriptToExport".
     *
     * @param list
     * @param processes
     * @param codeOptions
     */
    protected void addMavenScriptToExport(List<ExportFileResource> list, ExportFileResource[] processes,
            Map<String, String> mavenPropertiesMap) {
        List<URL> scriptsUrls = new ArrayList<URL>();

        addMavenBuildScripts(processes, scriptsUrls, mavenPropertiesMap);
        ExportFileResource mavenResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        mavenResource.addResources("", scriptsUrls); //$NON-NLS-1$
        list.add(mavenResource);
    }

    protected abstract void addMavenBuildScripts(ExportFileResource[] processes, List<URL> scriptsUrls,
            Map<String, String> mavenPropertiesMap);

}
