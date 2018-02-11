// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class DeploymentConfsUtilsTest {
    private DeploymentConfsUtils util;

    @Before
    public void setUp() throws Exception {
        util = new DeploymentConfsUtils(ProjectManager.getInstance().getCurrentProject());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLoadConfs() {
        util.loadConfs();
    }

    @Test
    public void testWriteConfModelToJsonFile() {
        fail("Not yet implemented");
    }

    @Test
    public void testRemoveDeletedConfsFolder() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetModuleId() {
        fail("Not yet implemented");
    }

    @Test
    public void testCalculateModulePath() {
        fail("Not yet implemented");
    }

}
