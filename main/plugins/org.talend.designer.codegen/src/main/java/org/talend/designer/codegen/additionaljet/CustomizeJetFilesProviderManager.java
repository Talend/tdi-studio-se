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
package org.talend.designer.codegen.additionaljet;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.designer.codegen.i18n.Messages;

/***/
public final class CustomizeJetFilesProviderManager {

    private static Logger log = Logger.getLogger(CustomizeJetFilesProviderManager.class);

    private static final CustomizeJetFilesProviderManager INSTANCE = new CustomizeJetFilesProviderManager();

    private Collection<AbstractJetFileProvider> providers;

    private CustomizeJetFilesProviderManager() {
    }

    public static CustomizeJetFilesProviderManager getInstance() {
        return INSTANCE;
    }

    public Collection<AbstractJetFileProvider> getProviders() {
        if (providers == null) {
            loadJetsProvidersFromExtension();
        }
        return providers;
    }

    private void loadJetsProvidersFromExtension() {
        providers = new ArrayList<AbstractJetFileProvider>();

        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.talend.designer.codegen.additional_jetfile"); //$NON-NLS-1$
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configurationElement : configurationElements) {
                String id = configurationElement.getAttribute("id"); //$NON-NLS-1$
                try {
                    AbstractJetFileProvider jetProvider = (AbstractJetFileProvider) configurationElement
                            .createExecutableExtension("class"); //$NON-NLS-1$
                    jetProvider.setId(id);

                    providers.add(jetProvider);

                } catch (CoreException e) {
                    log.error(Messages.getString("JetFilesProviderManager.unableLoad", id), e); //$NON-NLS-1$
                }
            }
        }
    }

}
