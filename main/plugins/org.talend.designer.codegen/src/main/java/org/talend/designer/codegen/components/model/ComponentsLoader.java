// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.component_cache.ComponentInfo;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.filters.ComponentsFactoryProviderManager;
import org.talend.core.model.components.filters.IComponentFactoryFilter;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.core.model.components.ComponentBundleToPath;
import org.talend.designer.core.model.components.ComponentFilesNaming;
import org.talend.designer.core.model.components.EmfComponent;

/*
 * Created by bhe on Dec 25, 2020
 */
public class ComponentsLoader {

    private static final Logger LOGGER = Logger.getLogger(ComponentsLoader.class);

    private static final ComponentsLoader INSTANCE = new ComponentsLoader();

    private ComponentsLoader() {
    }

    public static ComponentsLoader getInstance() {
        return INSTANCE;
    }

    public void loadComponents(List<AbstractComponentsProvider> providers, String installLocation) {

        ComponentsMemoryCacheMgr.getInstance().loadComponentsFromMemoryCache(providers);

        // this is a disk heavy task
        int nThreads = Runtime.getRuntime().availableProcessors() * 2;
        nThreads = Math.max(nThreads, 8);
        nThreads = Math.min(nThreads, 12);

        ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(nThreads);
        CompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(WORKER_THREAD_POOL);

        ConcurrentLinkedQueue<TaskInput> taskInputs = new ConcurrentLinkedQueue<TaskInput>();
        for (AbstractComponentsProvider provider : providers) {
            List<TaskInput> tis = getTaskInputs(provider);
            taskInputs.addAll(tis);
        }

        for (int i = 0; i < nThreads; i++) {
            LoadComponentTask task = new LoadComponentTask(taskInputs);
            service.submit(task);
        }

        // wait for all of tasks completed
        for (int i = 0; i < nThreads; i++) {
            try {
                service.take();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }

        // initiate shutdown
        WORKER_THREAD_POOL.shutdown();
        // save cache
        ComponentsMemoryCacheMgr.getInstance().persist();
    }

    private List<TaskInput> getTaskInputs(AbstractComponentsProvider provider) {
        List<TaskInput> ret = new ArrayList<TaskInput>();
        if (provider != null) {
            try {
                provider.preComponentsLoad();
                File componentFile = provider.getInstallationFolder();
                if (componentFile != null && componentFile.exists()) {
                    File source = provider.getInstallationFolder();
                    FileFilter fileFilter = new FileFilter() {

                        @Override
                        public boolean accept(final File file) {
                            return file.isDirectory() && file.getName().charAt(0) != '.'
                                    && !file.getName().equals(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER);
                        }

                    };

                    File[] childDirectories = source.listFiles(fileFilter);

                    for (File f : childDirectories) {
                        TaskInput ti = new TaskInput(provider, f, provider.getComponentsLocation());
                        ret.add(ti);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return ret;
    }

    public static void createEmfComponent(String name, ComponentInfo ci, AbstractComponentsProvider provider) {

        // check cache
        if (ComponentsMemoryCacheMgr.getInstance().containComponent(name, ci)) {
            return;
        }

        EmfComponent currentComp = null;
        try {
            currentComp = new EmfComponent(ci.getUriString(), ci.getSourceBundleName(), name, ci.getPathSource(), ci.getOwner(),
                    false, provider);
            ComponentsMemoryCacheMgr.getInstance().update(name, currentComp.getComponentInfo());
        } catch (BusinessException e) {
            ExceptionHandler.process(e);
            return;
        }

        boolean hiddenComponent = false;

        Collection<IComponentFactoryFilter> filters = ComponentsFactoryProviderManager.getInstance().getProviders();
        for (IComponentFactoryFilter filter : filters) {
            if (!filter.isAvailable(currentComp.getName())) {
                hiddenComponent = true;
                break;
            }
        }

        // if the component is not needed in the current branding,
        // and that this one IS NOT a specific component for code generation
        // just don't load it
        if (hiddenComponent && !(currentComp.getOriginalFamilyName().contains("Technical") || currentComp.isTechnical())) {
            return;
        }

        ComponentsMemoryCacheMgr.getInstance().putComponentsProvider(currentComp, provider);
        // if the component is not needed in the current branding,
        // and that this one IS a specific component for code generation,
        // hide it
        if (hiddenComponent && (currentComp.getOriginalFamilyName().contains("Technical") || currentComp.isTechnical())) {
            currentComp.setVisible(false);
            currentComp.setTechnical(true);
        }
        if (provider.getId().contains("Camel")) {
            currentComp.setPaletteType(ComponentCategory.CATEGORY_4_CAMEL.getName());
        } else {
            currentComp.setPaletteType(currentComp.getType());
        }

        currentComp.setProvider(provider);

        ComponentsMemoryCacheMgr.getInstance().addComponent(currentComp);
    }

    public static void createEmfComponent(int owner, String name, String pathName, String bundleName, String pathSource,
            String sha1, AbstractComponentsProvider provider) {
        ComponentInfo ci = EmfComponent.newComponentInfo();
        ci.setOwner(owner);
        ci.setPathSource(pathSource);
        ci.setSourceBundleName(bundleName);
        ci.setProviderClass(provider.getClass().getCanonicalName());
        ci.setUriString(pathName);
        ci.setSha1(sha1);

        createEmfComponent(name, ci, provider);
    }

    private static class TaskInput {

        AbstractComponentsProvider componentsProvider;

        File dir;

        String pathSource;

        public TaskInput(AbstractComponentsProvider componentsProvider, File dir, String pathSource) {
            this.componentsProvider = componentsProvider;
            this.dir = dir;
            this.pathSource = pathSource;
        }
    }

    private static class LoadComponentTask implements Callable<Boolean> {

        private static Logger LOGGER = Logger.getLogger(LoadComponentTask.class);

        private static final String SKELETON_SUFFIX = ".skeleton"; //$NON-NLS-1$

        private static final String INCLUDEFILEINJET_SUFFIX = ".inc.javajet"; //$NON-NLS-1$

        private static final String OLD_COMPONENTS_USER_INNER_FOLDER = "user";

        ConcurrentLinkedQueue<TaskInput> tis;

        public LoadComponentTask(ConcurrentLinkedQueue<TaskInput> tis) {
            this.tis = tis;
        }

        @Override
        public Boolean call() throws Exception {
            TaskInput ti = null;
            while ((ti = this.tis.poll()) != null) {
                loadComponentsFromFolder(ti);
            }
            return true;

        }

        private void loadComponentsFromFolder(TaskInput ti) {
            if (ti != null) {
                boolean isCustom = ti.componentsProvider.isCustom();

                FileFilter skeletonFilter = new FileFilter() {

                    @Override
                    public boolean accept(final File file) {
                        String fileName = file.getName();
                        return file.isFile() && fileName.charAt(0) != '.'
                                && (fileName.endsWith(SKELETON_SUFFIX) || fileName.endsWith(INCLUDEFILEINJET_SUFFIX));
                    }

                };
                // Changed by Marvin Wang on Feb.22 for bug TDI-19166, caz the test ConnectionManagerTest maybe get the
                // null
                // context.
                BundleContext context = null;
                if (Platform.getProduct() != null) {
                    final Bundle definingBundle = Platform.getProduct().getDefiningBundle();
                    if (definingBundle != null) {
                        context = definingBundle.getBundleContext();
                    }
                }
                if (context == null) {
                    context = CodeGeneratorActivator.getDefault().getBundle().getBundleContext();
                }

                ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
                PackageAdmin admin = (PackageAdmin) context.getService(sref);

                String bundleName;
                if (!isCustom) {
                    bundleName = admin.getBundle(ti.componentsProvider.getClass()).getSymbolicName();
                } else if (ti.componentsProvider.getComponentsBundle() == null) {
                    bundleName = IComponentsFactory.COMPONENTS_LOCATION;
                } else {
                    bundleName = ti.componentsProvider.getComponentsBundle();
                }
                if (ti.dir != null) {
                    // get the skeleton files first, then XML config files later.
                    File[] skeletonFiles = ti.dir.listFiles(skeletonFilter);
                    if (skeletonFiles != null) {
                        for (File file : skeletonFiles) {
                            ComponentsMemoryCacheMgr.getInstance().addSkeleton(file.getAbsolutePath());
                        }
                    }

                    try {
                        File xmlMainFile = new File(ti.dir,
                                ComponentFilesNaming.getInstance().getMainXMLFileName(ti.dir.getName(), getCodeLanguageSuffix()));
                        if (!xmlMainFile.exists()) {
                            // if not a component folder, ignore it.
                            return;
                        }

                        String name = xmlMainFile.getParentFile().getName();

                        String currentXmlSha1 = null;
                        try (FileInputStream fis = new FileInputStream(xmlMainFile)) {
                            currentXmlSha1 = DigestUtils.shaHex(fis);
                        } catch (Exception e) {
                            // nothing since exceptions are directly in the check bellow
                        }

                        ComponentFileChecker.checkComponentFolder(ti.dir, getCodeLanguageSuffix());
                        String pathName = xmlMainFile.getAbsolutePath();

                        String applicationPath = ComponentBundleToPath.getPathFromBundle(bundleName);

                        // pathName = C:\myapp\plugins\myplugin\components\mycomponent\mycomponent.xml
                        pathName = (new Path(pathName)).toPortableString();
                        // pathName = C:/myapp/plugins/myplugin/components/mycomponent/mycomponent.xml
                        pathName = pathName.replace(applicationPath, ""); //$NON-NLS-1$
                        // pathName = /components/mycomponent/mycomponent.xml

                        int owner = EmfComponent.OWNER_DEFAULT;
                        if (isCustom) {
                            owner |= EmfComponent.OWNER_CUSTOM;
                        }

                        if (ti.pathSource != null) {
                            Path userComponent = new Path(ti.pathSource);
                            Path templatePath = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER + File.separatorChar
                                    + IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER + File.separatorChar
                                    + ComponentUtilities.getExtFolder(LoadComponentTask.OLD_COMPONENTS_USER_INNER_FOLDER));
                            if (userComponent.equals(templatePath)) {
                                owner |= EmfComponent.OWNER_USER;
                            }
                        }

                        createEmfComponent(owner, name, pathName, bundleName, ti.pathSource, currentXmlSha1,
                                ti.componentsProvider);

                    } catch (MissingMainXMLComponentFileException e) {
                        LOGGER.trace(ti.dir.getName() + " is not a " + getCodeLanguageSuffix() + " component", e); //$NON-NLS-1$ //$NON-NLS-2$
                    } catch (BusinessException e) {
                        BusinessException ex = new BusinessException("Cannot load component \"" + ti.dir.getName() + "\": " //$NON-NLS-1$ //$NON-NLS-2$
                                + e.getMessage(), e);
                        ExceptionHandler.process(ex, Level.ERROR);
                    }
                }
            }
        }

        private String getCodeLanguageSuffix() {
            return LanguageManager.getCurrentLanguage().getName();
        }

    }

}
