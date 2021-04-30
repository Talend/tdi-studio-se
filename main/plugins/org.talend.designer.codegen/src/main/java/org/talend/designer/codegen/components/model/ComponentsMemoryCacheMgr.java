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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.component_cache.ComponentInfo;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentManager;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.model.components.EmfComponent;

/*
 * Created by bhe on Dec 28, 2020
 */
public class ComponentsMemoryCacheMgr {

    private static Logger LOGGER = Logger.getLogger(ComponentsMemoryCacheMgr.class);

    private static final ComponentsMemoryCacheMgr INSTANCE = new ComponentsMemoryCacheMgr();

    private Map<String, List<ComponentInfo>> cacheComponentInfoMap = new HashMap<String, List<ComponentInfo>>();

    private ReentrantReadWriteLock componentInfoLock = new ReentrantReadWriteLock();

    private Map<IComponent, AbstractComponentsProvider> componentToProviderMap = new ConcurrentHashMap<IComponent, AbstractComponentsProvider>();

    private Set<IComponent> componentList = Collections.newSetFromMap(new ConcurrentHashMap<IComponent, Boolean>());

    private Set<IComponent> customComponentList = Collections.newSetFromMap(new ConcurrentHashMap<IComponent, Boolean>());

    private Set<IComponent> userComponentList = Collections.newSetFromMap(new ConcurrentHashMap<IComponent, Boolean>());

    private ConcurrentLinkedQueue<String> skeletonList = new ConcurrentLinkedQueue<String>();

    private ComponentsMemoryCacheMgr() {
    }

    public static ComponentsMemoryCacheMgr getInstance() {
        return INSTANCE;
    }

    public void addSkeleton(String skl) {
        skeletonList.add(skl);
    }

    public List<String> getSkeletons() {
        List<String> ret = new ArrayList<String>();
        skeletonList.forEach(e -> {
            ret.add(e);
        });
        return ret;
    }

    public void putComponentsProvider(IComponent comp, AbstractComponentsProvider provider) {
        componentToProviderMap.putIfAbsent(comp, provider);
    }

    public Map<IComponent, AbstractComponentsProvider> getComponentsProviders() {
        return Collections.unmodifiableMap(componentToProviderMap);
    }

    public void addComponent(IComponent comp) {
        componentList.add(comp);
        if (comp instanceof EmfComponent) {
            int owner = ((EmfComponent) comp).getComponentInfo().getOwner();
            if ((owner & EmfComponent.OWNER_CUSTOM) > 0) {
                customComponentList.add(comp);
            }
            if ((owner & EmfComponent.OWNER_USER) > 0) {
                userComponentList.add(comp);
            }
        }
    }

    public void addComponents(Collection<IComponent> comps) {
        componentList.addAll(comps);
        for (IComponent comp : comps) {
            if (comp instanceof EmfComponent) {
                int owner = ((EmfComponent) comp).getComponentInfo().getOwner();
                if ((owner & EmfComponent.OWNER_CUSTOM) > 0) {
                    customComponentList.add(comp);
                }
                if ((owner & EmfComponent.OWNER_USER) > 0) {
                    userComponentList.add(comp);
                }
            }
        }
    }

    public void removeComponents(Collection<IComponent> comps) {
        componentList.removeAll(comps);
        for (IComponent comp : comps) {
            if (comp instanceof EmfComponent) {
                int owner = ((EmfComponent) comp).getComponentInfo().getOwner();
                if ((owner & EmfComponent.OWNER_CUSTOM) > 0) {
                    customComponentList.remove(comp);
                }
                if ((owner & EmfComponent.OWNER_USER) > 0) {
                    userComponentList.remove(comp);
                }
            }
        }
    }

    public void clearComponents() {
        componentList.clear();
        customComponentList.clear();
        userComponentList.clear();
    }

    public void addUserComponent(IComponent comp) {
        componentList.add(comp);
        userComponentList.add(comp);
    }

    public void addCustomComponent(IComponent comp) {
        componentList.add(comp);
        customComponentList.add(comp);
    }

    public boolean containComponent(IComponent comp) {
        return componentList.contains(comp);
    }

    public boolean containComponent(String name, ComponentInfo ci) {
        for (IComponent comp : componentList) {
            if (StringUtils.equals(comp.getName(), name) && comp instanceof EmfComponent) {
                String sha1 = ((EmfComponent) comp).getComponentInfo().getSha1();
                return StringUtils.equals(sha1, ci.getSha1());
            }
        }
        return false;
    }

