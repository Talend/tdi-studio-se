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
package org.talend.designer.codegen.model;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.codegen.CodeGenPlugin;
import org.eclipse.emf.codegen.jet.JETEmitter;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.runtime.utils.io.IOUtils;
import org.talend.commons.ui.runtime.CommonUIPlugin;
import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.repository.ExternalNodesFactory;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.LightJetBean;
import org.talend.designer.codegen.config.TalendJetEmitter;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.core.model.components.EmfComponent;

/**
 * Pool of initialized Jet Emitters. There are as many Emitters in this pool as Templzte available. Used for generation
 * performance constraint.
 *
 * $Id$
 *
 */
public final class CodeGeneratorEmittersPoolFactory {

    private static HashMap<JetBean, JETEmitter> emitterPool = null;

    private static boolean initialized = false;

    private static boolean initializeStart = false;

    private static Logger log = Logger.getLogger(CodeGeneratorEmittersPoolFactory.class);

    private static List<JetBean> jetFilesCompileFail = new ArrayList<JetBean>();

    private static String defaultTemplate = null;

    public static final String JET_PROJECT = ".JETEmitters"; //$NON-NLS-1$

    /**
     * Default Constructor. Must not be used.
     */
    private CodeGeneratorEmittersPoolFactory() {
    }

    private static JobRunnable jobRunnable = null;

    private static IStatus status = null;

    private static DelegateProgressMonitor delegateMonitor = new DelegateProgressMonitor();

    private static boolean firstTime = true;

    /***/
    private static class JobRunnable extends Thread {

        public JobRunnable(String name) {
            super(name);
            initializeStart = true;
        }

        @Override
        public void run() {
            status = doRun();
        }

        public IStatus doRun() {
            try {
                TimeMeasure.display = CommonsPlugin.isDebugMode();
                TimeMeasure.displaySteps = CommonsPlugin.isDebugMode();
                TimeMeasure.measureActive = CommonsPlugin.isDebugMode();

                TimeMeasure.begin("initialize Jet Emitters"); //$NON-NLS-1$

                jetFilesCompileFail.clear();

                IProgressMonitor monitorWrap = null;
                monitorWrap = new NullProgressMonitor();
                ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();

                initializeJetEmittersProject(monitorWrap);

                TimeMeasure.step("initialize Jet Emitters", "initialize JET Project"); //$NON-NLS-1$ //$NON-NLS-2$
                CodeGeneratorInternalTemplatesFactory templatesFactory = CodeGeneratorInternalTemplatesFactoryProvider
                        .getInstance();
                templatesFactory.setCurrentLanguage(codeLanguage);
                templatesFactory.init();

                IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

                long startTime = System.currentTimeMillis();

                defaultTemplate = templatesFactory.getTemplatesFromType(EInternalTemplate.DEFAULT_TEMPLATE).get(0)
                        .getTemplateRelativeUri();

                List<JetBean> jetBeans = new ArrayList<JetBean>();
                List<TemplateUtil> templates = templatesFactory.getTemplates();
                Collection<IComponent> components = componentsFactory.readComponents();
                List<IComponent> genericComponents = new ArrayList<IComponent>();// generic components
                TimeMeasure.step("initialize Jet Emitters", "getComponents"); //$NON-NLS-1$ //$NON-NLS-2$

                monitorWrap.beginTask(Messages.getString("CodeGeneratorEmittersPoolFactory.initMessage"), //$NON-NLS-1$
                        (2 * templates.size() + 5 * components.size()));

                int monitorBuffer = 0;
                for (TemplateUtil template : templates) {
                    JetBean jetBean = initializeUtilTemplate(template, codeLanguage);
                    jetBeans.add(jetBean);
                    monitorBuffer++;
                    if (monitorBuffer % 100 == 0) {
                        monitorWrap.worked(100);
                        monitorBuffer = 0;
                    }
                }
                TimeMeasure.step("initialize Jet Emitters", "initialize jet beans from templates"); //$NON-NLS-1$ //$NON-NLS-2$

                if (components != null) {
                    ECodePart codePart = ECodePart.MAIN;
                    for (IComponent component : components) {
                        // don't do anything for generic component?
                        if (EComponentType.GENERIC.equals(component.getComponentType())) {
                            genericComponents.add(component);
                            continue;
                        }
                        if (component.getAvailableCodeParts().size() > 0) {
                            initComponent(codeLanguage, jetBeans, codePart, component);
                        }
                        monitorBuffer++;
                        if (monitorBuffer % 100 == 0) {
                            monitorWrap.worked(100);
                            monitorBuffer = 0;
                        }
                    }
                }

                // Quick fix to avoid reinitialization of generic components. We have only two kinds of generic
                // components right now.
                // They have different javajet files, so we need to initialize once each of them.
                // Should be a better fix for this situation, but this is only a quick fix.
                Set<String> genericTemplates = new HashSet<>();
                // initialize generic component begin/main/end
                for (IComponent genericComponent : genericComponents) {
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.BEGIN, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.END, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.END_HEAD, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.END_BODY, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.END_TAIL, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.MAIN, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.PROCESS_DATA_BEGIN, genericComponent,
                            genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.PROCESS_DATA_END, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.PROCESS_RECORDS_END, genericComponent, genericTemplates);
                    initGenericComponent(codeLanguage, jetBeans, ECodePart.FINALLY, genericComponent, genericTemplates);
                }

                TimeMeasure.step("initialize Jet Emitters", "initialize jet beans from components"); //$NON-NLS-1$ //$NON-NLS-2$
                monitorWrap.worked(monitorBuffer);

                initializeEmittersPool(jetBeans, codeLanguage, monitorWrap);
                monitorWrap.done();
                TimeMeasure.step("initialize Jet Emitters", "initialize and generate each jet emitters"); //$NON-NLS-1$ //$NON-NLS-2$

                if (!CommonUIPlugin.isFullyHeadless()) {
                    if (firstTime) {
                        Job job = new Job(Messages.getString("CodeGeneratorEmittersPoolFactory.updatePaletteForEditors")) { //$NON-NLS-1$

                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                CorePlugin.getDefault().getDesignerCoreService().synchronizeDesignerUI(
                                        new PropertyChangeEvent(this, IComponentConstants.NORMAL, null, null));
                                return Status.OK_STATUS;
                            }

                        };
                        job.setUser(true);
                        job.setPriority(Job.INTERACTIVE);
                        job.schedule();
                        job.wakeUp(); // start as soon as possible
                        firstTime = false;
                    }
                }
                log.debug(Messages.getString("CodeGeneratorEmittersPoolFactory.componentCompiled", //$NON-NLS-1$
                        (System.currentTimeMillis() - startTime)));
                initialized = true;

