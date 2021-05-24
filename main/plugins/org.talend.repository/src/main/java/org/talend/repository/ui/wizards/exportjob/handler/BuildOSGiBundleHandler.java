// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.handler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.maven.launch.MavenPomCommandLauncher;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.OSGIJavaScriptForESBWithMavenManager;

/**
 * DOC yyan class global comment. 2018-1-15
 *
 * For OSGi data service - REST
 */
public class BuildOSGiBundleHandler extends BuildJobHandler {

    private OSGIJavaScriptForESBWithMavenManager osgiMavenManager;

    public BuildOSGiBundleHandler(ProcessItem processItem, String version, String contextName,
            Map<ExportChoice, Object> exportChoiceMap) {
        super(processItem, version, contextName, exportChoiceMap);

        osgiMavenManager = new OSGIJavaScriptForESBWithMavenManager(exportChoiceMap, contextName, JobScriptsManager.LAUNCHER_ALL,
                IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);

        osgiMavenManager.setJobVersion(version);
        osgiMavenManager.setBundleVersion(exportChoiceMap.containsKey(ExportChoice.bundleVersion) ?
        		(String)exportChoiceMap.get(ExportChoice.bundleVersion) : getArtifactVersion(processItem)); 
    }

    protected String getArtifactVersion(ProcessItem processItem) {
        if (processItem != null) {
            String v = PomIdsHelper.getJobVersion(processItem.getProperty());
            if (v != null) {
                return v.replace("-", ".");
            }
        }
        return null;
    }
    

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler#generateJobFiles(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public IProcessor generateJobFiles(IProgressMonitor monitor) throws Exception {
        IProcessor processor = super.generateJobFiles(monitor);
        cleanBundelResources(monitor);
        // Generate class before the final build goal, JobJavaScriptOSGIForESBManager.createAnalyzer needs the classes
        // to compute import-package for the manifest.mf. TalendJavaProjectManager.getTalendJobJavaProject is always
        // disabled MavenNature when create project(false), it will stop jdt to compile, and imporve this part will help
        // to avoid using maven in this step.
        MavenPomCommandLauncher mavenLauncher = new MavenPomCommandLauncher(talendProcessJavaProject.getProjectPom(),
                TalendMavenConstants.GOAL_COMPILE);
        mavenLauncher.setSkipCIBuilder(true);
        mavenLauncher.setSkipTests(true);
        mavenLauncher.execute(monitor);

        List<ExportFileResource> resources = osgiMavenManager
                .getExportResources(new ExportFileResource[] { new ExportFileResource(processItem, "") });
        for (ExportFileResource resource : resources) {
            for (String relativePath : resource.getRelativePathList()) {
                String path = resource.getDirectoryName().isEmpty() ? relativePath : resource.getDirectoryName();
                for (URL url : resource.getResourcesByRelativePath(relativePath)) {
                    String resourceUrl = url.toString();
                    String fileName = resourceUrl.substring(resourceUrl.lastIndexOf('/') + 1, resourceUrl.length());
                    IFile target = getTargetFile(path, fileName, monitor);
                    if (target != null) {
                        setFileContent(new FileInputStream(url.getFile()), target, monitor);
                    }
                }
            }
        }

        return processor;
    }

    private void cleanBundelResources(IProgressMonitor monitor) throws CoreException {
        // clean up bundel resources folder if needed
        for (IResource resource : talendProcessJavaProject.getBundleResourcesFolder().members()) {
            if (resource.exists()) {
                resource.delete(true, monitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler#build(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        super.build(monitor);
    }

    private IFile getTargetFile(String path, String fileName, IProgressMonitor monitor) {
        IFolder folder = null;
        if (path.startsWith(MavenSystemFolders.RESOURCES.getPath())) {
            String sub = path.replaceAll(MavenSystemFolders.RESOURCES.getPath(), "");
            folder = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getBundleResourcesFolder(), sub);
        } else if (path.startsWith(MavenSystemFolders.JAVA.getPath())) {
            String sub = path.replaceAll(MavenSystemFolders.JAVA.getPath(), "");
            folder = talendProcessJavaProject.getSrcSubFolder(monitor, sub);
        } else if (path.startsWith("META-INF")) {
            folder = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getBundleResourcesFolder(), path);
        } else if (path.startsWith("xmlMappings")) {
            folder = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getBundleResourcesFolder(), path);
        }
        return folder == null ? null : folder.getFile(fileName);
    }

    /*
     * Bundle extention is jar
     *
     * @see org.talend.repository.ui.wizards.exportjob.handler.AbstractBuildJobHandler#getJobTargetFile()
     */
    @Override
    public IFile getJobTargetFile() {
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder targetFolder = talendProcessJavaProject.getTargetFolder();
        IFile bundleFile = null;
        try {
            targetFolder.refreshLocal(IResource.DEPTH_ONE, null);

            String esbExportType = exportChoice.get(ExportChoice.esbExportType) == null ? ""
                    : exportChoice.get(ExportChoice.esbExportType).toString();

            String fileExtension = "jar";

            // we only build one zip at a time, so just get the zip file to be able to manage some pom customizations.
            for (IResource resource : targetFolder.members()) {
                if (resource instanceof IFile) {
                    IFile file = (IFile) resource;
                    if (StringUtils.isNotBlank(esbExportType)) {
                        fileExtension = esbExportType;
                    }

                    if (fileExtension.equals(file.getFileExtension())) {
                        bundleFile = file;
                        break;
                    }
                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        return bundleFile;
    }

    private void setFileContent(InputStream inputStream, IFile file, IProgressMonitor monitor) throws CoreException {
        file.refreshLocal(IResource.DEPTH_ZERO, monitor);
        if (file.exists()) {
            file.setContents(inputStream, 0, monitor);
        } else {
            file.create(inputStream, 0, monitor);
        }
    }
}