    public Set<IComponent> getComponents() {
        return componentList;
    }

    public Set<IComponent> getUserComponents() {
        return Collections.unmodifiableSet(userComponentList);
    }

    public Set<IComponent> getCustomComponents() {
        return Collections.unmodifiableSet(customComponentList);
    }

    public void update(String key, ComponentInfo info) {
        componentInfoLock.writeLock().lock();
        try {
            List<ComponentInfo> infos = cacheComponentInfoMap.get(key);
            if (infos == null) {
                infos = new ArrayList<ComponentInfo>();
                cacheComponentInfoMap.put(key, infos);
            } else {
                // not empty, need to replace
                Iterator<ComponentInfo> it = infos.iterator();
                while (it.hasNext()) {
                    ComponentInfo ci = it.next();
                    // found same, no need need to update
                    if (StringUtils.equals(ci.getSha1(), info.getSha1())) {
                        return;
                    }
                    // update by removing existing one
                    if ((StringUtils.equals(ci.getUriString(), info.getUriString())
                                    && StringUtils.equals(ci.getSourceBundleName(), info.getSourceBundleName())
                                    && StringUtils.equals(ci.getType(), info.getType()))) {
                        it.remove();
                        break;
                    }
                }
            }

            infos.add(info);

        } finally {
            componentInfoLock.writeLock().unlock();
        }
    }

    public void remove(String key) {
        componentInfoLock.writeLock().lock();
        try {
            cacheComponentInfoMap.remove(key);
        } finally {
            componentInfoLock.writeLock().unlock();
        }
    }

    public Map<String, List<ComponentInfo>> getCachedMap() {
        componentInfoLock.readLock().lock();
        try {
            return Collections.unmodifiableMap(cacheComponentInfoMap);
        } finally {
            componentInfoLock.readLock().unlock();
        }
    }

    public boolean containsKey(String key) {
        componentInfoLock.readLock().lock();
        try {
            return cacheComponentInfoMap.containsKey(key);
        } finally {
            componentInfoLock.readLock().unlock();
        }
    }

    public List<ComponentInfo> get(String key) {
        componentInfoLock.readLock().lock();
        try {
            return cacheComponentInfoMap.get(key);
        } finally {
            componentInfoLock.readLock().unlock();
        }
    }

    public void persist() {
        Map<String, List<ComponentInfo>> cachedData = this.getCachedMap();
        ComponentManager.getComponentCache().getComponentEntryMap().clear();
        cachedData.forEach((k, v) -> {
            BasicEList<ComponentInfo> list = new BasicEList<ComponentInfo>(v);
            ComponentManager.getComponentCache().getComponentEntryMap().put(k, list);
        });
        ComponentManager.setModified(true);
        ComponentManager.saveResource();
    }

    public void clear() {
        componentInfoLock.writeLock().lock();
        try {
            this.cacheComponentInfoMap.clear();
        } finally {
            componentInfoLock.writeLock().unlock();
        }
    }

    public void readCacheFile(String installLocation) {
        ComponentsCache cacheFromFile = null;
        try {
            cacheFromFile = ComponentManager.loadComponentCacheFile(installLocation);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        if (cacheFromFile == null) {
            return;
        }

        Set<Entry<String, EList<ComponentInfo>>> entries = cacheFromFile.getComponentEntryMap().entrySet();

        componentInfoLock.writeLock().lock();
        try {
            for (Entry<String, EList<ComponentInfo>> entry : entries) {
                List<ComponentInfo> list = new ArrayList<ComponentInfo>(entry.getValue());
                this.cacheComponentInfoMap.put(entry.getKey(), list);
            }

        } finally {
            componentInfoLock.writeLock().unlock();
        }
    }

    public void loadComponentsFromMemoryCache(List<AbstractComponentsProvider> providers) {
        Map<String, AbstractComponentsProvider> classProviders = new HashMap<String, AbstractComponentsProvider>();
        providers.forEach(e -> {
            classProviders.put(e.getClass().getCanonicalName(), e);
        });
        Map<String, List<ComponentInfo>> cachedData = this.getCachedMap();
        cachedData.forEach((k, v) -> {
            for (ComponentInfo ci : v) {
                AbstractComponentsProvider provider = classProviders.get(ci.getProviderClass());
                ComponentsLoader.createEmfComponent(k, ci, provider);
            }

        });

    }
}
