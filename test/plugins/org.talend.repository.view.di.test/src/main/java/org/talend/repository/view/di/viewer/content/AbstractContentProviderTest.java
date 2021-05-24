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
package org.talend.repository.view.di.viewer.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC ggu class global comment. Detailled comment
 *
 * this content provider only test the root type of node so far.
 *
 * make sure, the node can be display via CNF in repository view.
 */
public abstract class AbstractContentProviderTest {

    static INavigatorContentService navContentService;

    static ITreeContentProvider contentProvider;

    @BeforeClass
    public static void setup() {
        IRepositoryView repoView = RepositoryManagerHelper.findRepositoryView();
        if (repoView == null) {
            return;
        }
        assertNotNull("Must open the DI repository view to test", repoView);
        assertEquals("Must test in DI repository view", IRepositoryView.VIEW_ID, repoView.getViewSite().getId());

        navContentService = NavigatorContentServiceFactory.INSTANCE.createContentService(IRepositoryView.VIEW_ID,
                repoView.getViewer());
        assertNotNull(navContentService);

        contentProvider = navContentService.createCommonContentProvider();
        assertNotNull(contentProvider);
    }

    @AfterClass
    public static void clean() {
        if (contentProvider != null) {
            contentProvider.dispose();
            contentProvider = null;
        }
        if (navContentService != null) {
            navContentService.dispose();
            navContentService = null;
        }
    }
}
