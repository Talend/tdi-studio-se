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
package org.talend.repository.view.di.viewer.tester;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.model.StandardJobNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobTopNodesPropertyTesterTest {

    private static JobTopNodesPropertyTester tester = null;

    @BeforeClass
    public static void setup() {
        tester = new JobTopNodesPropertyTester();
    }

    @AfterClass
    public static void clean() {
        tester = null;
    }

    @Test
    public void testIsJobDesignsNode4Null() {
        assertFalse(tester.isJobDesignsNode(null));
    }

    @Test
    public void testIsJobDesignsNode4WrongType() {
        RepositoryNode node = new RepositoryNode(null, null, ENodeType.SYSTEM_FOLDER);
        node.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.CONTEXT);
        assertFalse(tester.isJobDesignsNode(node));
    }

    @Test
    public void testIsJobDesignsNode() {
        RepositoryNode node = new RepositoryNode(null, ProjectRepositoryNode.getInstance(), ENodeType.SYSTEM_FOLDER);
        node.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.PROCESS);
        assertTrue(tester.isJobDesignsNode(node));
    }

    @Test
    public void testIsStandardNode4Null() {
        assertFalse(tester.isStandardNode(null));
    }

    @Test
    public void testIsStandardNode4OtherObject() {
        assertFalse(tester.isStandardNode("ABC"));
    }

    @Test
    public void testIsStandardNode() {
        assertTrue(tester.isStandardNode(new StandardJobNode(null)));
    }
}
