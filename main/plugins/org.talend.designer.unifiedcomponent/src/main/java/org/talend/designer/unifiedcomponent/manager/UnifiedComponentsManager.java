// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.unifiedcomponent.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;
import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;
import org.talend.designer.unifiedcomponent.resources.ComponentImage;
import org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier;

/**
 * 
 * created by wchen on Dec 4, 2017 Detailled comment
 *
 */
public class UnifiedComponentsManager {

    private static UnifiedComponentsManager manager;

    private Map<String, DelegateComponent> delegateComponents;

    private Map<String, Set<IComponent>> paletteAndDelegateComps = new HashMap<String, Set<IComponent>>();

    private UnifiedComponentsManager() {
        getDelegateComponents();
    }

    public synchronized static UnifiedComponentsManager getInstance() {
        if (manager == null) {
            manager = new UnifiedComponentsManager();
        }
        return manager;
    }

    public synchronized Collection<DelegateComponent> getDelegateComponents() {
        if (delegateComponents == null) {
            delegateComponents = new HashMap<String, DelegateComponent>();
            BundleContext bc = FrameworkUtil.getBundle(UnifiedComponentsManager.class).getBundleContext();
            Collection<ServiceReference<IComponentDelegate>> components = Collections.emptyList();
            try {
                components = bc.getServiceReferences(IComponentDelegate.class, null);
            } catch (InvalidSyntaxException e) {
                CommonExceptionHandler.process(e);
            }

            // kinds of delegate component class like tDBInput/tDBOutput
            List<IComponentDelegate> componentDelegates = new ArrayList<IComponentDelegate>();
            for (ServiceReference<IComponentDelegate> sr : components) {
                IComponentDelegate delegate = bc.getService(sr);
                componentDelegates.add(delegate);
            }

            Collection<ServiceReference<IComponentsUnifier>> unifiers = Collections.emptyList();
            try {
                unifiers = bc.getServiceReferences(IComponentsUnifier.class, null);
            } catch (InvalidSyntaxException e) {
                CommonExceptionHandler.process(e);
            }
            for (ServiceReference<IComponentsUnifier> sr : unifiers) {
                IComponentsUnifier compUnifier = bc.getService(sr);
                for (IComponentDelegate delegateComp : componentDelegates) {
                    compUnifier.setDelegateComponent(delegateComp);
                    initDelegateComponent(compUnifier);
                }
            }
        }

        return delegateComponents.values();
    }

    public Set<IComponent> getDelegateComponents(String paletteType) {
        if (paletteType == null) {
            paletteType = ComponentCategory.CATEGORY_4_DI.getName();
        }
        Set<IComponent> delegateComponents = paletteAndDelegateComps.get(paletteType);
        if (delegateComponents == null) {
            delegateComponents = new HashSet<IComponent>();
            paletteAndDelegateComps.put(paletteType, delegateComponents);
            Collection<DelegateComponent> delegateComps = getDelegateComponents();
            for (DelegateComponent delegateComp : delegateComps) {
                Set<UnifiedObject> unifiedObjectsByPalette = delegateComp.getUnifiedObjectsByPalette(paletteType);
                if (!unifiedObjectsByPalette.isEmpty()) {
                    DelegateComponent newDelegateComponent = newDelegateComponent(delegateComp, paletteType);
                    newDelegateComponent.getUnifiedObjects().addAll(unifiedObjectsByPalette);
                    delegateComponents.add(newDelegateComponent);
                }

            }
        }
        return delegateComponents;
    }

    private void initDelegateComponent(IComponentsUnifier unifier) {
        if (unifier.getComponentName() != null) {
            IComponentDelegate delegateComp = unifier.getDelegateComponent();
            String key = delegateComp.getComponentName();
            DelegateComponent component = delegateComponents.get(key);
            if (component == null) {
                // create a new component
                component = createDelegateComponent(delegateComp.getFamily(), delegateComp.getComponentName(),
                        delegateComp.getImage());
                delegateComponents.put(key, component);
            }
            UnifiedObject object = new UnifiedObject();
            object.setDatabase(unifier.getDisplayName());
            object.setComponentName(unifier.getComponentName());
            object.getSupportedCategories().addAll(unifier.getCategories());
            object.getParameterMapping().putAll(unifier.getParameterMapping());
            object.getParamMappingExclude().addAll(unifier.getMappingExclude());
            object.getHideFamilies().addAll(unifier.getFamilies());
            component.getUnifiedObjects().add(object);

        }
    }

    private DelegateComponent createDelegateComponent(String familyName, String name, ComponentImage image) {
        DelegateComponent component = new DelegateComponent(familyName, name);
        component.setComponentImage(image);
        return component;
    }

    private DelegateComponent newDelegateComponent(DelegateComponent component, String paletteType) {
        DelegateComponent newComponent = createDelegateComponent(component.getOriginalFamilyName(), component.getName(),
                component.getComponentImage());
        newComponent.setPaletteType(paletteType);
        return newComponent;
    }

}
