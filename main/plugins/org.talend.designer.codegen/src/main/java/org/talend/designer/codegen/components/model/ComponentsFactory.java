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
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentManager;
import org.talend.core.model.components.ComponentProviderInfo;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsHandler;
import org.talend.core.model.components.filters.ComponentsFactoryProviderManager;
import org.talend.core.model.components.filters.IComponentFactoryFilter;
import org.talend.core.runtime.util.ComponentsLocationProvider;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ISparkJobletProviderService;
import org.talend.core.ui.ISparkStreamingJobletProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.core.model.components.ComponentBundleToPath;
import org.talend.designer.core.model.components.ComponentFilesNaming;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.process.GenericProcessProvider;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;

/**
 * Component factory that look for each component and load their information. <br/>
 *
 * $Id: ComponentsFactory.java 52892 2010-12-20 05:52:17Z nrousseau $
 */
public class ComponentsFactory implements IComponentsFactory {

    public static final String OLD_COMPONENTS_USER_INNER_FOLDER = "user"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(ComponentsFactory.class);

    private List<AbstractComponentsProvider> providers;

    private Set<IComponent> componentList = Collections.synchronizedSet(new HashSet<>());

    private static Set<IComponent> customComponentList = new HashSet<>(); // user/exchange/tck components

    private IProgressMonitor monitor;

    private SubMonitor subMonitor;

    // 1. only the in the directory /components ,not including /resource
    // 2. include the skeleton files and external include files
    private static ArrayList<String> skeletonList = new ArrayList<>();

    private static final String SKELETON_SUFFIX = ".skeleton"; //$NON-NLS-1$

    private static final String INCLUDEFILEINJET_SUFFIX = ".inc.javajet"; //$NON-NLS-1$

    private IComponentsHandler componentsHandler;// Added by Marvin Wang on Jan. 11, 2012 for M/R.

    protected static Map<String, Map<String, Set<IComponent>>> componentNameMap;

    private AtomicBoolean isInitializing = new AtomicBoolean(true);

    private AtomicBoolean isInitialized = new AtomicBoolean(false);

    public ComponentsFactory() {
        providers = ComponentsProviderManager.getInstance().getProviders();
    }

