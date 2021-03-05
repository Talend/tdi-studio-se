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
package org.talend.designer.core.model.process;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.IGenericProvider;

/**
 * created by hcyi on Mar 23, 2016 Detailled comment
 *
 */
public class GenericProcessProvider {

    public static final String EXTENSION_ID = "org.talend.designer.core.process_provider"; //$NON-NLS-1$

    public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    public static final String ATTR_PID = "pluginId"; //$NON-NLS-1$

    private static GenericProcessProvider singleton;

    public static Map<String, IGenericProvider> providerMap = new HashMap<>();

    public static synchronized GenericProcessProvider getInstance() {
        if (singleton == null) {
            singleton = new GenericProcessProvider();
        }
        return singleton;
    }

    public IGenericProvider findProcessProviderFromPID(String pid) {
        if (providerMap == null || !providerMap.containsKey(pid)) {
            findAllProcessProviders();
        }
        return providerMap.get(pid);
    }

    public Collection<IGenericProvider> findAllProcessProviders() {
        if (providerMap.isEmpty()) {
            IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);
            for (IConfigurationElement elem : elems) {
                String pid = elem.getAttribute(ATTR_PID);
                try {
                    Object provider = elem.createExecutableExtension(ATTR_CLASS);
                    if (provider instanceof IGenericProvider) {
                        IGenericProvider createExecutableExtension = (IGenericProvider) provider;
                        providerMap.put(pid, createExecutableExtension);
                    }
                } catch (CoreException ex) {
                    ExceptionHandler.process(ex);
                }
            }
        }
        return providerMap.values();
    }

    public void loadComponentsFromProviders() {
        for (IGenericProvider processProvider : findAllProcessProviders()) {
            try {
                processProvider.loadComponentsFromExtensionPoint();
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
    }
}
