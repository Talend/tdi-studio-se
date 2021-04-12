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
package org.talend.designer.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class UpdateParameterUtilsTest {

    @SuppressWarnings("nls")
    @Test
    public void deepCopyTest() {
        ElementParameter from = new ElementParameter(null);
        String P1 = "P1";
        from.setValue(P1); // $NON-NLS-1$

        Map<String, IElementParameter> fromChildren = from.getChildParameters();

        ElementParameter child = new ElementParameter(null);
        String CK1 = "CK1";
        String CV1 = "CV1";
        child.setValue(CV1);
        fromChildren.put(CK1, child);

        child = new ElementParameter(null);
        String CK2 = "CK2";
        String CV2 = "CV2";
        child.setValue(CV2);
        fromChildren.put(CK2, child);

        child = new ElementParameter(null);
        String CK3 = "CK3";
        String CV3 = "CV3";
        child.setValue(CV3);
        fromChildren.put(CK3, child);

        Map<String, IElementParameter> fromChidlren2 = child.getChildParameters();
        child = new ElementParameter(null);
        String CCK1 = "CCK1";
        String CCV1 = "CCV1";
        child.setValue(CCV1);
        fromChidlren2.put(CCK1, child);

        child = new ElementParameter(null);
        String CCK2 = "CCK2";
        String CCV2 = "CCV2";
        child.setValue(CCV2);
        fromChidlren2.put(CCK2, child);

        child = new ElementParameter(null);
        String CCK3 = "CCK3";
        String CCV3 = "CCV3";
        child.setValue(CCV3);
        fromChidlren2.put(CCK3, child);

        ElementParameter to = new ElementParameter(null);

        UpdateParameterUtils.deepCopy(from, to);

        assertResult(from, to);
    }

    private void assertResult(IElementParameter left, IElementParameter right) {

        if (left == null && right == null) {
            assertTrue(true);
            return;
        } else if (left == null || right == null) {
            assertTrue(false);
            return;
        }

        assertEquals(left.getValue(), right.getValue());

        Map<String, IElementParameter> leftChildren = left.getChildParameters();
        Map<String, IElementParameter> rightChildren = right.getChildParameters();

        assertEquals(leftChildren.size(), rightChildren.size());

        for (Map.Entry<String, IElementParameter> leftEntry : leftChildren.entrySet()) {
            String key = leftEntry.getKey();
            IElementParameter leftParameter = leftEntry.getValue();
            IElementParameter rightParameter = rightChildren.get(key);
            assertResult(leftParameter, rightParameter);
        }
    }
}
