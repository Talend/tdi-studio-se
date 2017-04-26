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
package org.talend.designer.runprocess.java;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
import org.talend.commons.utils.io.FilesUtils;
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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.maven.utils.TalendCodeProjectUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class JavaProcessorUtilities {

    /** The java project within the project. */
    private static ITalendProcessJavaProject talendJavaProject;

    /**
     * A java project under folder .Java will be created if there is no existed.
     * 
     * DOC ggu Comment method "getTalendJavaProject".
     * 
     * @return
     * @throws CoreException
     */
    public static ITalendProcessJavaProject getTalendJavaProject() {
        if (talendJavaProject == null) {
            synchronized (JavaProcessorUtilities.class) {
                if (talendJavaProject == null) {
                    try {
                        IProject project = TalendCodeProjectUtil.initCodeProject(new NullProgressMonitor());
                        if (project != null) {
                            IJavaProject javaProject = JavaCore.create(project);
                            talendJavaProject = new TalendProcessJavaProject(javaProject);

                            // synchronize templates
                            if (talendJavaProject != null) {
                                MavenPomSynchronizer pomSynchronizer = new MavenPomSynchronizer(talendJavaProject);
                                pomSynchronizer.syncTemplates(false);
                            }
                        }
                    } catch (Exception e) {
                        // create failure, only log it?
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return talendJavaProject;
    }

    /**
     * Extracts the set of modules for mapper and reducer dependency. Added by Marvin Wang on Mar 4, 2013.
     * 
     * @param process
     * @return
     */
    public static Set<ModuleNeeded> extractLibsOnlyForMapperAndReducer(IProcess process) {
        Set<ModuleNeeded> allModules = JavaProcessUtil.getNeededModules(process, true, true);
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
                libNames.add(currentModule.getModuleName());
            }
        }
        return libNames;
    }

    /**
     * Extracts the name of libs only for mapper and reducer methods dependency.
     * 
     * @param process
     * @return
     */
    public static Set<String> extractLibNamesOnlyForMapperAndReducer(IProcess2 process) {
        Set<String> libNames = extractLibNamesOnlyForMapperAndReducerWithoutRoutines(process);
        libNames.addAll(PomUtil.getCodesExportJars(process));
        return libNames;
    }

    public static Set<ModuleNeeded> getNeededModulesForProcess(IProcess process) {
        Set<ModuleNeeded> neededLibraries = new TreeSet<ModuleNeeded>(new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded m1, ModuleNeeded m2) {
                return m1.toString().compareTo(m2.toString());
            }
        });

        Set<ModuleNeeded> neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(process.getId(),
                process.getVersion());
        neededLibraries.addAll(neededModules);

        if (process == null || !(process instanceof IProcess2)) {
            if (neededLibraries.isEmpty() && process != null) {
                neededLibraries = process.getNeededModules(true);
                if (neededLibraries == null) {
                    neededLibraries = new HashSet<ModuleNeeded>();
                    // for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getModulesNeeded()) {
                    // neededLibraries.add(moduleNeeded.getModuleName());
                    // }
                }
            } else {
                for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getRunningModules()) {
                    neededLibraries.add(moduleNeeded);
                }
            }
            return neededLibraries;
        }
        Property property = ((IProcess2) process).getProperty();
        if (neededLibraries.isEmpty()) {
            neededLibraries = process.getNeededModules(true);
            if (neededLibraries == null) {
                neededLibraries = new HashSet<ModuleNeeded>();
                for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getModulesNeeded()) {
                    neededLibraries.add(moduleNeeded);
                }
            }
        } else {
            if (property != null && property.getItem() instanceof ProcessItem) {
                List<ModuleNeeded> modulesNeededs = ModulesNeededProvider.getModulesNeededForRoutines(
                        (ProcessItem) property.getItem(), ERepositoryObjectType.ROUTINES);
                for (ModuleNeeded moduleNeeded : modulesNeededs) {
                    neededLibraries.add(moduleNeeded);
                }
                List<ModuleNeeded> modulesForPigudf = ModulesNeededProvider.getModulesNeededForRoutines(
                        (ProcessItem) property.getItem(), ERepositoryObjectType.PIG_UDF);
                for (ModuleNeeded moduleNeeded : modulesForPigudf) {
                    neededLibraries.add(moduleNeeded);
                }

            } else {
                for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getRunningModules()) {
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
                                ModuleNeeded neededModule = new ModuleNeeded("camel bean dependencies", type.getMODULE(),
                                        "camel bean dependencies", true);
                                neededLibraries.add(neededModule);
                            }
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
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
            Set<String> jarsNeedRetrieve = new HashSet<String>();
            for (ModuleNeeded moduleNeeded : jarsNeeded) {
                jarsNeedRetrieve.add(moduleNeeded.getModuleName());
            }
            for (File externalLib : libDir.listFiles(FilesUtils.getAcceptJARFilesFilter())) {
                jarsNeedRetrieve.remove(externalLib.getName());
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
        if (alreadyRetrievedModules.isEmpty()) {
            // to update this only one time in one build of full job/subjobs
            checkAndUpdateLog4jFile();
        }
    }

    // // see bug 3914, make the order of the jar files consistent with the
    // command
    // // line in run mode
    private static void sortClasspath(Set<ModuleNeeded> jobModuleList, IProcess process, Set<ModuleNeeded> alreadyRetrievedModules)
            throws CoreException, ProcessorException {
        ITalendProcessJavaProject jProject = getTalendJavaProject();
        if (jProject == null) {
            return;
        }
        Set<ModuleNeeded> listModulesReallyNeeded = new HashSet<ModuleNeeded>();
        listModulesReallyNeeded.addAll(jobModuleList);

        Set<ModuleNeeded> optionalJarsOnlyForRoutines = new HashSet<ModuleNeeded>();

        // only for wizards or additional jars only to make the java project compile without any error.
        for (ModuleNeeded moduleNeeded : ModulesNeededProvider.getRunningModules()) {
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

        addLog4jToModuleList(listModulesReallyNeeded);
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
        repositoryBundleService.deployModules(listModulesReallyNeeded, null);
        if (missingJars != null) {
            handleMissingJarsForProcess(missingJarsForRoutinesOnly, missingJarsForProcessOnly, missingJars);
        }
    }

    public static boolean addLog4jToModuleList(Collection<ModuleNeeded> jarList) {
        boolean added = false;
        boolean foundLog4jJar = false;
        for (ModuleNeeded jar : jarList) {
            if (jar.getModuleName().matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                foundLog4jJar = true;
            }
        }
        if (!foundLog4jJar) {
            ModuleNeeded log4j = new ModuleNeeded("log4j", "log4j-1.2.16.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4j.setMavenUri("mvn:org.talend.libraries/log4j-1.2.16/6.0.0");
            jarList.add(log4j);
            added = true;
        }

        return added;
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
            if (!CommonsPlugin.isHeadless()) {
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
            } else {
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

    public static void checkAndUpdateLog4jFile() {
        try {
            ITalendProcessJavaProject jProject = JavaProcessorUtilities.getTalendJavaProject();
            if (jProject != null) {
                IRunProcessService service = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                    service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
                }
                if (service != null) {
                    service.updateLogFiles(jProject.getProject(), true);
                }

            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC ycbai Comment method "getJavaProjectLibPath".
     * 
     * @return
     */
    public static File getJavaProjectLibFolder() {
        try {
            ITalendProcessJavaProject jProject = getTalendJavaProject();
            if (jProject != null) {
                IFolder libFolder = jProject.getLibFolder();
                if (libFolder != null) {
                    File libDir = libFolder.getLocation().toFile();
                    return libDir;
                }
            }
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
