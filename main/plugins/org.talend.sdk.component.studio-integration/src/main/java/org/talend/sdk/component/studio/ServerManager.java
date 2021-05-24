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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.tomcat.websocket.Constants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.osgi.hook.maven.MavenResolver;
import org.talend.sdk.component.studio.debounce.DebounceManager;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.mvn.Mvn;
import org.talend.sdk.component.studio.service.AsciidoctorService;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.service.Configuration;
import org.talend.sdk.component.studio.service.UiActionsThreadPool;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class ServerManager {

    private static ServerManager instance;

    private ProcessManager manager;

    private final Collection<ServiceRegistration<?>> services = new ArrayList<>();

    private WebSocketClient client;

    private Runnable reset;

    private DebounceManager debounceManager;

    private UiActionsThreadPool uiActionsThreadPool;

    private volatile boolean start;

    private static Object lock = new Object();

    private ServerManager() {
        /**
         * update/set "talend.studio.version", because TCK server need to know whether running on studio by checking
         * this parameter
         */
        VersionUtils.getInternalVersion();
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ServerManager();
                }
            }
        }
        return instance;
    }

    public synchronized void start() throws Exception {
        if (start) {
            return;
        }
        try {
            // make sure all things are cleaned
            try {
                stop();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

            final BundleContext ctx = SdkComponentPlugin.getDefault().getBundle().getBundleContext();
            final Configuration configuration = new Configuration(!Boolean.getBoolean("component.kit.skip"),
                    Integer.getInteger("component.debounce.timeout", 750));
            services.add(ctx.registerService(Configuration.class.getName(), configuration, new Hashtable<>()));
            debounceManager = new DebounceManager();
            services.add(ctx.registerService(DebounceManager.class.getName(), debounceManager, new Hashtable<>()));
            uiActionsThreadPool = new UiActionsThreadPool(Executors.newCachedThreadPool(
                    new BasicThreadFactory.Builder().namingPattern(UiActionsThreadPool.class.getName() + "-%d").build()));
            services.add(ctx.registerService(UiActionsThreadPool.class.getName(), uiActionsThreadPool, new Hashtable<>()));
            if (!configuration.isActive()) {
                return;
            }
            services.add(ctx.registerService(AsciidoctorService.class.getName(), new AsciidoctorService(), new Hashtable<>()));

            extractFiles();

            reset = Lookups.init();

            final MavenResolver mavenResolver = findMavenResolver();
            final Function<String, File> mvnResolverImpl = gav -> {
                try { // convert to pax-url syntax
                    return mavenResolver.resolve(Mvn.locationToMvn(gav));
                } catch (final IOException e) {
                    throw new IllegalArgumentException("can't resolve '" + gav + "', "
                            + "in development ensure you are using maven" + ".repository=global in configuration/config.ini, "
                            + "in a standalone installation, " + "ensure the studio maven repository contains this dependency",
                            e);
                }
            };
            manager = new ProcessManager(GAV.INSTANCE.getGroupId(), mvnResolverImpl);
            manager.start();

            client = new WebSocketClient("ws://", String.valueOf(manager.getPort()), "/websocket/v1",
                    Long.getLong("talend.component.websocket.client.timeout", Constants.IO_TIMEOUT_MS_DEFAULT));
            client.setSynch(() -> {
                manager.waitForServer(() -> {
                    client.setServerHost(manager.getServerAddress());
                    client.v1().healthCheck();
                });
                client.setServerHost(manager.getServerAddress());
            });

            services.add(ctx.registerService(ProcessManager.class.getName(), manager, new Hashtable<>()));
            services.add(ctx.registerService(WebSocketClient.class.getName(), client, new Hashtable<>()));
            services.add(ctx.registerService(ComponentService.class.getName(), new ComponentService(mvnResolverImpl),
                    new Hashtable<>()));
            services.add(ctx.registerService(TaCoKitCache.class.getName(), new TaCoKitCache(), new Hashtable<>()));
            start = true;
        } catch (Throwable ex) {
            start = false;
            try {
                stop();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            throw ex;
        }
    }

    private void extractFiles() throws Exception {
        final String codegenUrl = CommonsPlugin.getBundleRealURL("org.talend.designer.codegen").getFile();
        final TemplatesExtractor stubExtractor = new TemplatesExtractor("jet_stub/generic", //$NON-NLS-1$
                codegenUrl, "tacokit/jet_stub"); //$NON-NLS-1$
        stubExtractor.extract();
        final TemplatesExtractor guessSchemaExtractor = new TemplatesExtractor("components/tTaCoKitGuessSchema", //$NON-NLS-1$
                codegenUrl, "tacokit/components"); //$NON-NLS-1$
        guessSchemaExtractor.extract();
    }

    public boolean isStarted() {
        return start;
    }

    public synchronized void stop() throws Exception {
        services.forEach(ServiceRegistration::unregister);
        services.clear();

        RuntimeException error = null;
        try {
            if (debounceManager != null) {
                debounceManager.close();
                debounceManager = null;
            }
        } catch (final RuntimeException re) {
            error = re;
        }
        try {
            if (uiActionsThreadPool != null) {
                uiActionsThreadPool.close();
                uiActionsThreadPool = null;
            }
        } catch (final RuntimeException re) {
            error = re;
        }
        try {
            if (manager != null) {
                manager.close();
                manager = null;
            }
        } catch (final RuntimeException re) {
            error = re;
        }
        try {
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (final RuntimeException ioe) {
            if (error != null) {
                throw error;
            }
            throw ioe;
        }

        if (reset != null) {
            reset.run();
        }

        if (error != null) {
            throw error;
        }
        start = false;
    }

    private MavenResolver findMavenResolver() {
        final BundleContext bundleContext = SdkComponentPlugin.getDefault().getBundle().getBundleContext();
        final ServiceReference<MavenResolver> serviceReference = bundleContext.getServiceReference(MavenResolver.class);
        MavenResolver mavenResolver = null;
        if (serviceReference != null) {
            mavenResolver = bundleContext.getService(serviceReference);
        }
        if (mavenResolver == null) {
            throw new IllegalArgumentException("No MavenResolver found");
        }
        return mavenResolver;
    }

}
