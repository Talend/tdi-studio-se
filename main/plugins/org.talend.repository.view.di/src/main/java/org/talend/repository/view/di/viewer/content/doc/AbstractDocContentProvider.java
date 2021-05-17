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
package org.talend.repository.view.di.viewer.content.doc;

import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.viewer.tester.doc.DocumentationNodeTester;
import org.talend.repository.viewer.content.SubEmptyTopNodeContentProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractDocContentProvider extends SubEmptyTopNodeContentProvider {

    DocumentationNodeTester docTester = new DocumentationNodeTester();

    public AbstractDocContentProvider() {
        super();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.viewer.content.SingleTopLevelContentProvider#isRootNodeType(java.lang.Object)
     */
    @Override
    protected boolean isRootNodeType(Object element) {
        if (element instanceof RepositoryNode) {
            return docTester.isDocTopNode((RepositoryNode) element);
        } else {
            return false;
        }

    }

}
