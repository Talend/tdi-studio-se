/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IGenericProvider;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.repository.ProjectManager;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.VirtualComponentModel.VirtualComponentModelType;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCokitImageUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

// note: for now we load the component on the server but
// we can use the mojo generating the meta later
// to avoid to load all components at startup
public class TaCoKitGenericProvider implements IGenericProvider {
    private static final Map<String, VirtualComponentModel> VIRTUAL_COMPONENT_CACHE = new HashMap <String, VirtualComponentModel>();
    @Override
    public void loadComponentsFromExtensionPoint() {
        if (ProjectManager.getInstance().getCurrentProject() == null || !Lookups.configuration().isActive()) {
            return;
        }

        final WebSocketClient client = Lookups.client();
        Stream<Pair<ComponentIndex, ComponentDetail>> details = client.v1().component().details(Locale.getDefault().getLanguage());
        final ConfigTypeNodes configTypes = client.v1().configurationType().getRepositoryModel(true);

        final ComponentService service = Lookups.service();
        final IComponentsFactory factory = ComponentsFactoryProvider.getInstance();
        final Set<IComponent> components = factory.getComponents();
        final Set<String> createdConnectionFamiliySet = new HashSet<String>();
        final Set<String> createdCloseFamiliySet = new HashSet<String>();
        synchronized (components) {
            components.removeIf(component -> {
                if (TaCoKitConst.GUESS_SCHEMA_COMPONENT_NAME.equals(component.getName())) { // this should likely
                    // move...
                    Lookups.taCoKitCache().setTaCoKitGuessSchemaComponent(component);
                }
                return ComponentModel.class.isInstance(component);
            });

            final String reportPath =
                    CorePlugin.getDefault().getPluginPreferences().getString(ITalendCorePrefConstants.IREPORT_PATH);
            final boolean isCatcherAvailable =
                    ComponentsFactoryProvider.getInstance().get(EmfComponent.TSTATCATCHER_NAME,
                            ComponentCategory.CATEGORY_4_DI.getName()) != null;
            details.forEach(pair -> {
                ComponentIndex index = pair.getFirst();
                ComponentDetail detail = pair.getSecond();
                ImageDescriptor imageDesc = null;
                try {
                    imageDesc = service.toEclipseIcon(index.getIcon());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                if (imageDesc == null) {
                    imageDesc = ComponentService.DEFAULT_IMAGE;
                }
                IComponent baseComponentModel = new ComponentModel(index, detail, configTypes, imageDesc, reportPath, isCatcherAvailable);
                components.add(baseComponentModel);
                
                IComponent connectionModel = createConnectionComponent(index, detail, configTypes, reportPath, isCatcherAvailable, createdConnectionFamiliySet);
                if (connectionModel != null) {
                    components.add(connectionModel);
                }
                IComponent closeModel = createCloseConnectionComponent(index, detail, configTypes, reportPath, isCatcherAvailable, createdCloseFamiliySet);
                if (closeModel != null) {
                    components.add(closeModel);
                }                
            });
        }
    }
    

    private VirtualComponentModel createCloseConnectionComponent(final ComponentIndex index, final ComponentDetail detail, final ConfigTypeNodes configTypeNodes,String reportPath, boolean isCatcherAvailable, Set<String> createdFamiliySet) {
        boolean isSupport = false;
        VirtualComponentModel model = null;
        if (detail != null && detail.getActions() != null) {
            for (ActionReference action: detail.getActions()) {
                //if ("CLOSE_CONNECTION".equals(action.getName())) {
                    isSupport = true;
                    break;
                //}
            }
        }
        if (isSupport && !createdFamiliySet.contains(index.getId().getFamily())) {
            ImageDescriptor imageDesc = null;
            try {
                imageDesc = TaCokitImageUtil.getConnectionImage(detail.getId().getFamilyId());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            if (imageDesc == null) {
                imageDesc = ComponentService.DEFAULT_IMAGE;
            }
            model = new VirtualComponentModel(index, detail, configTypeNodes, imageDesc, reportPath, isCatcherAvailable, VirtualComponentModelType.CLOSE_CONNECTION); 
            registeVirtualComponent(model);
            createdFamiliySet.add(index.getId().getFamily());
        }

        return model;
    }
    private VirtualComponentModel createConnectionComponent(final ComponentIndex index, final ComponentDetail detail, final ConfigTypeNodes configTypeNodes,String reportPath,boolean isCatcherAvailable, Set<String> createdFamiliySet) {
        boolean isSupport = false;
        VirtualComponentModel model = null;
        if (detail != null && detail.getActions() != null) {
            for (ActionReference action: detail.getActions()) {
                //if ("CLOSE_CONNECTION".equals(action.getName())) {
                    isSupport = true;
                    break;
                //}
            }
        }
        if (isSupport && !createdFamiliySet.contains(index.getId().getFamily())) {
            ImageDescriptor imageDesc = null;
          try {
              imageDesc = TaCokitImageUtil.getConnectionImage(detail.getId().getFamilyId());
          } catch (Exception e) {
              ExceptionHandler.process(e);
          }
          if (imageDesc == null) {
              imageDesc = ComponentService.DEFAULT_IMAGE;
          }
          model = new VirtualComponentModel(index, detail, configTypeNodes, imageDesc, reportPath, isCatcherAvailable, VirtualComponentModelType.CONNECTION); 
          registeVirtualComponent(model);
          createdFamiliySet.add(index.getId().getFamily());
      }

        return model;
    }

    @Override // unused
    public List<?> addPaletteEntry() {
        return emptyList();
    }
    
    private void registeVirtualComponent(VirtualComponentModel component) {
        VIRTUAL_COMPONENT_CACHE.put(component.getName(), component);
    }

    public static boolean isVirtualComponent(String componentName) {
        if (VIRTUAL_COMPONENT_CACHE.containsKey(componentName)) {
            return true;
        }
        return false;
    }
    public static Map<String, PropertyDefinitionDecorator> getVirtualComponentProperties(String componentName) {
        if (VIRTUAL_COMPONENT_CACHE.containsKey(componentName)) {
            final Map<String, PropertyDefinitionDecorator> tree = new HashMap<>();
            VirtualComponentModel component = VIRTUAL_COMPONENT_CACHE.get(componentName);
            TaCoKitCache cache = Lookups.taCoKitCache();
            ConfigTypeNode configTypeNode = cache.findConfigTypeNodeById(component.getDetail().getId().getFamily(), "datastore");
            if (configTypeNode != null && configTypeNode.getProperties() != null) {
                final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator.wrap(configTypeNode.getProperties());
                properties.forEach(p -> tree.put(p.getPath(), p));
            }
            return tree;
        }
        return null;
    }
    
}
