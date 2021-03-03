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
package org.talend.repository.view.di.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;
import org.talend.repository.view.di.model.StandardJobNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobTopNodesPropertyTester extends AbstractNodeTester {

    private static final String IS_JOB_DESIGNS = "isJobDesignsNode"; //$NON-NLS-1$

    private static final String IS_STANDARD = "isStandardNode"; //$NON-NLS-1$

    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (IS_JOB_DESIGNS.equals(property)) {
                return isJobDesignsNode(repositoryNode);
            } else if (IS_STANDARD.equals(property)) {
                return isStandardNode(repositoryNode);
            }
        }
        return null;
    }

    public boolean isJobDesignsNode(RepositoryNode repositoryNode) {
        return !isStandardNode(repositoryNode) && isTypeTopNode(repositoryNode, ERepositoryObjectType.PROCESS);
    }

    public boolean isStandardNode(Object repositoryNode) {
        return repositoryNode instanceof StandardJobNode;
    }

}
