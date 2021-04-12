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
package org.talend.repository.view.di.viewer.filter;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.talend.repository.view.di.viewer.tester.JobTopNodesPropertyTester;
import org.talend.repository.viewer.filter.PerspectiveFilterHelper;
import org.talend.repository.viewer.filter.RepositoryNodeFilterHelper;

public class StandardJobNodeFilter extends ViewerFilter {

    private JobTopNodesPropertyTester jobTopTester = new JobTopNodesPropertyTester();

    /**
     * only check for standard node only,when the repository filter was enabled
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (jobTopTester.isStandardNode(element) && RepositoryNodeFilterHelper.isActivedFilter()) {
            final String filterByNodeKey = RepositoryNodeFilterHelper.getFilterByNodeKey(
                    RepositoryNodeFilterHelper.getPerspectiveId(), PerspectiveFilterHelper.isActivedPerspectiveFilter());
            String[] filterByNode = RepositoryNodeFilterHelper.getFiltersByPreferenceKey(filterByNodeKey);
            if (filterByNode != null) {
                List<String> filterList = Arrays.asList(filterByNode);
                if (filterList.contains(element.getClass().getName())) {
                    return false;
                }
            }
        }
        return true;
    }
}
