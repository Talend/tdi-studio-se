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
package org.talend.designer.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.utils.io.IOUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;

/**
 * Routine synchronizer of java project.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: JavaRoutineSynchronizer.java JavaRoutineSynchronizer 2007-2-2 下午03:29:12 +0000 (下午03:29:12, 2007-2-2 2007)
 * yzhang $
 *
 */
public class JavaRoutineSynchronizer extends AbstractRoutineSynchronizer {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.IRoutineSynchronizer#syncAllRoutines()
     */
    @Override
    public void syncAllRoutines() throws SystemException {
        syncRoutineItems(getRoutines(false), false);
    }

    @Override
    public void syncAllRoutinesForLogOn() throws SystemException {
        syncRoutineItems(getRoutines(true), true);
    }

    @Override
    public void syncAllInnerCodes() throws SystemException {
        syncInnerCodeItems(false);
    }

    @Override
    public void syncAllInnerCodesForLogOn() throws SystemException {
        syncInnerCodeItems(true);
    }

    private void syncRoutineItems(Collection<RoutineItem> routineObjects, boolean forceUpdate) throws SystemException {
        for (RoutineItem routineItem : routineObjects) {
            syncRoutine(routineItem, true, forceUpdate);
        }
        syncSystemRoutine(ProjectManager.getInstance().getCurrentProject());
    }

    @Override
    protected void syncSystemRoutine(Project project) throws SystemException {
        try {
            ILibrariesService jms = CorePlugin.getDefault().getLibrariesService();
            List<URL> urls = jms.getTalendRoutinesFolder();

            for (URL systemModuleURL : urls) {
                if (systemModuleURL != null) {
                    String fileName = systemModuleURL.getPath();
                    if (fileName.startsWith("/")) { //$NON-NLS-1$
                        fileName = fileName.substring(1);
                    }
                    File f = new File(systemModuleURL.getPath());
                    if (f.isDirectory()) {
                        syncModule(project, f.listFiles());
                    }
                }
            }
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    private IFile getTestContainerFile(ProcessItem item) throws SystemException {
        String projectFolderName = JavaResourcesHelper.getProjectFolderName(item);
        String jobName = item.getProperty().getLabel();
        String folderName = JavaResourcesHelper.getJobFolderName(jobName, item.getProperty().getVersion());
        return getTestContainerFile(item, projectFolderName, folderName, jobName);
    }

    private IFile getTestContainerFile(ProcessItem item, String projectFolderName, String folderName, String jobName) {
        IRunProcessService service = CodeGeneratorActivator.getDefault().getRunProcessService();
        ITalendProcessJavaProject talendProcessJavaProject = service.getTalendJobJavaProject(item.getProperty());
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder srcFolder = talendProcessJavaProject.getTestSrcFolder();
        String packageName = JavaResourcesHelper.getJobClassPackageFolder(item, true);
        IFile file = srcFolder.getFile(packageName + '/' + jobName + "Test" + JavaUtils.JAVA_EXTENSION);
        return file;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.IRoutineSynchronizer#syncRoutine(org.talend .core.model.properties.RoutineItem)
     */
    private static void syncModule(Project project, File[] modules) throws SystemException {
        IRunProcessService service = CodeGeneratorActivator.getDefault().getRunProcessService();
        ITalendProcessJavaProject talenCodeJavaProject = service.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES,
                project.getTechnicalLabel());
        if (talenCodeJavaProject == null) {
            return;
        }
        final IFolder systemFolder = talenCodeJavaProject.getSrcSubFolder(null,
                JavaUtils.JAVA_ROUTINES_DIRECTORY + '/' + JavaUtils.JAVA_SYSTEM_DIRECTORY);
        syncModules(modules, systemFolder);
    }

    private static void syncModules(File[] modules, IFolder directory) throws SystemException {
        try {
            if (!directory.exists()) {
                directory.create(true, true, null);
            }
            for (File module : modules) {
                if (!module.isDirectory()) {
                    copyFile(module, directory.getFile(module.getName()));
                } else if (!module.getName().startsWith(".") && !FilesUtils.isSVNFolder(module.getName())) {
                    syncModules(module.listFiles(), directory.getFolder(module.getName()));
                }
            }
        } catch (CoreException e) {
            throw new SystemException(e);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    private static void copyFile(File in, IFile out) throws CoreException, IOException {
        final FileInputStream fis = new FileInputStream(in);
        if (out.exists()) {
            InputStream outIs = out.getContents(true);
            if (!IOUtils.contentEquals(fis, outIs)) {                
                out.setContents(fis, true, false, null);
            }
            outIs.close();
        } else {
            out.create(fis, true, null);
        }
        fis.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ITalendSynchronizer#getFile(org.talend.core .model.properties.Item)
     */
    @Override
    public IFile getFile(Item item) throws SystemException {
        boolean isTestContainer = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                isTestContainer = testContainerService.isTestContainerItem(item);
            }
        }
        if (isTestContainer) {
            return getTestContainerFile((ProcessItem) item);
        }
        return super.getFile(item);
    }

    /*
     * (non-Javadoc)
     *
     * qli modified to fix the bug 5400 and 6185.
     *
     * @seeorg.talend.designer.codegen.AbstractRoutineSynchronizer#renameRoutineClass(org.talend.core.model.properties.
     * RoutineItem, java.lang.String)
     */
    @Override
    public void renameRoutineClass(RoutineItem routineItem) {
        if (routineItem == null) {
            return;
        }
        String routineContent = new String(routineItem.getContent().getInnerContent());
        String label = routineItem.getProperty().getLabel();
        //
        String regexp = "public(\\s)+class(\\s)+\\w+(\\s)+\\{";//$NON-NLS-1$
        routineContent = routineContent.replaceFirst(regexp, "public class " + label + " {");//$NON-NLS-1$//$NON-NLS-2$
        // constructor
        Matcher matcher = Pattern.compile("(.*public\\s+)(\\w+)(\\s*\\(.*)", Pattern.DOTALL).matcher(routineContent); //$NON-NLS-1$
        if (matcher.find()) {
            routineContent = matcher.group(1) + label + matcher.group(3);
        }
        routineItem.getContent().setInnerContent(routineContent.getBytes());
    }

}
