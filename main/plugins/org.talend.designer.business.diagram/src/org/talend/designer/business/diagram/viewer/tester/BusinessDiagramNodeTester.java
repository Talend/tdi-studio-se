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
package org.talend.designer.business.diagram.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BusinessDiagramNodeTester extends AbstractNodeTester {

    private static final String IS_BUSINESS_DIAGRAM_NODE = "isBusinessDiagramNode"; //$NON-NLS-1$

    private static final String IS_BUSINESS_DIAGRAM_TOP_NODE = "isBusinessDiagramTopNode"; //$NON-NLS-1$

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
            if (IS_BUSINESS_DIAGRAM_NODE.equals(property)) {
                return isBusinessDiagramNode(repositoryNode);
            } else if (IS_BUSINESS_DIAGRAM_TOP_NODE.equals(property)) {
                return isBusinessDiagramTopNode(repositoryNode);
            }
        }
        return null;
    }

    public boolean isBusinessDiagramNode(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.BUSINESS_PROCESS);
    }

    public boolean isBusinessDiagramTopNode(RepositoryNode repositoryNode) {
        return isTypeTopNode(repositoryNode, ERepositoryObjectType.BUSINESS_PROCESS);
    }

}
