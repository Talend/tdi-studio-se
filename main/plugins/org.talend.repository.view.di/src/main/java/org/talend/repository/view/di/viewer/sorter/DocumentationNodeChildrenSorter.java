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
package org.talend.repository.view.di.viewer.sorter;

import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.viewer.tester.doc.DocumentationNodeTester;
import org.talend.repository.view.sorter.RepositoryNodeCompareSorter;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DocumentationNodeChildrenSorter extends RepositoryNodeCompareSorter {

    protected DocumentationNodeTester docTester = new DocumentationNodeTester();

    protected void sortChildren(RepositoryNode parent, Object[] children) {
        if (docTester.isDocTopNode(parent)) {
            sortChildren(children);
        }
    }

}
