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

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.navigator.TalendRepositoryRoot;

/**
 * DOC ggu class global comment. Detailled comment
 *
 * this content provider only test the root type of node so far.
 *
 * make sure, the node can be display via CNF in repository view.
 */
public abstract class AbstractRootNodeContentProviderTest extends AbstractContentProviderTest {

    TalendRepositoryRoot root = new TalendRepositoryRoot();

    @Test
    public void testTopNodeLoad() {
        if (contentProvider == null) {
            return;
        }
        final ERepositoryObjectType testType = getTestType();
        assertNotNull(testType);
        RepositoryNode testNode = ProjectRepositoryNode.getInstance().getRootRepositoryNode(testType);
        assertNotNull(testNode);

        Object[] elements = null;
        if (isRoot()) {
            elements = contentProvider.getElements(root);
        } else {
            elements = contentProvider.getChildren(root);
        }
        assertNotNull(elements);
        assert (elements.length > 0); // must children

        assert (Arrays.asList(elements).contains(testNode));
    }

    protected boolean isRoot() {
        return false;
    }

    protected abstract ERepositoryObjectType getTestType();
}
