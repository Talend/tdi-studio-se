// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.preference;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.preference.IProjectSettingPageTester;

public class ArtifactRepositoryShareSettingPageTester implements IProjectSettingPageTester {

    @Override
    public boolean valid(IConfigurationElement element, IPreferenceNode node) {
        try {
            boolean isLocalProject = ProxyRepositoryFactory.getInstance().isLocalConnectionProvider();
            boolean isOffline = false;
            if (!isLocalProject) {
                RepositoryContext repositoryContext = (RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                        .getProperty(Context.REPOSITORY_CONTEXT_KEY);
                isOffline = repositoryContext.isOffline();
            }
            return !isLocalProject && !isOffline;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

}
