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
package org.talend.designer.core.generic.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC zwzhao class global comment. Detailled comment
 */
public class ComponentsFactoryProviderManager {

    private static Logger log = Logger.getLogger(ComponentsFactoryProviderManager.class);

    private static final ComponentsFactoryProviderManager INSTANCE = new ComponentsFactoryProviderManager();

    private Collection<IComponentFactoryFilter> filters;

    private ComponentsFactoryProviderManager() {
    }

    public static ComponentsFactoryProviderManager getInstance() {
        return INSTANCE;
    }

    public Collection<IComponentFactoryFilter> getProviders() {
        if (filters == null) {
            loadFromExtension();
        }
        return filters;
    }

    private void loadFromExtension() {
        filters = new ArrayList<IComponentFactoryFilter>();

        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.talend.designer.core.generic.componentFilter"); //$NON-NLS-1$
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configurationElement : configurationElements) {
                try {
                    IComponentFactoryFilter filter = (IComponentFactoryFilter) configurationElement
                            .createExecutableExtension("class"); //$NON-NLS-1$
                    filters.add(filter);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }
}