    private void init(boolean duringLogon) {
        if (isInitialized.compareAndSet(false, true)) {
            log.debug("init " + this.hashCode());
            try {
                removeOldComponentsUserFolder();
            } catch (IOException ex) {
                ExceptionHandler.process(ex);
            } // not used anymore

            long startTime = System.currentTimeMillis();

            // TimeMeasure.display = true;
            // TimeMeasure.displaySteps = true;
            // TimeMeasure.measureActive = true;
            // TimeMeasure.begin("initComponents");
            componentList.clear();
            skeletonList.clear();
            customComponentList.clear();

            boolean needRegenerate = false;
            if (CodeGeneratorActivator.getDefault().getBundle().getBundleContext().getProperty("osgi.dev") != null) {
                needRegenerate = providers.stream().filter(p -> ComponentsLoader.isCachedProvider(p)).anyMatch(p -> {
                    try {
                        return !ComponentManager.hasComponentFile(p.getInstallationFolder().getAbsolutePath());
                    } catch (IOException e) {
                        return false;
                    }
                });
            }
            if (Boolean.getBoolean("generate.component.cache") || needRegenerate) {
                ComponentManager.clearAllCaches();
                providers.stream().filter(p -> ComponentsLoader.isCachedProvider(p)).forEach(p -> loadComponents(p));
                ComponentManager.saveResource();
            }

            try {
                for (AbstractComponentsProvider provider : providers) {
                    provider.preComponentsLoad();
                }
                ComponentManager.loadComponentResource();
                ComponentsLoader.getInstance().loadAllComponentsFromIndex(componentList);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

            providers.stream().filter(p -> p.isCustom()).forEach(p -> loadCustomComponents(p));

            loadComponentsFromExtensions();

            loadSkeletonFromProviders();

            // init component name map, used to pick specified component immediately
            initComponentNameMap();

            isInitializing.set(false);
            // TimeMeasure.step("initComponents", "createCache");
            log.info(componentList.size() + " components loaded in " + (System.currentTimeMillis() - startTime) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

            // TimeMeasure.end("initComponents");
            // TimeMeasure.display = false;
            // TimeMeasure.displaySteps = false;
            // TimeMeasure.measureActive = false;
        }
    }

    private void loadSkeletonFromProviders() {
        providers.forEach(p -> {
            try {
                File componentFile = p.getInstallationFolder();
                if (componentFile != null && componentFile.exists()) {
                    File[] childDirectories = componentFile
                            .listFiles(file -> file.getName().charAt(0) != '.'
                                    && !file.getName().equals(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER)
                                    && file.isDirectory());
                    skeletonList.ensureCapacity(childDirectories.length);// to optimize the size of the array
                    for (File currentFolder : childDirectories) {
                        File[] skeletonFiles = currentFolder.listFiles(file -> file.getName().charAt(0) != '.'
                                && (file.getName().endsWith(SKELETON_SUFFIX)
                                        || file.getName().endsWith(INCLUDEFILEINJET_SUFFIX))
                                && file.isFile());
                        if (skeletonFiles != null) {
                            for (File file : skeletonFiles) {
                                skeletonList.add(file.getAbsolutePath()); // path
                            }
                        }
                    }
                    skeletonList.trimToSize();
                }
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        });
    }

    protected void initComponentNameMap() {
        /**
         * component names example: <br>
         * 1. xmlMapComponent <br>
         * 2. xmlmapComponent <br>
         * 3. xmlmapcomponent <br>
         * 4. xmlMapComponent (for DI) <br>
         * 5. xmlMapComponent (for BD) <br>
         */
        componentNameMap = new HashMap<>();
        Iterator<IComponent> componentIter = componentList.iterator();
        while (componentIter.hasNext()) {
            IComponent component = componentIter.next();
            String componentName = component.getName();
            if (StringUtils.isEmpty(componentName)) {
                continue;
            }
            String componentNameLowerCase = componentName.toLowerCase();
            Map<String, Set<IComponent>> map = componentNameMap.get(componentNameLowerCase);
            if (map == null) {
                map = new HashMap<String, Set<IComponent>>();
                Set<IComponent> componentSet = new HashSet<IComponent>();
                componentSet.add(component);
                map.put(componentName, componentSet);
                componentNameMap.put(componentNameLowerCase, map);
            } else {
                Set<IComponent> componentSet = map.get(componentName);
                if (componentSet == null) {
                    componentSet = new HashSet<IComponent>();
                    map.put(componentName, componentSet);
                }
                componentSet.add(component);
            }
        }
    }

    private void loadCustomComponents(AbstractComponentsProvider componentsProvider) {
        if (componentsProvider != null) {
            try {
                File componentFile = componentsProvider.getInstallationFolder();
                if (componentFile == null) {
                    log.warn(Messages.getString("ComponentsFactory.loadComponents.missingFolder", //$NON-NLS-1$
                            componentsProvider.getFolderName(), componentsProvider.getContributer()));
                    return;
                }
                if (componentFile != null && componentFile.exists()) {
                    loadCustomComponentsFromFolder(componentsProvider.getComponentsLocation(), componentsProvider);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void loadComponents(AbstractComponentsProvider componentsProvider) {
        if (componentsProvider != null) {
            try {
                File componentFile = componentsProvider.getInstallationFolder();
                if (componentFile == null) {
                    log.warn(Messages.getString("ComponentsFactory.loadComponents.missingFolder", //$NON-NLS-1$
                            componentsProvider.getFolderName(), componentsProvider.getContributer()));
                    return;
                }
                if (componentFile != null && componentFile.exists()) {
                    loadComponentsFromFolder(componentsProvider.getComponentsLocation(), componentsProvider);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    @Override
    public void loadUserComponentsFromComponentsProviderExtension() {
        getComponents();
        // remove old user components
        componentList.removeIf(comp -> isUserComponent(comp));
        customComponentList.removeIf(comp -> isUserComponent(comp));
        loadCustomComponents(ComponentsProviderManager.getInstance().loadUserComponentsProvidersFromExtension());
    }

    private boolean isUserComponent(IComponent component) {
        return component instanceof EmfComponent && ((EmfComponent) component).getProvider() instanceof UserComponentsProvider;
    }

    private void removeOldComponentsUserFolder() throws IOException {
        String userPath = IComponentsFactory.COMPONENTS_INNER_FOLDER + File.separatorChar
                + ComponentUtilities.getExtFolder(OLD_COMPONENTS_USER_INNER_FOLDER);
        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        AbstractComponentsProvider componentsProvider = componentsProviderManager.loadUserComponentsProvidersFromExtension();
        File componentsLocation = getComponentsLocation(componentsProvider, userPath);
        if (componentsLocation != null && componentsLocation.exists()) {
            FilesUtils.removeFolder(componentsLocation, true);
        }
    }

    /**
     * DOC qzhang Comment method "loadComponentsFromExtensions".
     */
    private void loadComponentsFromExtensions() {
        AbstractProcessProvider.loadComponentsFromProviders();
        GenericProcessProvider.getInstance().loadComponentsFromProviders();
    }

    private void loadCustomComponentsFromFolder(String pathSource, AbstractComponentsProvider provider) {
        String bundleName = provider.getComponentsBundle() != null ? provider.getComponentsBundle() : COMPONENTS_LOCATION;
        File source;
        try {
            source = provider.getInstallationFolder();
        } catch (IOException e) {
            ExceptionHandler.process(e);
            return;
        }
        if (source == null) {
            ExceptionHandler.process(new Exception(Messages.getString("ComponentsFactory.componentNotFound") + pathSource)); //$NON-NLS-1$
            return;
        }

        File[] childDirectories = source.listFiles(file -> file.isDirectory() && file.getName().charAt(0) != '.' // $NON-NLS-1$
                && !file.getName().equals(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER));

        if (childDirectories != null) {
            if (monitor != null) {
                this.subMonitor = SubMonitor.convert(monitor, Messages.getString("ComponentsFactory.load.components"), //$NON-NLS-1$
                        childDirectories.length);
            }
            for (File currentFolder : childDirectories) {
                try {
                    File xmlMainFile = new File(currentFolder, ComponentFilesNaming.getInstance()
                            .getMainXMLFileName(currentFolder.getName(), getCodeLanguageSuffix()));
                    if (!xmlMainFile.exists()) {
                        // if not a component folder, ignore it.
                        continue;
                    }
                    ComponentFileChecker.checkComponentFolder(currentFolder, getCodeLanguageSuffix());

                    String pathName = xmlMainFile.getAbsolutePath();

                    String applicationPath = ComponentBundleToPath.getPathFromBundle(bundleName);

                    // pathName = C:\myapp\plugins\myplugin\components\mycomponent\mycomponent.xml
                    pathName = (new Path(pathName)).toPortableString();
                    // pathName = C:/myapp/plugins/myplugin/components/mycomponent/mycomponent.xml
                    pathName = pathName.replace(applicationPath, ""); //$NON-NLS-1$
                    // pathName = /components/mycomponent/mycomponent.xml

                    EmfComponent currentComp = new EmfComponent(pathName, bundleName, xmlMainFile.getParentFile().getName(),
                            pathSource, null, false, provider);

                    if (ComponentsLoader.skipLoadComponent(currentComp)) {
                        continue;
                    }

                    ComponentsLoader.setComponentPaletteType(currentComp);

                    if (componentList.contains(currentComp)) {
                        log.warn("Component " + currentComp.getName() + " already exists. Cannot load user version."); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        componentList.add(currentComp);
                        customComponentList.add(currentComp);
                    }
                } catch (MissingMainXMLComponentFileException e) {
                    log.trace(currentFolder.getName() + " is not a " + getCodeLanguageSuffix() + " component", e); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (BusinessException e) {
                    BusinessException ex = new BusinessException("Cannot load component \"" + currentFolder.getName() + "\": " //$NON-NLS-1$ //$NON-NLS-2$
                            + e.getMessage(), e);
                    ExceptionHandler.process(ex, Level.ERROR);
                }

                if (this.subMonitor != null) {
                    this.subMonitor.worked(1);
                }
                if (this.monitor != null && this.monitor.isCanceled()) {
                    return;
                }
            }
        }
    }

    private void loadComponentsFromFolder(String pathSource, AbstractComponentsProvider provider) {
        // Changed by Marvin Wang on Feb.22 for bug TDI-19166, caz the test ConnectionManagerTest maybe get the null
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
        String bundleName = admin.getBundle(provider.getClass()).getSymbolicName();

        File source;
        try {
            source = provider.getInstallationFolder();
        } catch (IOException e1) {
            ExceptionHandler.process(e1);
            return;
        }

        if (source == null) {
            ExceptionHandler.process(new Exception(Messages.getString("ComponentsFactory.componentNotFound") + pathSource)); //$NON-NLS-1$
            return;
        }

        File[] childDirectories = source.listFiles(file -> file.isDirectory() && file.getName().charAt(0) != '.'
                && !file.getName().equals(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER));

        if (childDirectories != null) {
            if (monitor != null) {
                this.subMonitor = SubMonitor.convert(monitor, Messages.getString("ComponentsFactory.load.components"), //$NON-NLS-1$
                        childDirectories.length);
            }

            for (File currentFolder : childDirectories) {
                try {
                    File xmlMainFile = new File(currentFolder, ComponentFilesNaming.getInstance()
                            .getMainXMLFileName(currentFolder.getName(), getCodeLanguageSuffix()));
                    if (!xmlMainFile.exists()) {
                        // if not a component folder, ignore it.
                        continue;
                    }

                    ComponentFileChecker.checkComponentFolder(currentFolder, getCodeLanguageSuffix());

                    String pathName = xmlMainFile.getAbsolutePath();

                    String applicationPath = ComponentBundleToPath.getPathFromBundle(bundleName);

                    // pathName = C:\myapp\plugins\myplugin\components\mycomponent\mycomponent.xml
                    pathName = (new Path(pathName)).toPortableString();
                    // pathName = C:/myapp/plugins/myplugin/components/mycomponent/mycomponent.xml
                    pathName = pathName.replace(applicationPath, ""); //$NON-NLS-1$
                    // pathName = /components/mycomponent/mycomponent.xml

                    ComponentsCache cache = ComponentManager.getComponentCaches().get(source.getAbsolutePath());
                    EmfComponent currentComp = new EmfComponent(pathName, bundleName, xmlMainFile.getParentFile().getName(),
                            pathSource, cache, false, provider);

                    ComponentManager.setModified(source.getAbsolutePath());

                    if (ComponentsLoader.skipLoadComponent(currentComp)) {
                        // continue;
                    }

                    ComponentsLoader.setComponentPaletteType(currentComp);

                    if (componentList.contains(currentComp)) {
                        log.warn("Component " + currentComp.getName() + " already exists. Cannot load user version."); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        componentList.add(currentComp);
                    }
                } catch (MissingMainXMLComponentFileException e) {
                    log.trace(currentFolder.getName() + " is not a " + getCodeLanguageSuffix() + " component", e); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (BusinessException e) {
                    BusinessException ex = new BusinessException("Cannot load component \"" + currentFolder.getName() + "\": " //$NON-NLS-1$ //$NON-NLS-2$
                            + e.getMessage(), e);
                    ExceptionHandler.process(ex, Level.ERROR);
                }

                if (this.subMonitor != null) {
                    this.subMonitor.worked(1);
                }
                if (this.monitor != null && this.monitor.isCanceled()) {
                    return;
                }
            }
        }
    }

    /**
     * DOC smallet Comment method "checkComponentFolder".
     *
     * @param currentFolder
     * @return
     * @throws IOException 
     * @throws BusinessException
     */

    private File getComponentsLocation(AbstractComponentsProvider componentsProvider, String folder) throws IOException {
    	
    	if (componentsProvider instanceof ComponentsLocationProvider) {
    		return componentsProvider.getInstallationFolder();
    	} else {
            String componentsPath = IComponentsFactory.COMPONENTS_LOCATION;
            IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault()
                    .getService(IBrandingService.class);
            if (breaningService.isPoweredOnlyCamel()) {
                componentsPath = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
            }
            Bundle b = Platform.getBundle(componentsPath);

            File file = null;
            try {
                URL url = FileLocator.find(b, new Path(folder), null);
                if (url == null) {
                    return null;
                }
                URL fileUrl = FileLocator.toFileURL(url);
                file = new File(fileUrl.getPath());
            } catch (Exception e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

            return file;
    	}
    }

    private String getCodeLanguageSuffix() {
        return LanguageManager.getCurrentLanguage().getName();
    }

    @Override
    public int size() {
        waitForInit();
        return componentList.size();
    }

    @Override
    public IComponent get(String name) {
        waitForInit();
        
        for (IComponent comp : componentList) {
            if (comp != null && comp.getName().equals(name)
                    && !ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(comp.getPaletteType())) {
                return comp;
            } // else keep looking
        }
        log.debug("can not get componentList.size: " + componentList.size() + ",name: " + name);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#get(java.lang.String, java.lang.String)
     */
    @Override
    public IComponent get(String name, String paletteType) {
        waitForInit();

        for (IComponent comp : componentList) {
            if (comp != null && comp.getName().equals(name) && paletteType.equals(comp.getPaletteType())) {
                return comp;
            }
        }
        log.debug("can not get componentList.size: " + componentList.size() + ",name: " + name + ",paletteType: " + paletteType);
        return null;
    }

    @Override
    public IComponent getJobletComponent(String name, String paletteType) {
        waitForInit();

        // check if reference joblet component presents
        JobletUtil jobletUtils = new JobletUtil();
        Optional<IComponent> result = jobletUtils.findComponentByName(componentList, name, paletteType);
        if (!result.isPresent()) {
            // check if any name matching joblet component presents
            if (jobletUtils.matchExpression(name)) {
                name = StringUtils.substringAfterLast(name, ":"); //$NON-NLS-1$
                if (StringUtils.isNotBlank(name)) {
                    result = jobletUtils.findComponentByName(componentList, name, paletteType);
                }
            }
        }
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    @Override
    public void initializeComponents(IProgressMonitor monitor) {
        this.monitor = monitor;
        waitForInit();
        this.monitor = null;
        this.subMonitor = null;
    }

    @Override
    public void initializeComponents(IProgressMonitor monitor, boolean duringLogon) {
        this.monitor = monitor;
        waitForInit();
        this.monitor = null;
        this.subMonitor = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#getComponents()
     */
    @Override
    public Set<IComponent> getComponents() {
        waitForInit();
        return componentList;
    }

    @Override
    public boolean isInitializing() {
        return isInitializing.get();
    }

    @Override
    public Collection<IComponent> readComponents() {
        Set<IComponent> components = getComponents();
        Collection<IComponent> readComponents = null;
        synchronized (components) {
            readComponents = Arrays.asList(components.toArray(new IComponent[0]));
        }
        return readComponents;
    }

    @Override
    public Map<String, Map<String, Set<IComponent>>> getComponentNameMap() {
        waitForInit();
        return componentNameMap;
    }

    @Override
    public List<IComponent> getCustomComponents() {
        waitForInit();
        return new ArrayList<>(customComponentList);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#getSkeletons()
     */
    @Override
    public List<String> getSkeletons() {
        waitForInit();
        return skeletonList;
    }

    @Override
    public void reset() {
        if (isInitialized.compareAndSet(true, false)) {
            componentList.clear();
            skeletonList.clear();
            customComponentList.clear();
            Collection<IComponentFactoryFilter> filters = ComponentsFactoryProviderManager.getInstance().getProviders();
            for (IComponentFactoryFilter filter : filters) {
                filter.cleanCache();
            }
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
                IJobletProviderService jobletService = GlobalServiceRegister.getDefault()
                        .getService(IJobletProviderService.class);
                if (jobletService != null) {
                    jobletService.clearJobletComponent();
                }
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkJobletProviderService.class)) {
                ISparkJobletProviderService jobletService = GlobalServiceRegister.getDefault()
                        .getService(ISparkJobletProviderService.class);
                if (jobletService != null) {
                    jobletService.clearSparkJobletComponent();
                }
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkStreamingJobletProviderService.class)) {
                ISparkStreamingJobletProviderService jobletService = GlobalServiceRegister.getDefault()
                        .getService(ISparkStreamingJobletProviderService.class);
                if (jobletService != null) {
                    jobletService.clearSparkStreamingJobletComponent();
                }
            }
            isInitializing.set(true);
        }
    }

    private void waitForInit() {
        init(false);
        int spent = 0;
        int time = 1000;
        int timeout = 1000 * 60 * 10;
        Thread thread = Thread.currentThread();
        while (isInitializing.get()) {
            try {
                thread.sleep(time);
                spent += time;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (spent >= timeout) {
                // may be track in dead lock, throw exception to try to break dead lock
                throw new RuntimeException("Waiting for component initialization timeout!"); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void resetCache() {
        reset();
        if (!CommonsPlugin.isHeadless()) {
            CoreImageProvider.clearComponentIconImages();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#getFamilyTranslation(IComponent component,
     * java.lang.String)
     */
    @Override
    public String getFamilyTranslation(Object component, String text) {
        String translated = Messages.getString(text);

        // if text translated is not in local provider, look into other providers.
        if (translated.startsWith("!!") && component instanceof EmfComponent) { //$NON-NLS-1$
            AbstractComponentsProvider provider = ((EmfComponent) component).getProvider();
            if (provider != null) {
                String translatedFromProvider = provider.getFamilyTranslation(text);
                if (translatedFromProvider != null) {
                    translated = translatedFromProvider;
                }
            }
        }

        return translated;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#resetSpecificComponents()
     */
    @Override
    public void resetSpecificComponents() {
        loadComponentsFromExtensions();
    }

    @Override
    public Map<String, File> getComponentsProvidersFolder() {
        Map<String, File> list = new HashMap<String, File>();

        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        for (AbstractComponentsProvider componentsProvider : componentsProviderManager.getProviders()) {
            try {
                // list.add(componentsProvider.getInstallationFolder());
                list.put(componentsProvider.getContributer(), componentsProvider.getInstallationFolder());
            } catch (IOException e) {
                ExceptionHandler.process(e);
                continue;
            }
        }
        return list;
    }

    @Override
    public List<ComponentProviderInfo> getComponentsProvidersInfo() {
        List<ComponentProviderInfo> list = new ArrayList<ComponentProviderInfo>();
        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        for (AbstractComponentsProvider componentsProvider : componentsProviderManager.getProviders()) {
            try {
                ComponentProviderInfo info = new ComponentProviderInfo();
                info.setId(componentsProvider.getId());
                info.setContributer(componentsProvider.getContributer());
                File folder = componentsProvider.getInstallationFolder();
                if (folder != null) {
                    info.setLocation(folder.getAbsolutePath());
                    list.add(info);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                continue;
            }
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#getComponentsHandler()
     */
    @Override
    public IComponentsHandler getComponentsHandler() {
        return componentsHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsFactory#setComponentsHandler(org.talend.core.model.components.
     * TComponentsHandler)
     */
    @Override
    public void setComponentsHandler(IComponentsHandler componentsHandler) {
        this.componentsHandler = componentsHandler;
    }
    
    public String getCustomComponentBundlePath() {
    	ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
    	AbstractComponentsProvider componentsProvider = componentsProviderManager.loadUserComponentsProvidersFromExtension();
    	String bundle = componentsProvider.getComponentsBundle();
    	return ComponentBundleToPath.getPathFromBundle(bundle);
    	
    }

    public Set<IComponent> getComponentsForInit() {
        return this.componentList;
    }

    public Set<File> getProviderInstallationFolders() {
        return providers.stream().filter(p -> ComponentsLoader.isCachedProvider(p)).map(p -> {
            try {
                return p.getInstallationFolder();
            } catch (IOException e) {
                // never reach
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toSet());
    }

}
