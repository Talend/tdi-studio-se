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

import static java.util.Optional.ofNullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.sdk.component.studio.debounce.DebounceManager;
import org.talend.sdk.component.studio.debounce.DebouncedAction;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.service.AsciidoctorService;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.service.Configuration;
import org.talend.sdk.component.studio.service.UiActionsThreadPool;
import org.talend.sdk.component.studio.ui.composite.TaCoKitComposite;
import org.talend.sdk.component.studio.ui.composite.problemmanager.ComponentViewProblemManager;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public final class Lookups {

    private Lookups() {
        throw new AssertionError();
    }

    public static Runnable init() {
        try {
            final Field instance = GlobalServiceRegister.class.getDeclaredField("instance");
            if (!instance.isAccessible()) {
                instance.setAccessible(true);
            }
            final Object originalInstance = instance.get(null);
            instance.set(null, EnrichedGlobalServiceRegister.clone(GlobalServiceRegister.class.cast(originalInstance)));
            return () -> {
                try {
                    instance.set(null, originalInstance);
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            };
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static UiActionsThreadPool uiActionsThreadPool() {
        try {
            return lookup(UiActionsThreadPool.class);
        } catch (final Exception e) {
            // for tests
            return new UiActionsThreadPool(command -> new Thread(command).start());
        }
    }

    public static DebounceManager debouncer() {
        try {
            return lookup(DebounceManager.class);
        } catch (final Exception e) {
            // for tests mainly
            return new DebounceManager() {
                @Override
                public DebouncedAction createAction() {
                    return new DebouncedAction(this) {
                        private Runnable task;

                        @Override
                        public synchronized void debounce(final Runnable task, final int timeoutMillis) {
                            this.task = task;
                        }

                        @Override
                        public void run() {
                            ofNullable(task).ifPresent(Runnable::run);
                        }
                    };
                }
            };
        }
    }

    public static Configuration configuration() {
        try {
            return lookup(Configuration.class);
        } catch (final Exception e) {
            // for tests mainly
            return new Configuration(false, 1000);
        }
    }

    public static WebSocketClient client() {
        return lookup(WebSocketClient.class);
    }

    public static ComponentService service() {
        try {
            return lookup(ComponentService.class);
        } catch (final Exception e) {
            // for tests mainly
            return new ComponentService(s -> null);
        }
    }

    public static TaCoKitCache taCoKitCache() {
        return lookup(TaCoKitCache.class);
    }

    public static ProcessManager manager() {
        return lookup(ProcessManager.class);
    }

    private static <T> T lookup(final Class<T> type) {
        final BundleContext context = Platform.getBundle(TaCoKitConst.BUNDLE_ID).getBundleContext();
        final ServiceReference<T> clientRef = context.getServiceReference(type);
        return context.getService(clientRef);
    }

    public static AsciidoctorService asciidoctor() {
        try {
            return lookup(AsciidoctorService.class);
        } catch (final Exception e) {
            // for tests mainly
            return new AsciidoctorService();
        }
    }

    private static class EnrichedGlobalServiceRegister extends GlobalServiceRegister {

        private volatile IGenericWizardService wizardService;

        public static EnrichedGlobalServiceRegister clone(final GlobalServiceRegister instance) throws Exception {
            EnrichedGlobalServiceRegister enrichedRegister = new EnrichedGlobalServiceRegister();
            Field[] fields = GlobalServiceRegister.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(enrichedRegister, field.get(instance));
            }
            return enrichedRegister;
        }

        @Override
        public IService getService(final Class klass) {
            final IService service = super.getService(klass);
            if (klass == IGenericWizardService.class) {
                if (wizardService == null) {
                    synchronized (this) {
                        if (wizardService == null) {
                            // final WizardRegistry customService = new WizardRegistry();
                            wizardService = IGenericWizardService.class
                                    .cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                            service.getClass().getInterfaces(), (proxy, method, args) -> {
                                                try {
                                                    switch (method.getName()) {
                                                    case "creatDynamicComposite":
                                                        if (args[1] != null && args[1] instanceof INode) {
                                                            INode node = (INode) args[1];
                                                            // decide whether it is v0 or v1
                                                            IComponent component = node.getComponent();
                                                            if (component != null
                                                                    && ComponentModel.class.isAssignableFrom(component.getClass())) {
                                                                return creatComposite((Composite) args[0], (Element) args[1],
                                                                        (EComponentCategory) args[2], (boolean) args[3]);
                                                            } else {
                                                                // it is v0 component, so call GenericWizardService
                                                                // original method
                                                                return method.invoke(service, args);
                                                            }
                                                        }
                                                        return null;

                                                    default:
                                                        return method.invoke(service, args);
                                                    }
                                                } catch (final InvocationTargetException ite) {
                                                    throw ite.getTargetException();
                                                }
                                            }));
                        }
                    }
                }
                return wizardService;
            }
            return service;
        }

        private Composite creatComposite(final Composite parent, final Element element, final EComponentCategory category,
                final boolean isCompactView) {
            return new TaCoKitComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element, isCompactView,
                    new ComponentViewProblemManager());
        }
    }

}
