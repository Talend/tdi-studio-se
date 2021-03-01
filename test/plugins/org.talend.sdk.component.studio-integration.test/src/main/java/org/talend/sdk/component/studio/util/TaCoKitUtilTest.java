// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.util;

import static java.util.Collections.emptyMap;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.talend.sdk.component.server.front.model.ComponentId;
import org.talend.sdk.component.server.front.model.ComponentIndex;

/**
 * created by hcyi on Oct 9, 2019
 * Detailled comment
 *
 */
public class TaCoKitUtilTest {

    @Test
    public void testGetDisplayName1() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.test.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                                                        null, emptyMap());
        String displayName = TaCoKitUtil.getDisplayName(index);
        Assert.assertEquals("TestInput", displayName);//$NON-NLS-1$
    }

    @Test
    public void testGetDisplayName2() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.talend.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        String displayName = TaCoKitUtil.getDisplayName(index);
        Assert.assertEquals("tTestInput", displayName);//$NON-NLS-1$
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendNull() throws Exception {
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(null));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendEmpty() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test", "",
                "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendFalse() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.test.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendTrue() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.talend.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertTrue(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }
}
