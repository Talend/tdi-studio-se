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
package org.talend.repository.view.di.metadata.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.SubNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class QueryNodeTester extends SubNodeTester {

    private static final String IS_QUERY = "isQuery"; //$NON-NLS-1$

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
            if (IS_QUERY.equals(property)) {
                return isQuery(repositoryNode);
            }
        }
        return null;
    }

    public boolean isQuery(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.METADATA_CON_QUERY);
    }

    @Override
    public ERepositoryObjectType findParentItemType(RepositoryNode repositoryNode) {
        final ERepositoryObjectType objectType = repositoryNode.getObjectType();
        if (objectType == ERepositoryObjectType.METADATA_CON_QUERY) {
            final IRepositoryViewObject object = repositoryNode.getObject();
            if (object != null && object instanceof QueryRepositoryObject) {
                final QueryRepositoryObject queryObj = (QueryRepositoryObject) object;
                final IRepositoryViewObject viewObject = queryObj.getViewObject();
                if (viewObject != null) {
                    return viewObject.getRepositoryObjectType();
                }
            }
        }
        return null;
    }

}
