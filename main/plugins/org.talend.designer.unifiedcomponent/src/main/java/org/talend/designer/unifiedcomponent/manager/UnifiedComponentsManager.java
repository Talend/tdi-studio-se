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
package org.talend.designer.unifiedcomponent.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.designer.core.model.components.UnifiedJDBCBean;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;
import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;
import org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier;
import org.talend.designer.unifiedcomponent.unifier.jdbc.JDBCComponentsUnifier;

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
            // init additional JDBC component
            Map<String, UnifiedJDBCBean> additionalJDBC = UnifiedComponentUtil.getAdditionalJDBC();
            for (UnifiedJDBCBean bean : additionalJDBC.values()) {
                JDBCComponentsUnifier jdbcUnifier = new JDBCComponentsUnifier();
                jdbcUnifier.setDisplayName(bean.getDisplayName());
                for (IComponentDelegate delegateComp : componentDelegates) {
                    jdbcUnifier.setDelegateComponent(delegateComp);
                    if (StringUtils.isNotBlank(jdbcUnifier.getComponentName())
                            && UnifiedComponentUtil.isUnsupportedComponent(jdbcUnifier.getComponentName(), bean)) {
                        continue;
                    }
                    initDelegateComponent(jdbcUnifier);
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
        String componentName = unifier.getComponentName();
        if (componentName != null) {
            IComponentDelegate delegateComp = unifier.getDelegateComponent();
            String key = delegateComp.getComponentName();
            DelegateComponent component = delegateComponents.get(key);
            if (component == null) {
                // create a new component
                component = createDelegateComponent(delegateComp.getFamily(), delegateComp.getComponentName(),
                        delegateComp.getImage());
            }

            IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault()
                    .getService(IComponentsService.class);
            Set<String> exsitCategories = new HashSet<String>();
            for (String paletteType : unifier.getCategories()) {
                IComponent emfComponent = compService.getComponentsFactory().get(componentName, paletteType);
                if (emfComponent != null) {
                    exsitCategories.add(paletteType);
                }
            }
            if (!exsitCategories.isEmpty()) {
                UnifiedObject object = new UnifiedObject();
                object.setDatabase(unifier.getDisplayName());
                object.setComponentName(componentName);
                object.getSupportedCategories().addAll(unifier.getCategories());
                object.getParameterMapping().putAll(unifier.getParameterMapping());
                object.getConnectorMapping().putAll(unifier.getConnectorMapping());
                object.getParamMappingExclude().addAll(unifier.getMappingExclude());
                object.getHideFamilies().addAll(unifier.getFamilies());
                component.getUnifiedObjects().add(object);

                delegateComponents.put(key, component);
            }

        }
    }

    private DelegateComponent createDelegateComponent(String familyName, String name, IImage image) {
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
