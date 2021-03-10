// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.view.di.viewer.tester;

import org.talend.core.PluginChecker;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

public class RoutinesJarNodeTester extends AbstractNodeTester {

    private static final String IS_ROUTINESJAR = "isRoutinesJar"; //$NON-NLS-1$

    private static final String IS_NEED_SHOW_CUSTOMJAR_NODE = "isNeedShowCustomJarNode"; //$NON-NLS-1$

    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (IS_ROUTINESJAR.equals(property)) {
                return isRoutinesJar(repositoryNode);
            } else if (IS_NEED_SHOW_CUSTOMJAR_NODE.equals(property)) {
                return PluginChecker.isTIS();
            }
        }
        return null;
    }

    public boolean isRoutinesJar(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.ROUTINESJAR);
    }

    public boolean isRoutinesJarTopNode(RepositoryNode repositoryNode) {
        return isTypeTopNode(repositoryNode, ERepositoryObjectType.ROUTINESJAR);
    }

}
