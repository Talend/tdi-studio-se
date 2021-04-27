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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.component_cache.ComponentInfo;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.filters.ComponentsFactoryProviderManager;
import org.talend.core.model.components.filters.IComponentFactoryFilter;
import org.talend.designer.core.model.components.EmfComponent;

/*
 * Created by bhe on Dec 31, 2020
 */
public class ComponentsLoader {

    private static final Logger LOGGER = Logger.getLogger(ComponentsLoader.class);

    private static final ComponentsLoader INSTANCE = new ComponentsLoader();

    private ComponentsCache cacheFromFile;

    private final Map<String, AbstractComponentsProvider> providers = new HashMap<String, AbstractComponentsProvider>();

    private ComponentsLoader() {
        loadComponentProviders();
    }

    public static ComponentsLoader getInstance() {
        return INSTANCE;
    }

    private void readCacheFile() {
        String installLocation = new Path(Platform.getConfigurationLocation().getURL().getPath()).toFile().getAbsolutePath();
        try {
            cacheFromFile = ComponentsFactory.loadComponentResource(installLocation);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private void loadComponentProviders() {
        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        componentsProviderManager.getProviders().forEach(e -> {
            try {
                e.preComponentsLoad();
            } catch (IOException e1) {
                LOGGER.error(e1);
            }
            providers.put(e.getClass().getCanonicalName(), e);
        });
    }

    public void loadAllComponentsFromIndex(Set<IComponent> allSet, Set<IComponent> customSet, Set<IComponent> userSet,
            Map<IComponent, AbstractComponentsProvider> componentToProviderMap) {
        readCacheFile();
        if (cacheFromFile == null) {
            return;
        }
        int total = 0;
        Set<Entry<String, EList<ComponentInfo>>> entries = cacheFromFile.getComponentEntryMap().entrySet();
        for (Entry<String, EList<ComponentInfo>> entry : entries) {
            for (ComponentInfo ci : entry.getValue()) {
                AbstractComponentsProvider provider = providers.get(ci.getProviderClass());
                try {
                    EmfComponent comp = new EmfComponent(entry.getKey(), ci, provider);

                    if (skipLoadComponent(comp)) {
                        continue;
                    }

                    setComponentType(comp, provider);

                    componentToProviderMap.put(comp, provider);

                    if (provider.isCustom()) {
                        customSet.add(comp);
                    }

                    if (isUserComponent(ci.getPathSource())) {
                        userSet.add(comp);
                    }

                    allSet.add(comp);
                    total++;

                    LOGGER.debug("comp loaded: " + comp.getName() + ",palette type: " + comp.getPaletteType());

                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        LOGGER.info("allSet size: " + allSet.size() + ", total: " + total);
    }

    public static boolean skipLoadComponent(EmfComponent comp) {
        boolean hiddenComponent = false;

        Collection<IComponentFactoryFilter> filters = ComponentsFactoryProviderManager.getInstance().getProviders();
        for (IComponentFactoryFilter filter : filters) {
            if (!filter.isAvailable(comp.getName())) {
                hiddenComponent = true;
                break;
            }
        }

        // if the component is not needed in the current branding,
        // and that this one IS NOT a specific component for code generation
        // just don't load it
        if (hiddenComponent && !(comp.getOriginalFamilyName().contains("Technical") || comp.isTechnical())) {
            return true;
        }

        // if the component is not needed in the current branding,
        // and that this one IS a specific component for code generation,
        // hide it
        if (hiddenComponent && (comp.getOriginalFamilyName().contains("Technical") || comp.isTechnical())) {
            comp.setVisible(false);
            comp.setTechnical(true);
        }
        return false;
    }

    public static void setComponentType(EmfComponent comp, AbstractComponentsProvider provider) {

        if (provider.getId().contains("Camel")) {
            comp.setPaletteType(ComponentCategory.CATEGORY_4_CAMEL.getName());
        } else {
            comp.setPaletteType(comp.getType());
        }
    }

    public static boolean isUserComponent(String pathSource) {
        if (pathSource != null) {
            Path userComponent = new Path(pathSource);
            Path templatePath = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER + File.separatorChar
                    + IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER + File.separatorChar
                    + ComponentUtilities.getExtFolder(ComponentsFactory.OLD_COMPONENTS_USER_INNER_FOLDER));
            if (userComponent.equals(templatePath)) {
                return true;
            }
        }
        return false;
    }
}