                ComponentCompilations.addMarkers();
                initializeStart = false;

            } catch (Exception e) {
                log.error(Messages.getString("CodeGeneratorEmittersPoolFactory.initialException"), e); //$NON-NLS-1$
                TimeMeasure.end("initialize Jet Emitters"); //$NON-NLS-1$
                TimeMeasure.display = false;
                TimeMeasure.displaySteps = false;
                TimeMeasure.measureActive = false;
                return new Status(IStatus.ERROR, CodeGeneratorActivator.PLUGIN_ID,
                        Messages.getString("CodeGeneratorEmittersPoolFactory.initialException"), e); //$NON-NLS-1$
            }
            // finally {
            // try {
            // IWorkspace workspace = ResourcesPlugin.getWorkspace();
            // IProject project = workspace.getRoot().getProject(JET_PROJECT);
            // project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
            // TimeMeasure.step("initialize Jet Emitters", "build project .JETEmitters"); //$NON-NLS-1$ //$NON-NLS-2$
            // } catch (CoreException e) {
            // ExceptionHandler.process(e);
            // }
            // }
            TimeMeasure.end("initialize Jet Emitters"); //$NON-NLS-1$
            TimeMeasure.display = false;
            TimeMeasure.displaySteps = false;
            TimeMeasure.measureActive = false;
            if (jetFilesCompileFail.size() > 0) {
                StringBuilder message = new StringBuilder();
                for (JetBean tmpJetBean : jetFilesCompileFail) {
                    if (message.length() > 0) {
                        message.append(",\r\n").append(tmpJetBean.getTemplateRelativeUri()); //$NON-NLS-1$
                    } else {
                        message.append(tmpJetBean.getTemplateRelativeUri());
                    }
                }
                return new Status(IStatus.ERROR, CodeGeneratorActivator.PLUGIN_ID,
                        Messages.getString("CodeGeneratorEmittersPoolFactory.failCompail") //$NON-NLS-1$
                                + message.toString());
            }
            if (!CommonsPlugin.isHeadless()) {
                CorePlugin.getDefault().getRcpService().activeSwitchProjectAction();
            }
            return Status.OK_STATUS;
        }

        /**
         * DOC nrousseau Comment method "initializeJetEmittersProject".
         *
         * @throws CoreException
         */
        private void initializeJetEmittersProject(IProgressMonitor progressMonitor) throws CoreException {
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();

            IProject project = workspace.getRoot().getProject(JET_PROJECT);
            progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETPreparingProject_message", //$NON-NLS-1$
                    new Object[] { project.getName() }));
            if (project.exists() && !project.isAccessible()) {
                // .metadata missing, so need to reimport project to add it in the metadata.
                progressMonitor.subTask("Reinitilializing project " + project.getName()); //$NON-NLS-1$
                project.create(new SubProgressMonitor(progressMonitor, 1));
                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreatingProject_message", //$NON-NLS-1$
                        new Object[] { project.getName() }));
            } else if (!project.isAccessible()) {
                // project was deleted manually on the disk. The delete here will remove infos from metadata
                // then we'll be able to create a new clean project.
                project.delete(true, progressMonitor);
            }
            if (!project.exists()) {
                progressMonitor.subTask("JET creating project " + project.getName()); //$NON-NLS-1$
                project.create(new SubProgressMonitor(progressMonitor, 1));
                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreatingProject_message", //$NON-NLS-1$
                        new Object[] { project.getName() }));
            }
            if (!project.isOpen()) {
                project.open(new SubProgressMonitor(progressMonitor, 5));
                project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(progressMonitor, 1));
            }
        }

    };

    public static Job initialize() {
        Job job = new Job(Messages.getString("CodeGeneratorEmittersPoolFactory.initMessage")) { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                synchronized (delegateMonitor) {
                    if (jobRunnable == null) {
                        jobRunnable = new JobRunnable(Messages.getString("CodeGeneratorEmittersPoolFactory.codeThread")); //$NON-NLS-1$
                        jobRunnable.start();
                    }
                }

                delegateMonitor.addDelegate(monitor);

                while (jobRunnable != null && jobRunnable.isAlive()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }

                delegateMonitor.clearDelegate();

                synchronized (delegateMonitor) {
                    jobRunnable = null;
                }

                return status;
            }

        };
        job.setUser(false);
        job.setPriority(Job.LONG);
        job.schedule();
        job.wakeUp(); // start as soon as possible
        return job;
    }

    /**
     *
     * Utility method that create a {@link DelegateClassLoader} made of a given parent {@link ClassLoader} and a
     * delegate one given a bundle name and a class name.
     *
     * @param baseClassLoader the parent ClassLoader
     * @param bundleName the name of the bundle to load the className from.
     * @param className the className to load in order to get its ClassLoader
     * @return the {@link DelegateClassLoader} made of the parent ClassLoader and the new created secondary ClassLoader.
     */
    private static ClassLoader createDelegateClassLoader(ClassLoader baseClassLoader, String bundleName, String className) {
        try {
            // This secondary class loader is used to find other classes in the given bundle. None of the classes in the
            // parent can depend on these classes, but these can depend on the parent.
            ClassLoader secondaryClassLoader = Platform.getBundle(bundleName).loadClass(className).getClassLoader();
            return new DelegateClassLoader(baseClassLoader, secondaryClassLoader);
        } catch (ClassNotFoundException e) {
            ExceptionHandler.process(e);
        }
        return baseClassLoader;
    }

    /**
     * initialization of available templates.
     *
     * @param template
     * @param codeLanguage
     * @return
     */
    private static JetBean initializeUtilTemplate(TemplateUtil template, ECodeLanguage codeLanguage) {
        JetBean jetBean = new JetBean(template.getJetPluginRepository(), template.getTemplateRelativeUri(),
                template.getResourceName(), template.getVersion(), codeLanguage.getName(), ""); //$NON-NLS-1$
        addCommonLibs(jetBean);
        jetBean.addClassPath("COMPONENT_FRAMEWORK", "org.talend.components.api"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("TALEND_MODEL", "org.talend.model"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("DAIKON", "org.talend.daikon"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("COMPONENT_CORE", "org.talend.designer.core.generic"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("DESIGNER_CORE", "org.talend.designer.core"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("HADOOP_DISTRIBUTIONS", "org.talend.hadoop.distribution"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("HADOOP_CUSTOM_DISTRIBUTIONS", "org.talend.hadoop.distribution.custom"); //$NON-NLS-1$ //$NON-NLS-2$

        jetBean.setClassLoader(new CodeGeneratorEmittersPoolFactory().getClass().getClassLoader());
        String sparkUtilsPluginName = "org.talend.designer.spark"; //$NON-NLS-1$
        if (PluginChecker.isPluginLoaded(sparkUtilsPluginName)) {
            jetBean.addClassPath("SPARK_LIBRARIES", sparkUtilsPluginName); //$NON-NLS-1$
            jetBean.setClassLoader(createDelegateClassLoader(jetBean.getClassLoader(), sparkUtilsPluginName,
                    "org.talend.designer.spark.SparkPlugin")); //$NON-NLS-1$
        }
        return jetBean;
    }

    /**
     * initialization of the available components.
     *
     * @param codeLanguage
     * @param jetBeans
     * @param codePart
     * @param component
     */
    private static void initComponent(ECodeLanguage codeLanguage, List<JetBean> jetBeans, ECodePart codePart,
            IComponent component) {

        if (component.getAvailableCodeParts().contains(codePart)) {
            IComponentFileNaming fileNamingInstance = ComponentsFactoryProvider.getFileNamingInstance();
            String templateURI = component.getPathSource() + TemplateUtil.DIR_SEP + component.getName() + TemplateUtil.DIR_SEP
                    + fileNamingInstance.getJetFileName(component, codeLanguage.getExtension(), codePart);
            String componentsPath = IComponentsFactory.COMPONENTS_LOCATION;
            IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault()
                    .getService(IBrandingService.class);
            if (breaningService.isPoweredOnlyCamel()) {
                componentsPath = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
            }
            if (component instanceof EmfComponent) {
                componentsPath = ((EmfComponent) component).getSourceBundleName();
            }
            JetBean jetBean = new JetBean(componentsPath, templateURI, component.getName(), component.getVersion(),
                    codeLanguage.getName(), codePart.getName());
            addCommonLibs(jetBean);
            jetBean.addClassPath("COMPONENT_FRAMEWORK", "org.talend.components.api"); //$NON-NLS-1$ //$NON-NLS-2$
            jetBean.addClassPath("DAIKON", "org.talend.daikon"); //$NON-NLS-1$ //$NON-NLS-2$
            jetBean.addClassPath("COMPONENT_CORE", "org.talend.designer.core.generic"); //$NON-NLS-1$ //$NON-NLS-2$
            jetBean.addClassPath("DESIGNER_CORE", "org.talend.designer.core"); //$NON-NLS-1$ //$NON-NLS-2$
            jetBean.addClassPath("HADOOP_DISTRIBUTIONS", "org.talend.hadoop.distribution"); //$NON-NLS-1$ //$NON-NLS-2$
            jetBean.addClassPath("HADOOP_CUSTOM_DISTRIBUTIONS", "org.talend.hadoop.distribution.custom"); //$NON-NLS-1$ //$NON-NLS-2$

            if (PluginChecker.isGEFAbstractMapLoaded()) {
                jetBean.addClassPath("GEF_MAP", "org.talend.designer.gefabstractmap"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            for (String pluginDependency : component.getPluginDependencies()) {
                jetBean.addClassPath(pluginDependency.toUpperCase().replaceAll("\\.", "_") + "_LIBRARIES", pluginDependency); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            String familyName = component.getOriginalFamilyName();
            if (familyName.contains("|")) { //$NON-NLS-1$
                familyName = component.getOriginalFamilyName().substring(0, component.getOriginalFamilyName().indexOf("|")); //$NON-NLS-1$
            }
            jetBean.setFamily(StringUtils.removeSpecialCharsForPackage(familyName.toLowerCase()));

            // Spark, M/R and Storm requires the plugin org.talend.designer.spark to be in the classpath in order to
            // generate the code.
            String sparkUtilsPluginName = "org.talend.designer.spark"; //$NON-NLS-1$
            String bigDataUtilsPluginName = "org.talend.designer.bigdata"; //$NON-NLS-1$
            if (PluginChecker.isPluginLoaded(sparkUtilsPluginName) && ("SPARK".equals(component.getPaletteType()) //$NON-NLS-1$
                    || "MR".equals(component.getPaletteType()) || "STORM".equals(component.getPaletteType()) //$NON-NLS-1$ //$NON-NLS-2$
                    || "SPARKSTREAMING".equals(component.getPaletteType()))) { //$NON-NLS-1$
                jetBean.addClassPath("BIGDATA_LIBRARIES", bigDataUtilsPluginName); //$NON-NLS-1$
                jetBean.addClassPath("SPARK_LIBRARIES", sparkUtilsPluginName); //$NON-NLS-1$
                jetBean.setClassLoader(createDelegateClassLoader(
                        createDelegateClassLoader(new CodeGeneratorEmittersPoolFactory().getClass().getClassLoader(),
                                bigDataUtilsPluginName, "org.talend.designer.bigdata.common.BigDataDataProcess"), //$NON-NLS-1$
                        sparkUtilsPluginName, "org.talend.designer.spark.SparkPlugin")); //$NON-NLS-1$

                // If Spark AND with an external component, use the external component as the parent classloader and
                // spark as a secondary, delegate classloader.
                if (component.getPluginExtension() != null) {
                    jetBean.addClassPath(
                            "EXTERNAL_COMPONENT_" + component.getPluginExtension().toUpperCase().replaceAll("\\.", "_"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            component.getPluginExtension());
                    jetBean.setClassLoader(new DelegateClassLoader(
                            ExternalNodesFactory.getInstance(component.getPluginExtension()).getClass().getClassLoader(),
                            jetBean.getClassLoader()));
                }

            } else if (component.getPluginExtension() != null) {

                jetBean.addClassPath("EXTERNAL_COMPONENT_" + component.getPluginExtension().toUpperCase().replaceAll("\\.", "_"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        component.getPluginExtension());
                jetBean.setClassLoader(
                        ExternalNodesFactory.getInstance(component.getPluginExtension()).getClass().getClassLoader());
            } else {
                jetBean.setClassLoader(new CodeGeneratorEmittersPoolFactory().getClass().getClassLoader());
            }

            jetBeans.add(jetBean);
        }
        // When building the main part of the component, also attempt to build the other parts.
        if (codePart.compareTo(ECodePart.MAIN) == 0) {
            for (ECodePart otherPart : ECodePart.values()) {
                if (otherPart != ECodePart.MAIN && component.getAvailableCodeParts().contains(otherPart)) {
                    initComponent(codeLanguage, jetBeans, otherPart, component);
                }
            }
        }
    }

    /**
     *
     * Initialization of the generic components.
     *
     * @param codeLanguage
     * @param jetBeans
     * @param codePart
     * @param component
     */
    private static void initGenericComponent(ECodeLanguage codeLanguage, List<JetBean> jetBeans, ECodePart codePart,
            IComponent component, Set<String> templateUris) {
        if (component.getAvailableCodeParts().contains(codePart)) {
            String templateURI = component.getTemplateFolder() + TemplateUtil.DIR_SEP + component.getTemplateNamePrefix() + "_"
                    + codePart.getName() + TemplateUtil.EXT_SEP + codeLanguage.getExtension() + TemplateUtil.TEMPLATE_EXT;
            if (!templateUris.add(templateURI)) {
                return;
            }
            String componentsPath = "org.talend.designer.codegen";//$NON-NLS-1$
            // TODO
            JetBean jetBean = new JetBean(componentsPath, templateURI, "component", component.getVersion(), //$NON-NLS-1$
                    "", codePart.getName());//$NON-NLS-1$
            addCommonLibs(jetBean);
            jetBean.addClassPath("AVRO", "org.apache.servicemix.bundles.avro"); //$NON-NLS-1$ //$NON-NLS-2$

            for (String pluginDependency : component.getPluginDependencies()) {
                jetBean.addClassPath(pluginDependency.toUpperCase().replaceAll("\\.", "_") + "_LIBRARIES", pluginDependency); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            String familyName = component.getOriginalFamilyName();
            // TODO
            familyName = "generic/component";//$NON-NLS-1$
            if (familyName.contains("|")) { //$NON-NLS-1$
                familyName = component.getOriginalFamilyName().substring(0, component.getOriginalFamilyName().indexOf("|")); //$NON-NLS-1$
            }
            jetBean.setFamily(StringUtils.removeSpecialCharsForPackage(familyName.toLowerCase()));

            if (component.getPluginExtension() != null) {
                jetBean.addClassPath("EXTERNAL_COMPONENT_" + component.getPluginExtension().toUpperCase().replaceAll("\\.", "_"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        component.getPluginExtension());
                jetBean.setClassLoader(
                        ExternalNodesFactory.getInstance(component.getPluginExtension()).getClass().getClassLoader());
            } else {
                jetBean.setClassLoader(new CodeGeneratorEmittersPoolFactory().getClass().getClassLoader());
            }
            jetBeans.add(jetBean);
        }
    }

    /**
     * real pool initialization.
     *
     * @param monitorWrap
     *
     * @return
     * @throws JETException
     */
    private static void initializeEmittersPool(List<JetBean> components, ECodeLanguage codeLanguage,
            IProgressMonitor monitorWrap) {
        IProgressMonitor monitor = new NullProgressMonitor();
        IProgressMonitor sub = new SubProgressMonitor(monitor, 1);
        int monitorBuffer = 0;

        HashMap<String, String> globalClasspath = new HashMap<String, String>();
        for (JetBean jetBean : components) {
            globalClasspath.putAll(jetBean.getClassPath());
            // compute the CRC
            jetBean.setCrc(extractTemplateHashCode(jetBean));
        }

        emitterPool = new HashMap<JetBean, JETEmitter>();
        List<JetBean> alreadyCompiledEmitters = new ArrayList<JetBean>();

        // try {
        TalendJetEmitter dummyEmitter = null;
        try {
            dummyEmitter = new TalendJetEmitter(null, null, sub, globalClasspath, !ComponentCompilations.getMarkers());
        } catch (JETException e) {
            log.error(Messages.getString("CodeGeneratorEmittersPoolFactory.jetEmitterInitialException") + e.getMessage(), e); //$NON-NLS-1$
        }

        boolean isSkeletonChanged = JetSkeletonManager.updateSkeletonPersistenceData();
        // if there is one skeleton changed, there need generate all jet--->java again. so, it won't load the
        // JetPersistenceJAVA

        if (!isSkeletonChanged) {
            try {
                alreadyCompiledEmitters = loadEmfPersistentData(
                        EmfEmittersPersistenceFactory.getInstance(codeLanguage).loadEmittersPool(), components, monitorWrap);
                for (JetBean jetBean : alreadyCompiledEmitters) {
                    TalendJetEmitter emitter = new TalendJetEmitter(jetBean, dummyEmitter.getTalendEclipseHelper());
                    emitterPool.put(jetBean, emitter);
                    monitorBuffer++;
                    if (monitorBuffer % 100 == 0) {
                        monitorWrap.worked(100);
                        monitorBuffer = 0;
                    }
                }
            } catch (BusinessException e) {
                // error already loggued
                emitterPool = new HashMap<JetBean, JETEmitter>();
            }
        } else {
            ComponentCompilations.deleteMarkers();
        }

        synchronizedComponent(components, sub, alreadyCompiledEmitters, dummyEmitter, monitorBuffer, monitorWrap);

        monitorWrap.worked(monitorBuffer);
        try {
            EmfEmittersPersistenceFactory.getInstance(codeLanguage)
                    .saveEmittersPool(extractEmfPersistenData(alreadyCompiledEmitters));
        } catch (BusinessException e) {
            log.error(Messages.getString("CodeGeneratorEmittersPoolFactory.PersitentData.Error") + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    private static void synchronizedComponent(List<JetBean> components, IProgressMonitor sub,
            List<JetBean> alreadyCompiledEmitters, TalendJetEmitter dummyEmitter, int monitorBuffer,
            IProgressMonitor monitorWrap) {
        for (JetBean jetBean : components) {
            if (!emitterPool.containsKey(jetBean)) {
                ComponentCompilations.deleteMarkers();

                // System.out.println("The new file is not in JetPersistence* cache:" + getFullTemplatePath(jetBean));
                TalendJetEmitter emitter = new TalendJetEmitter(jetBean, dummyEmitter.getTalendEclipseHelper());
                // wzhang modified to fix bug 11439
                if (monitorWrap.isCanceled()) {
                    if (!CommonUIPlugin.isFullyHeadless()) {
                        Display.getDefault().syncExec(new Runnable() {

                            @Override
                            public void run() {
                                MessageDialog.openError(Display.getDefault().getActiveShell(),
                                        Messages.getString("CodeGeneratorEmittersPoolFactory.operationCanceled"), //$NON-NLS-1$
                                        Messages.getString("CodeGeneratorEmittersPoolFactory.dialogContent")); //$NON-NLS-1$

                            }
                        });
                    }
                    return;
                }
                if (Boolean.getBoolean("force_full_codegen")) { //$NON-NLS-1$
                    try {
                        emitter.initialize(sub);
                    } catch (JETException e) {
                        log.error(
                                Messages.getString("CodeGeneratorEmittersPoolFactory.jetEmitterInitialException") //$NON-NLS-1$
                                        + e.getMessage(), e);
                        continue;
                    }
                }

                if (emitter.isClassAvailable()) {
                    alreadyCompiledEmitters.add(jetBean);
                } else {
                    jetFilesCompileFail.add(jetBean);
                }
                emitterPool.put(jetBean, emitter);
                monitorBuffer++;
                if (monitorBuffer % 100 == 0) {
                    monitorWrap.worked(100);
                    monitorBuffer = 0;
                }
            }
        }
    }

    /**
     * DOC mhirt Comment method "extractEmfPersistenData".
     *
     * @param alreadyCompiledEmitters
     * @return
     */
    private static List<LightJetBean> extractEmfPersistenData(List<JetBean> alreadyCompiledEmitters) {
        List<LightJetBean> toReturn = new ArrayList<LightJetBean>();
        for (JetBean unit : alreadyCompiledEmitters) {
            // long unitCRC = extractTemplateHashCode(unit);
            long unitCRC = unit.getCrc();
            toReturn.add(new LightJetBean(unit.getTemplateRelativeUri(), unit.getClassName(), unit.getMethodName(),
                    unit.getVersion(), unit.getLanguage(), unitCRC));
        }
        return toReturn;
    }

    /**
     * DOC mhirt Comment method "extractTemplateHashCode".
     *
     * @param unit
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private static long extractTemplateHashCode(JetBean unit) {
        long unitCRC = 0;

        URI uri = URI.createURI(unit.getTemplateFullUri());
        uri = CommonPlugin.resolve(uri);
        URL url;
        try {
            url = new URL(uri.toString());
            unitCRC = IOUtils.computeCRC(url.openStream());
        } catch (Exception e) {
            // ignore me even if i'm null
        }

        return unitCRC;
    }

    private static List<JetBean> loadEmfPersistentData(List<LightJetBean> datas, List<JetBean> completeJetBeanList,
            IProgressMonitor monitorWrap) throws BusinessException {
        List<JetBean> toReturn = new ArrayList<JetBean>();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = workspace.getRoot().getProject(".JETEmitters"); //$NON-NLS-1$
        URL url = null;
        try {
            IFile file = project.getFile(Path.fromOSString("runtime"));
            url = file.getLocationURI().toURL();
            int lightBeanIndex = 0;
            LightJetBean lightBean = null;
            LightJetBean myLightJetBean = null;
            String unitTemplateFullURI = ""; //$NON-NLS-1$
            long unitTemplateHashCode = 0;

            HashMap<String, LightJetBean> mapOnName = new HashMap<String, LightJetBean>();
            boolean forceMethodLoad = ComponentCompilations.getMarkers();
            if (forceMethodLoad) {
                // init specific map based on component name : mapOnName
                for (LightJetBean ljb : datas) {
                    mapOnName.put(ljb.getTemplateRelativeUri().substring(ljb.getTemplateRelativeUri().lastIndexOf("/")), ljb); //$NON-NLS-1$
                }
            }
            int monitorBuffer = 0;
            for (JetBean unit : completeJetBeanList) {
                monitorBuffer++;
                if (monitorBuffer % 200 == 0) {
                    monitorWrap.worked(200);
                    monitorBuffer = 0;
                }
                unitTemplateFullURI = unit.getTemplateRelativeUri();
                unitTemplateHashCode = unit.getCrc();

                myLightJetBean = new LightJetBean(unitTemplateFullURI, unit.getVersion(), unitTemplateHashCode);
                if (((lightBeanIndex = datas.indexOf(myLightJetBean)) > -1) || forceMethodLoad) {
                    if (!forceMethodLoad) {
                        lightBean = datas.get(lightBeanIndex);
                    } else {
                        lightBean = mapOnName.get(myLightJetBean.getTemplateRelativeUri()
                                .substring(myLightJetBean.getTemplateRelativeUri().lastIndexOf("/"))); //$NON-NLS-1$
                    }
                    if (lightBean != null && lightBean.getCrc() == unit.getCrc()) {
                        unit.setClassName(lightBean.getClassName());
                        unit.setMethodName(lightBean.getMethodName());
                        toReturn.add(unit);
                    }
                }
            }
            monitorWrap.worked(monitorBuffer);
        } catch (MalformedURLException e) {
            log.error(Messages.getString("CodeGeneratorEmittersPoolFactory.JETEmitters.NoPresent")); //$NON-NLS-1$
            throw new BusinessException(e);
        }
        return toReturn;
    }

    /**
     * Getter for emitterPool.
     *
     * @return the emitterPool
     */
    public static HashMap<JetBean, JETEmitter> getEmitterPool() {
        if (emitterPool == null || (!isInitialized() && !isInitializeStart())) {
            initialize();
        }
        return emitterPool;
    }

    /**
     * DOC xtan Comment method "getJETEmitter".
     *
     * @param jetBean
     * @return
     */
    public static JETEmitter getJETEmitter(JetBean jetBean) {
        if (emitterPool == null || (!isInitialized() && !isInitializeStart())) {
            try {
                new CodeGeneratorManager().initTemplate();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // only for components, not for /resources jet file, if it compile error, it will get the
        // default_template.javajet
        if (jetBean.getTemplateRelativeUri() != null && !jetBean.getTemplateRelativeUri().startsWith("resources")) { //$NON-NLS-1$
            if (jetFilesCompileFail.contains(jetBean)) {
                JetBean defaultJetBean = new JetBean();
                defaultJetBean.setTemplateRelativeUri(defaultTemplate);
                return emitterPool.get(defaultJetBean);
            }
        }
        return emitterPool.get(jetBean);
    }

    /**
     * Getter for initialized.
     *
     * @return the initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the initialized.
     *
     * @param initialized the initialized to set
     */
    public static void setInitialized(boolean initialized) {
        CodeGeneratorEmittersPoolFactory.initialized = initialized;
    }

    public static void addCommonLibs(JetBean jetBean) {
        jetBean.addClassPath("EMF_ECORE", "org.eclipse.emf.ecore"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("EMF_COMMON", "org.eclipse.emf.common"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("CORERUNTIME_LIBRARIES", "org.talend.core.runtime"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("MANAGEMENT_LIBRARIES", "org.talend.metadata.managment"); //$NON-NLS-1$ //$NON-NLS-2$
        jetBean.addClassPath("CORE_LIBRARIES", CorePlugin.PLUGIN_ID); //$NON-NLS-1$
        jetBean.addClassPath("CODEGEN_LIBRARIES", CodeGeneratorActivator.PLUGIN_ID); //$NON-NLS-1$
        jetBean.addClassPath("COMMON_LIBRARIES", CommonsPlugin.PLUGIN_ID); //$NON-NLS-1$
        jetBean.addClassPath("COMMON_LIBRARIES_STUDIO_UTILS", "org.talend.studio.studio-utils"); //$NON-NLS-1$
    }

    /***/
    private static class DelegateProgressMonitor implements IProgressMonitor {

        private List<IProgressMonitor> delegates = new ArrayList<IProgressMonitor>();

        private boolean cancelled;

        public void addDelegate(IProgressMonitor progressMonitor) {
            delegates.add(progressMonitor);
        }

        public void clearDelegate() {
            delegates.clear();
        }

        @Override
        public void beginTask(String name, int totalWork) {
            for (IProgressMonitor delegate : delegates) {
                delegate.beginTask(name, totalWork);
            }
        }

        @Override
        public void done() {
            for (IProgressMonitor delegate : delegates) {
                delegate.done();
            }
        }

        @Override
        public void internalWorked(double work) {
            for (IProgressMonitor delegate : delegates) {
                delegate.internalWorked(work);
            }
        }

        @Override
        public boolean isCanceled() {
            for (IProgressMonitor monitor : delegates) {
                if (monitor.isCanceled()) {
                    return true;
                }
            }
            return cancelled;
        }

        @Override
        public void setCanceled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public void setTaskName(String name) {
            for (IProgressMonitor delegate : delegates) {
                delegate.setTaskName(name);
            }
        }

        @Override
        public void subTask(String name) {
            for (IProgressMonitor delegate : delegates) {
                delegate.subTask(name);
            }
        }

        @Override
        public void worked(int work) {
            for (IProgressMonitor delegate : delegates) {
                delegate.worked(work);
            }
        }

    }

    public static boolean isInitializeStart() {
        return initializeStart;
    }

    static class CodeGeneratorManager {

        private IStatus status;

        public IStatus initTemplate() throws InterruptedException {
            final Job initializeTemplatesJob = initialize();
            Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    if (event.getJob().equals(initializeTemplatesJob)) {
                        setStatus(event.getResult());
                    }
                }
            });

            while (status == null) {
                Thread.sleep(10);
            }

            return status;
        }

        private void setStatus(IStatus result) {
            this.status = result;
        }

        private ICodeGeneratorService getCodeGenerationService() {
            IService service = GlobalServiceRegister.getDefault().getService(ICodeGeneratorService.class);
            return (ICodeGeneratorService) service;
        }
    }

    /**
     * A simple classloader that permits a "delegate" to be added to a "parent" classloader. With the model used by the
     * Java ClassLoader, this permits classes that are in the delegate to be instantiated, and are able to use objects
     * created from the parent ClassLoader.
     *
     * Objects in the parent ClassLoader must not use objects instantiated from the delegate.
     */
    public static class DelegateClassLoader extends ClassLoader {

        private ClassLoader delegate;

        public DelegateClassLoader(ClassLoader parent, ClassLoader delegate) {
            super(parent);
            this.delegate = delegate;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }

        @Override
        protected URL findResource(String name) {
            return delegate.getResource(name);
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            return delegate.getResources(name);
        }

    }
}
