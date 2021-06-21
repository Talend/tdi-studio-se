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
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.osgi.container.Module;
import org.eclipse.osgi.container.ModuleContainer;
import org.eclipse.osgi.container.ModuleDatabase;
import org.eclipse.osgi.internal.framework.BundleContextImpl;
import org.eclipse.osgi.internal.framework.EquinoxBundle;
import org.eclipse.osgi.internal.framework.EquinoxContainer;
import org.eclipse.osgi.internal.framework.EquinoxContainerAdaptor;
import org.eclipse.osgi.storage.Storage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleListener;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.DesignerCoreService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.sdk.component.runtime.manager.ComponentManager;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentId;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.studio.service.ComponentService;

/**
 * Unit-tests for {@link ComponentModel}
 */
@Disabled
class ComponentModelTest {

    private static Runnable stop;

    @BeforeAll
    static void mockOSGI() throws Exception {
        final EquinoxContainer equinoxContainer = new EquinoxContainer(new HashMap<>(), null);
        final EquinoxContainerAdaptor adaptor =
                new EquinoxContainerAdaptor(equinoxContainer, Storage.createStorage(equinoxContainer), new HashMap<>());
        final ModuleContainer moduleContainer = new ModuleContainer(adaptor, new ModuleDatabase(adaptor));
        final EquinoxBundle bundle = new EquinoxBundle(1L, "ici", moduleContainer,
                EnumSet.noneOf(Module.Settings.class), 0, equinoxContainer) {

            @Override
            public String getSymbolicName() {
                return "test";
            }
        };
        final BundleContextImpl bundleContext = new BundleContextImpl(bundle, equinoxContainer) {

            @Override
            public void addBundleListener(final BundleListener listener) {
                // no-op
            }

            @Override
            public void removeBundleListener(final BundleListener listener) {
                // no-op
            }
        };
        final CorePlugin corePlugin = new CorePlugin();
        final DesignerPlugin designerPlugin = new DesignerPlugin();
        Stream.of(corePlugin, designerPlugin).forEach(p -> {
            try {
                p.start(bundleContext);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        });
        final Field instance = GlobalServiceRegister.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, new GlobalServiceRegister() {

            @Override
            public IService getService(final Class klass) {
                if (IDesignerCoreService.class == klass) {
                    return new DesignerCoreService() {

                        @Override
                        public String getPreferenceStore(final String key) {
                            return null;
                        }
                    };
                }
                return super.getService(klass);
            }
        });
        stop = () -> {
            try {
                try {
                    corePlugin.stop(bundleContext);
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
                instance.set(null, null);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @AfterAll
    static void unmockOSGI() {
        ofNullable(stop).ifPresent(Runnable::run);
    }

    @Test
    void getModuleNeeded() {
        final ComponentId id = new ComponentId("id", "family", "plugin", "group:plugin:1", "XML", "XMLInput");
        final ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        final ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        final ComponentModel componentModel = new ComponentModel(idx, detail) {

            @Override
            protected ComponentService.Dependencies getDependencies() {
                return new ComponentService(s -> s.startsWith("org.talend.sdk.component:component-runtime-manager:")
                        ? jarLocation(ComponentManager.class)
                        : null).getDependencies();
            }

            @Override
            protected boolean hasTcomp0Component(final INode iNode) {
                return false;
            }
        };
        final List<ModuleNeeded> modulesNeeded = componentModel.getModulesNeeded();
        assertEquals(19, modulesNeeded.size());
        // just assert a few
        assertTrue(modulesNeeded.stream().anyMatch(m -> m.getModuleName().startsWith("component-runtime-manager")));
        assertTrue(modulesNeeded.stream().anyMatch(m -> m.getModuleName().startsWith("component-runtime-impl")));
        assertTrue(modulesNeeded.stream().anyMatch(m -> m.getModuleName().startsWith("component-runtime-di")));
        assertTrue(modulesNeeded.stream().anyMatch(m -> m.getModuleName().startsWith("xbean-reflect")));
        assertTrue(modulesNeeded.stream().anyMatch(m -> "plugin-1.jar".equals(m.getModuleName())));
    }

    @Test
    void testGetOriginalFamilyName() {
        String expectedFamilyName = "Local/XML|File/XML";

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        assertEquals(expectedFamilyName, componentModel.getOriginalFamilyName());
    }

    @Test
    void testGetLongName() {
        String expectedName = "XML Input";

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        assertEquals(expectedName, componentModel.getLongName());
    }

    @Disabled("Cannot be tested as CorePlugin is null")
    @Test
    void testCreateConnectors() {

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        List<? extends INodeConnector> connectors = componentModel.createConnectors(null);
        assertEquals(22, connectors.size());
    }

    @Test
    void testCreateReturns() {

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        List<? extends INodeReturn> returnVariables = componentModel.createReturns(null);
        assertEquals(2, returnVariables.size());
        INodeReturn errorMessage = returnVariables.get(0);
        assertEquals("Error Message", errorMessage.getDisplayName());
        assertEquals("!!!NodeReturn.Availability.AFTER!!!", errorMessage.getAvailability());
        assertEquals("String", errorMessage.getType());
        INodeReturn numberLines = returnVariables.get(1);
        assertEquals("Number of line", numberLines.getDisplayName());
        assertEquals("!!!NodeReturn.Availability.AFTER!!!", numberLines.getAvailability());
        assertEquals("id_Integer", numberLines.getType());
    }

    @Test
    void testGetAvailableProcessorCodeParts() {

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Processor", 1, emptyList(), null,
                emptyList(), emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        List<ECodePart> codeParts = componentModel.getAvailableCodeParts();
        assertEquals(4, codeParts.size());
        assertTrue(codeParts.contains(ECodePart.BEGIN));
        assertTrue(codeParts.contains(ECodePart.MAIN));
        assertTrue(codeParts.contains(ECodePart.FINALLY));
    }

    @Test
    void testGetAvailableInputCodeParts() {

        ComponentId id = new ComponentId("id", "family", "plugin", "plugin", "XML", "XMLInput");
        ComponentIndex idx =
                new ComponentIndex(id, "XML Input", null, null, null, 1, Arrays.asList("Local", "File"), null, emptyMap());
        ComponentDetail detail = new ComponentDetail(id, "XML Input", null, "Input", 1, emptyList(), null, emptyList(),
                emptyList(), emptyList(), null);
        ComponentModel componentModel = new ComponentModel(idx, detail);

        List<ECodePart> codeParts = componentModel.getAvailableCodeParts();
        assertEquals(3, codeParts.size());
        assertTrue(codeParts.contains(ECodePart.BEGIN));
        assertTrue(codeParts.contains(ECodePart.END));
        assertTrue(codeParts.contains(ECodePart.FINALLY));
    }
}
