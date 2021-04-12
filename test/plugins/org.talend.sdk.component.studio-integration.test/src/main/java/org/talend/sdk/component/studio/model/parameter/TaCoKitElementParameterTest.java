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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TaCoKitElementParameterTest {

    @Test
    public void testSerilization() {
        TaCoKitElementParameter elemParam = new TaCoKitElementParameter() {

            @Override
            public boolean isPersisted() {
                return false;
            };
        };
        elemParam.setSerialized(true);
        assertTrue(elemParam.isSerialized());
        elemParam.setSerialized(false);
        assertTrue(elemParam.isSerialized());

        elemParam = new TaCoKitElementParameter() {

            @Override
            public boolean isPersisted() {
                return true;
            };
        };
        elemParam.setSerialized(true);
        assertTrue(elemParam.isSerialized());
        elemParam.setSerialized(false);
        assertFalse(elemParam.isSerialized());
    }

}
