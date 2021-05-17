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
package org.talend.sdk.component.studio.model.parameter;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public class InputSchemaParameterTest {

    @Test
    public void testIsPersisted() {
        InputSchemaParameter isp = new InputSchemaParameter(null, "", "", Collections.emptyList());
        assertTrue(isp.isPersisted());
    }
}
