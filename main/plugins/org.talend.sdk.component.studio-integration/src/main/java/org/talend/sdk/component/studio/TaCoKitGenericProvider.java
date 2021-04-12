/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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

import java.util.List;
import java.util.Locale;
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
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

// note: for now we load the component on the server but
// we can use the mojo generating the meta later
// to avoid to load all components at startup
public class TaCoKitGenericProvider implements IGenericProvider {

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
                components.add(new ComponentModel(index, detail, configTypes, imageDesc, reportPath, isCatcherAvailable));
            });
        }
    }

    @Override // unused
    public List<?> addPaletteEntry() {
        return emptyList();
    }
}
