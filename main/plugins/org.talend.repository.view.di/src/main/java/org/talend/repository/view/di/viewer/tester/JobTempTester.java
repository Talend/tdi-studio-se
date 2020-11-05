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

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

/**
 * created by kongxiaohan on Jun 23, 2015 Detailled comment
 *
 */
public class JobTempTester extends AbstractNodeTester {

    /**
     * property used to check if object is a mapreduce job
     */
    public static final String IS_JOBTEMP_BRANCH = "isJobTempBranch"; //$NON-NLS-1$

    public boolean isJobTempBranch(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode);
    }

    public boolean isTypeNode(RepositoryNode repositoryNode) {
        ERepositoryObjectType contentType = getNodeContentType(repositoryNode);
        return contentType != null && (ERepositoryObjectType.getAllTypesOfProcess().contains(contentType));
    }

    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (IS_JOBTEMP_BRANCH.equals(property)) {
                return isJobTempBranch(repositoryNode);
            }
        }
        return null;
    }
}
