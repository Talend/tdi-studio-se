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
package org.talend.repository.view.di.viewer.tester.doc;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DocumentationNodeTester extends AbstractNodeTester {

    private static final String IS_DOC_TOP_NODE = "isDocTopNode"; //$NON-NLS-1$

    private static final String IS_DOC_NODE = "isDocNode"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.tester.AbstractNodeTester#testProperty(java.lang.Object, java.lang.String,
     * java.lang.Object[], java.lang.Object)
     */
    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (IS_DOC_NODE.equals(property)) {
                return isDocNode(repositoryNode);
            } else if (IS_DOC_TOP_NODE.equals(property)) {
                return isDocTopNode(repositoryNode);
            }
        }
        return null;
    }

    public boolean isDocNode(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.DOCUMENTATION);
    }

    public boolean isDocTopNode(RepositoryNode repositoryNode) {
        return isTypeTopNode(repositoryNode, ERepositoryObjectType.DOCUMENTATION);
    }
}
