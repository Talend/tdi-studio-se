// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.PaletteEntry;
import org.talend.commons.exception.ExceptionHandler;

/**
 * created by hcyi on Mar 23, 2016 Detailled comment
 *
 */
public abstract class AbstractGenericProvider {

    public static final String EXTENSION_ID = "org.talend.designer.core.process_provider"; //$NON-NLS-1$

    public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    public static final String ATTR_PID = "pluginId"; //$NON-NLS-1$

    public static Map<String, AbstractGenericProvider> providerMap = new HashMap<>();

    public static AbstractGenericProvider findProcessProviderFromPID(String pid) {
        if (providerMap == null || !providerMap.containsKey(pid)) {
            findAllProcessProviders();
        }
        return providerMap.get(pid);
    }

    public static Collection<AbstractGenericProvider> findAllProcessProviders() {
        if (providerMap.isEmpty()) {
            IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);
            for (IConfigurationElement elem : elems) {
                String pid = elem.getAttribute(ATTR_PID);
                try {
                    Object provider = elem.createExecutableExtension(ATTR_CLASS);
                    if (provider instanceof AbstractGenericProvider) {
                        AbstractGenericProvider createExecutableExtension = (AbstractGenericProvider) provider;
                        providerMap.put(pid, createExecutableExtension);
                    }
                } catch (CoreException ex) {
                    ExceptionHandler.process(ex);
                }
            }
        }
        return providerMap.values();
    }

    public static void loadComponentsFromProviders() {
        for (AbstractGenericProvider processProvider : findAllProcessProviders()) {
            processProvider.loadComponentsFromExtensionPoint();
        }
    }

    public void loadComponentsFromExtensionPoint() {
        // do nothing.
    }

    public List<PaletteEntry> addPaletteEntry() {
        return null;
    }
}
