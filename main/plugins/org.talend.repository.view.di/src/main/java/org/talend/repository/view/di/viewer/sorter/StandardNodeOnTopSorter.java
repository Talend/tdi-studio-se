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

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardNodeOnTopSorter extends JobDesignsWithStandardNodeChildrenSorter {

    /**
     * always on top
     */
    @Override
    protected int compareNode(RepositoryNode n1, RepositoryNode n2) {
        if (topNodeTester.isStandardNode(n1)) {
            return -1;
        }
        if (topNodeTester.isStandardNode(n2)) {
            return 1;
        }
        return 0;
    }
}
