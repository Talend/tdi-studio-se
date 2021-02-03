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
package org.talend.designer.runprocess.java;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.BigDataJobUtil;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class JavaProcessorUtilities {


    /**
     * @deprecated use {@link org.talend.designer.runprocess.java.TalendJavaProjectManager#getTalendJobJavaProject(Property)} instead
     */
    @Deprecated
    public static ITalendProcessJavaProject getTalendJavaProject() {
        return null;
    }

    /**
     * Extracts the set of modules for mapper and reducer dependency. Added by Marvin Wang on Mar 4, 2013.
     *
     * @param process
     * @return
     */
    public static Set<ModuleNeeded> extractLibsOnlyForMapperAndReducer(IProcess process) {
        int options = TalendProcessOptionConstants.MODULES_WITH_CHILDREN | TalendProcessOptionConstants.MODULES_FOR_MR
                | TalendProcessOptionConstants.MODULES_EXCLUDE_SHADED;
        Set<ModuleNeeded> allModules = JavaProcessUtil.getNeededModules(process, options);
        return allModules;
    }

    /**
     * Extracts the name of libs only for mapper and reducer methods dependency, excluding the routines/beans/udfs.
     *
     * @param process
     * @return
     */
    public static Set<String> extractLibNamesOnlyForMapperAndReducerWithoutRoutines(IProcess2 process) {
        Set<String> libNames = new HashSet<String>();
        Set<ModuleNeeded> libs = extractLibsOnlyForMapperAndReducer(process);
        if (libs != null) {
            Iterator<ModuleNeeded> itLibs = libs.iterator();
            while (itLibs.hasNext()) {
                ModuleNeeded currentModule = itLibs.next();
                if (ProcessorUtilities.hadoopConfJarCanBeLoadedDynamically(process.getProperty())) {
                    Object obj = currentModule.getExtraAttributes().get(HadoopConstants.IS_DYNAMIC_JAR);
                    if (Boolean.valueOf(String.valueOf(obj))) {
                        continue;
                    }
                }
                libNames.add(MavenUrlHelper.generateModuleNameByMavenURI(currentModule.getMavenUri()));
            }
        }
        return libNames;
    }

    /**
     * Extracts the name of libs only for mapper and reducer methods dependency.
     *
     * @param process
     * @return
     * @deprecated
     */
    public static Set<String> extractLibNamesOnlyForMapperAndReducer(IProcess2 process) {
        Set<String> libNames = extractLibNamesOnlyForMapperAndReducerWithoutRoutines(process);
        libNames.addAll(JavaProcessUtil.getCodesExportJars(process));
        return libNames;
    }

    public static Set<String> extractLibNamesOnlyForMapperAndReducer(IProcessor processor) {
        Set<String> libNames = extractLibNamesOnlyForMapperAndReducerWithoutRoutines((IProcess2) processor.getProcess());
        libNames.addAll(JavaProcessUtil.getCodesExportJars(processor));
        return libNames;
    }

    public static Set<ModuleNeeded> getNeededModulesForProcess(IProcess process) {
        return getNeededModulesForProcess(process, TalendProcessOptionConstants.MODULES_WITH_CHILDREN);
    }

    public static Set<ModuleNeeded> getNeededModulesForProcess(IProcess process, int options) {
        Set<ModuleNeeded> neededLibraries = new TreeSet<>(new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded m1, ModuleNeeded m2) {
                // FIXME still have possible bug if version 1.1 > 1.11
                // should use MavenVersionHelper to compare version
                int compareResult = m1.toString().compareTo(m2.toString());
                return compareResult != 0 ? compareResult
                        : MavenUrlHelper.getCoordinate(m1.getMavenUri())
                                .compareTo(MavenUrlHelper.getCoordinate(m2.getMavenUri()));
            }
        });

        boolean includeCodesJar = BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_WITH_CODESJAR);
        Set<ModuleNeeded> neededModules;
        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_WITH_CHILDREN)) {
            neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(process.getId(),
                process.getVersion());
            if (includeCodesJar) {
                neededModules.addAll(LastGenerationInfo.getInstance().getCodesJarModulesNeededWithSubjobPerJob(process.getId(),
                        process.getVersion()));
            }
        } else {
            neededModules = LastGenerationInfo.getInstance().getModulesNeededPerJob(process.getId(),
                    process.getVersion());
            Set<ModuleNeeded> neededModulesWithSubjobs = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(process.getId(),
                    process.getVersion());

            Iterator<ModuleNeeded> it = neededModules.iterator();
            while(it.hasNext()) {
                ModuleNeeded module = it.next();
                if (!neededModulesWithSubjobs.contains(module)) {
                    // in case the classpath adjuster removed dependencies (cf use in ProcessorUtilities)
                    it.remove();
                }
            }
            if (includeCodesJar) {
                neededModules.addAll(
                        LastGenerationInfo.getInstance().getCodesJarModulesNeededPerJob(process.getId(), process.getVersion()));
            }
        }
        neededLibraries.addAll(neededModules);

        if (!(process instanceof IProcess2)) {
            if (neededLibraries.isEmpty()) {
                neededLibraries = process.getNeededModules(options);
                if (neededLibraries == null) {
                    neededLibraries = new HashSet<ModuleNeeded>();
                }
            } else {
                neededLibraries.addAll(ModulesNeededProvider.getRunningModules(false));
            }
            return neededLibraries;
        }
        Property property = ((IProcess2) process).getProperty();
        if (neededLibraries.isEmpty()) {
            neededLibraries = process.getNeededModules(options);
            if (neededLibraries == null) {
                neededLibraries = new HashSet<>();
                neededLibraries.addAll(ModulesNeededProvider.getModulesNeeded());
            }
        } else {
            if (property != null && property.getItem() instanceof ProcessItem) {
                neededLibraries.addAll(ModulesNeededProvider.getCodesModulesNeededForProcess((ProcessItem) property.getItem(),
                        process, includeCodesJar));
            } else {
                for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getRunningModules(false)) {
                    neededLibraries.add(moduleNeeded);
                }
            }
        }
        if (property != null && GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (camelService.isInstanceofCamel(property.getItem())) {
                // http://jira.talendforge.org/browse/TESB-5887 LiXiaopeng 2012-6-19
                // Synchronize Route resources
                camelService.synchronizeRouteResource((ProcessItem) property.getItem());
                try {
                    for (IRepositoryViewObject object : CoreRuntimePlugin.getInstance().getProxyRepositoryFactory()
                            .getAll(camelService.getBeansType())) {
                        Item item = object.getProperty().getItem();
                        if (item instanceof RoutineItem) {
                            RoutineItem routine = (RoutineItem) item;
                            for (Object o : routine.getImports()) {
                                IMPORTType type = (IMPORTType) o;
                                
                                boolean needToAdd = true;
                                ModuleNeeded neededModule = new ModuleNeeded("camel bean dependencies", type.getMODULE(),
                                        "camel bean dependencies", true);

                                for(ModuleNeeded mn:neededLibraries) {
                                    if (StringUtils.equals(mn.getModuleName(), type.getMODULE())) {
                                        if (StringUtils.equals(neededModule.getModuleLocaion(), mn.getModuleLocaion())) {
                                            needToAdd = false;
                                            break;
                                        }
                                    }
                                }
                                
                                if (needToAdd) {
                                    neededLibraries.add(neededModule);
                                }

                            }
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }

        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_EXCLUDE_SHADED)) {
            new BigDataJobUtil(process).removeExcludedModules(neededLibraries);
        }

        // move high priority modules to front.
        Set<ModuleNeeded> highPriorityModuleNeeded = LastGenerationInfo.getInstance()
                .getHighPriorityModuleNeeded(property.getId(), property.getVersion());
        if (!highPriorityModuleNeeded.isEmpty()) {
            Iterator<ModuleNeeded> iterator = highPriorityModuleNeeded.iterator();
            while (iterator.hasNext()) {
                ModuleNeeded needed = iterator.next();
                if (highPriorityModuleNeeded.contains(needed)) {
                    neededLibraries.remove(needed);
                }
            }
            // order should be main -> sub1 -> sub_sub1 -> normal modules
            Set<ModuleNeeded> orderedNeededLibraries = new LinkedHashSet<>();
            orderedNeededLibraries.addAll(highPriorityModuleNeeded);
            orderedNeededLibraries.addAll(neededLibraries);
            return orderedNeededLibraries;
        }

        return neededLibraries;
    }

    /**
     * DOC ycbai Comment method "checkJavaProjectLib".
     *
     * @param jarsNeeded
     */
    public static void checkJavaProjectLib(Collection<ModuleNeeded> jarsNeeded) {
        File libDir = getJavaProjectLibFolder();
        if ((libDir != null) && (libDir.isDirectory())) {
            Set<ModuleNeeded> jarsNeedRetrieve = new HashSet<ModuleNeeded>(jarsNeeded);
            File[] listFiles = libDir.listFiles(FilesUtils.getAcceptJARFilesFilter());
            for (ModuleNeeded moduleNeeded : jarsNeeded) {
                for (File externalLib : listFiles) {
                    if (moduleNeeded.getModuleName().equals(externalLib.getName())) {
                        jarsNeedRetrieve.remove(moduleNeeded);
                        break;
                    }
                }
            }

            if (!jarsNeedRetrieve.isEmpty()) {
                ILibraryManagerService repositoryBundleService = CorePlugin.getDefault().getRepositoryBundleService();
                repositoryBundleService.retrieve(jarsNeedRetrieve, libDir.getAbsolutePath(), false);
            }
        }
    }

    private static String projectSetup;

    /*
     * @see bug 0005633. Classpath error when current job inlcude some tRunJob-es.
     *
     * @see org.talend.designer.runprocess.IProcessor#computeLibrariesPath(Set<String>)
     */
    public static void computeLibrariesPath(Set<ModuleNeeded> jobModuleList, IProcess process) {
        try {
            computeLibrariesPath(jobModuleList, process, new HashSet<ModuleNeeded>());
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC nrousseau Comment method "computeLibrariesPath".
     *
     * @param hashSet
     * @param process
     * @param alreadyRetrievedModules
     * @throws BusinessException
     */
    public static void computeLibrariesPath(Set<ModuleNeeded> jobModuleList, IProcess process,
            Set<ModuleNeeded> alreadyRetrievedModules) throws ProcessorException {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        Project project = repositoryContext.getProject();
        if (projectSetup == null || !projectSetup.equals(project.getTechnicalLabel())) {
            projectSetup = project.getTechnicalLabel();
        }
        // use maven to update the class path.
        try {
            sortClasspath(jobModuleList, process, alreadyRetrievedModules);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        if (/*alreadyRetrievedModules.isEmpty()*/ true) { // FIXME check TDI-35139
            // to update this only one time in one build of full job/subjobs
            if (process instanceof Process) {
                IRunProcessService service = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                    service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
                }
                if (service != null) {
                    ITalendProcessJavaProject talendProject = service.getTalendJobJavaProject(((Process) process).getProperty());
                    service.updateLogFiles(talendProject, true);
                }
            }
        }
    }

    // // see bug 3914, make the order of the jar files consistent with the
    // command
    // // line in run mode
    private static void sortClasspath(Set<ModuleNeeded> jobModuleList, IProcess process, Set<ModuleNeeded> alreadyRetrievedModules)
            throws CoreException, ProcessorException {
        Set<ModuleNeeded> listModulesReallyNeeded = new HashSet<ModuleNeeded>();
        listModulesReallyNeeded.addAll(jobModuleList);

        Set<ModuleNeeded> optionalJarsOnlyForRoutines = new HashSet<ModuleNeeded>();

        // only for wizards or additional jars only to make the java project compile without any error.
        for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getSystemRunningModules()) {
            optionalJarsOnlyForRoutines.add(moduleNeeded);
        }

        // list contains all routines linked to job as well as routines not used in the job
        // rebuild the list to have only the libs linked to routines "not used".
        optionalJarsOnlyForRoutines.removeAll(listModulesReallyNeeded);

        // at this point, the Set for optional jars really only contains optional jars for routines
        // only to be able to compile java project without error.
        for (ModuleNeeded jar : optionalJarsOnlyForRoutines) {
            listModulesReallyNeeded.add(jar);
        }

        listModulesReallyNeeded.removeAll(alreadyRetrievedModules);
        alreadyRetrievedModules.addAll(listModulesReallyNeeded);

        String missingJars = null;
        Set<String> missingJarsForRoutinesOnly = new HashSet<String>();
        Set<String> missingJarsForProcessOnly = new HashSet<String>();
        File libDir = getJavaProjectLibFolder();
        ILibraryManagerService repositoryBundleService = CorePlugin.getDefault().getRepositoryBundleService();
        if ((libDir != null) && (libDir.isDirectory())) {
            Set<ModuleNeeded> jarsNeedRetrieve = new HashSet<ModuleNeeded>();
            for (ModuleNeeded moduleNeeded : listModulesReallyNeeded) {
                jarsNeedRetrieve.add(moduleNeeded);
            }

            if (!jarsNeedRetrieve.isEmpty()) {
                repositoryBundleService.retrieve(jarsNeedRetrieve, libDir.getAbsolutePath(), true);

                if (process instanceof IProcess2) {
                    ((IProcess2) process).checkProcess();
                }
            }

            Set<ModuleNeeded> exist = new HashSet<ModuleNeeded>();
            for (File externalLib : libDir.listFiles(FilesUtils.getAcceptJARFilesFilter())) {
                for (ModuleNeeded module : jarsNeedRetrieve) {
                    if (externalLib.getName().equals(module.getModuleName())) {
                        exist.add(module);
                    }

                }
            }
            jarsNeedRetrieve.removeAll(exist);
            Set<String> jarStringListNeededByProcess = new HashSet<String>();
            for (ModuleNeeded moduleNeeded : jobModuleList) {
                jarStringListNeededByProcess.add(moduleNeeded.getModuleName());
            }
            for (ModuleNeeded jar : jarsNeedRetrieve) {
                if (jobModuleList.contains(jar)) {
                    missingJarsForProcessOnly.add(jar.getModuleName());
                } else {
                    missingJarsForRoutinesOnly.add(jar.getModuleName());
                }
                if (missingJars == null) {
                    missingJars = Messages.getString("JavaProcessorUtilities.msg.missingjar.forProcess") + jar; //$NON-NLS-1$
                } else {
                    missingJars = missingJars + ", " + jar; //$NON-NLS-1$
                }
            }
        }
        repositoryBundleService.installModules(listModulesReallyNeeded, null);
        if (missingJars != null) {
            handleMissingJarsForProcess(missingJarsForRoutinesOnly, missingJarsForProcessOnly, missingJars);
        }
        if (ModulesNeededProvider.installModuleForRoutineOrBeans()) {
            AggregatorPomsHelper ph = new AggregatorPomsHelper();
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                ph.updateCodeProjects(monitor, false, true);
                // FIXME can remove this line if retrieve/install modules are done
                CodesJarM2CacheManager.updateCodesJarProject(monitor);
                ModulesNeededProvider.setInstallModuleForRoutineOrBeans();
            } catch (Exception e) {
                CommonExceptionHandler.process(e);
            }
        }
    }


    /**
     *
     * Added by Marvin Wang on Nov 7, 2012.
     *
     * @param missingJarsForRoutines
     * @param missingJarsForProcess
     * @param missingJars
     * @throws BusinessException
     */
    private static void handleMissingJarsForProcess(Set<String> missingJarsForRoutines, final Set<String> missingJarsForProcess,
            String missingJars) throws ProcessorException {
        final StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
        if (missingJarsForProcess.size() > 0) {
            sb.append(Messages.getString("JavaProcessorUtilities.msg.missingjar.forProcess")); //$NON-NLS-1$
            for (String missingJar : missingJarsForProcess) {
                sb.append(missingJar);
                sb.append(", "); //$NON-NLS-1$
            }
            if (missingJarsForRoutines.size() > 0) {
                // subForMsg(sb.toString());
                sb.append("\r\n\r\n\r\n"); //$NON-NLS-1$
                sb.append(Messages.getString("JavaProcessorUtilities.msg.missingjar.note")); //$NON-NLS-1$
                sb.append("\r\n"); //$NON-NLS-1$
                sb.append(Messages.getString("JavaProcessorUtilities.msg.missingjar.onlyforroutine")); //$NON-NLS-1$
                sb.append("\r\n"); //$NON-NLS-1$
                for (String missingJar : missingJarsForRoutines) {
                    sb.append(missingJar);
                    sb.append(", "); //$NON-NLS-1$
                }
                subForMsg(sb.toString());
            } else {
                subForMsg(sb.toString());
            }
            if (!CommonsPlugin.isHeadless() && !CommonsPlugin.isJUnitTest()) {
                Display display = DisplayUtils.getDisplay();
                if (display != null) {
                    display.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            // fix for TDI-24906
                            MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), Messages
                                    .getString("JavaProcessorUtilities.msg.missingjar.warningtitle"), null, subForMsg(sb
                                    .toString()), 4, new String[] { IDialogConstants.OK_LABEL }, 0) {

                                /*
                                 * (non-Javadoc)
                                 *
                                 * @see org.eclipse.jface.window.Window#setShellStyle(int)
                                 */
                                @Override
                                protected void setShellStyle(int newShellStyle) {
                                    super.setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
                                }
                            };
                            dialog.open();
                        }

                    });
                }
            } else if (!Boolean.getBoolean("skip_missing_jars")) {
                throw new ProcessorException(missingJars);
            }

        } else {
            if (missingJarsForRoutines.size() > 0) {
                sb.append(Messages.getString("JavaProcessorUtilities.msg.missingjar.onlyforroutine")); //$NON-NLS-1$
                for (String missingJar : missingJarsForRoutines) {
                    sb.append(missingJar);
                    sb.append(", "); //$NON-NLS-1$
                }
                CommonExceptionHandler.warn(subForMsg(sb.toString()));
            }
        }
    }

    private static String subForMsg(String outputMsg) {
        String trimedStr = outputMsg.trim();
        int lastIndexOf = trimedStr.lastIndexOf(","); //$NON-NLS-1$
        return outputMsg.trim().substring(0, lastIndexOf);
    }

    private static int indexOfEntry(final IClasspathEntry[] dest, final String jarName) {

        if (jarName == null) {
            return -1;
        }

        for (int i = 0; i < dest.length; i++) {
            IClasspathEntry entry = dest[i];

            if (entry == null) {
                continue;
            }

            if (entry.getPath() == null || entry.getPath().lastSegment() == null) {
                continue;
            }
            if (entry.getPath().lastSegment().equals(jarName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * DOC ycbai Comment method "getJavaProjectLibPath".
     *
     * @return
     */
    public static File getJavaProjectLibFolder() {
        IFolder libFolder = getJavaProjectLibFolder2();
        if (libFolder != null) {
            return libFolder.getLocation().toFile();
        }
        return null;
    }

    public static IFolder getJavaProjectLibFolder2() {
        try {
            IProject fsProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            IFolder libFolder = ResourceUtils.getFolder(fsProject,
                    RepositoryConstants.TEMP_DIRECTORY + "/" + JavaUtils.JAVA_LIB_DIRECTORY, false); //$NON-NLS-1$
            if (!libFolder.exists()) {
                ResourceUtils.createFolder(libFolder);
            }
            return libFolder;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static boolean hasBatchOrStreamingSubProcess(Item item) throws PersistenceException {
        return hasBatchOrStreamingSubProcess(item, new HashSet<String>());
    }

    public static boolean hasBatchOrStreamingSubProcess(Item item, Set<String> testedItems) throws PersistenceException {
        if (testedItems.contains(item.getProperty().getId())) {
            return false;
        }
        testedItems.add(item.getProperty().getId());
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
            IMRProcessService batchService = (IMRProcessService) GlobalServiceRegister.getDefault().getService(
                    IMRProcessService.class);
            if (batchService.isMapReduceItem(item)) {
                return true;
            }
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
            IStormProcessService streamingService = (IStormProcessService) GlobalServiceRegister.getDefault().getService(
                    IStormProcessService.class);
            if (streamingService.isStormItem(item)) {
                return true;
            }
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)
                || GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
            if (item != null && item.eClass() == PropertiesPackage.Literals.PROCESS_ITEM) {
                ProcessType processType = ((ProcessItem) item).getProcess();
                EList<NodeType> nodes = processType.getNode();
                for (NodeType node : nodes) {
                    if ("tRunJob".equals(node.getComponentName())) {//$NON-NLS-1$
                        EList<ElementParameterType> elementParameters = node.getElementParameter();
                        for (ElementParameterType param : elementParameters) {
                            if (param.getName() != null && "PROCESS:PROCESS_TYPE_PROCESS".equals(param.getName())) {//$NON-NLS-1$
                                Object value = param.getValue();
                                if (value != null && !"".equals(value)) {//$NON-NLS-1$
                                    IRepositoryViewObject lastVersion = RunProcessPlugin.getDefault().getRepositoryService()
                                            .getProxyRepositoryFactory().getLastVersion(value.toString());
                                    if (lastVersion != null) {
                                        boolean hasBatchOrStreaming = hasBatchOrStreamingSubProcess(lastVersion.getProperty()
                                                .getItem(), testedItems);
                                        if (hasBatchOrStreaming) {
                                            // only stop the loop once we checked every child
                                            return hasBatchOrStreaming;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
