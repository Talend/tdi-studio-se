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
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobNodePropertyTester extends AbstractNodeTester {

    private static final String NEED_STANDARD_NODE = "needStandardNode"; //$NON-NLS-1$

    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (NEED_STANDARD_NODE.equals(property)) {
                return needStandardNode(repositoryNode);
            }
        }
        return null;
    }

    public boolean needStandardNode(RepositoryNode repositoryNode) {
        if (repositoryNode != null) {
            ProjectRepositoryNode root = (ProjectRepositoryNode) repositoryNode.getRoot();
            // FIXME, only load the related plugin, then can display the standard node, else same TOS, only Job Designs
            // with standard job by default, and be children directly.
            if (root != null
                    && (PluginChecker.isMapReducePluginLoader() || PluginChecker
                            .isJobLetPluginLoaded())) {
                return true;
            }
        }
        return false;

    }

}
