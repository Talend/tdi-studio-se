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
package org.talend.designer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class ExtendedNodeManager {

    static List<IExtendedNodeHandler> extendedNodeHandler = null;

    public static List<IExtendedNodeHandler> getExtendedNodeHandler() {
        if (extendedNodeHandler == null) {
            extendedNodeHandler = new ArrayList<IExtendedNodeHandler>();
            IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = extensionRegistry
                    .getExtensionPoint("org.talend.designer.core.extended_node_handler"); //$NON-NLS-1$
            if (extensionPoint != null) {
                IExtension[] extensions = extensionPoint.getExtensions();
                for (IExtension extension : extensions) {
                    IConfigurationElement[] configurationElements = extension.getConfigurationElements();
                    for (IConfigurationElement configurationElement : configurationElements) {
                        try {
                            Object service = configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
                            if (service instanceof IExtendedNodeHandler) {
                                extendedNodeHandler.add((IExtendedNodeHandler) service);
                            }
                        } catch (CoreException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
        return extendedNodeHandler;
    }

}
