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
import org.talend.repository.view.di.viewer.tester.RoutinesNodeTester;
import org.talend.repository.viewer.sorter.CodeRepositoryNodeSorter;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutinesRepositoryNodeSorter extends CodeRepositoryNodeSorter {

    RoutinesNodeTester routinesTester = new RoutinesNodeTester();

    /**
     * always on top
     */
    @Override
    protected int compareNode(RepositoryNode n1, RepositoryNode n2) {
        if (routinesTester.isRoutinesTopNode(n1)) {
            return -1;
        }
        if (routinesTester.isRoutinesTopNode(n2)) {
            return 1;
        }
        return 0;
    }
}
