// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.components;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;

/**
 * created by ycbai on 2014-6-10 Detailled comment
 * 
 */

public class ExpressionTest {

    @Test
    public void testIsShowFunction() {
        List<IElementParameter> parameters = spy(new ArrayList<IElementParameter>());

        /*
         * PARA2 -> PARA1, PARA3 -> PARA1, PARA1 -> true
         */
        ElementParameter param1 = createParameter("PARA1", true, null);
        ElementParameter param2 = createParameter("PARA2", false, "isShow[PARA1]");
        ElementParameter param3 = createParameter("PARA3", false, "isShow[PARA1]");
        updateParameters(parameters, param1, param2, param3);
        assertTrue(Expression.evaluate("isShow[PARA1]", parameters, param3));
        assertFalse(Expression.evaluate("isShow[PARA3]", parameters, param3)); // Self loop

        /*
         * PARA3 -> PARA2, PARA2 -> PARA3, PARA1 -> true
         */
        param2 = createParameter("PARA2", false, "isShow[PARA3]");
        param3 = createParameter("PARA3", false, "isShow[PARA2]");
        updateParameters(parameters, param1, param2, param3);
        assertFalse(Expression.evaluate("isShow[PARA2]", parameters, param3));

        /*
         * PARA3 -> PARA2, PARA2 -> PARA1, PARA1 -> PARA3
         */
        param1 = createParameter("PARA1", false, "isShow[PARA3]");
        param2 = createParameter("PARA2", false, "isShow[PARA1]");
        param3 = createParameter("PARA3", false, "isShow[PARA2]");
        updateParameters(parameters, param1, param2, param3);
        assertFalse(Expression.evaluate("isShow[PARA2]", parameters, param3));
    }

    @Test
    public void testIsShowFunctionBooleanItem() {
        List<IElementParameter> parameters = spy(new ArrayList<IElementParameter>());

        ElementParameter param1 = createParameter("PARA1.VALUE1", true, null);
        ElementParameter param2 = createParameter("PARA1.VALUE2", false, null);
        ElementParameter param3 = createParameter("PARA2", false, "(isShow[PARA1.VALUE1]) AND (isShow[PARA1.VALUE2])");

        /*
         * TODO The commented test should work and should be fixed.
         */
        // ElementParameter param4 = createParameter("PARA2", false, "isShow[PARA1.VALUE1] AND isShow[PARA1.VALUE2]");

        updateParameters(parameters, param1, param2, param3/* , param4 */);
        assertFalse(Expression.evaluate("(isShow[PARA1.VALUE1]) AND (isShow[PARA1.VALUE2])", parameters, param3));
        // assertFalse(Expression.evaluate("isShow[PARA1.VALUE1] AND isShow[PARA1.VALUE2]", parameters, param4));
    }

    @Test
    public void testIsShowFunctionClosedListItem() {
        List<IElementParameter> parameters = spy(new ArrayList<IElementParameter>());

        ElementParameter param1 = createParameter("PARA1.VALUE1", true, null);
        ElementParameter param2 = createParameter("PARA1.VALUE2", false, null);
        ElementParameter param3 = createParameter("PARA2", false, "isShow[PARA1.VALUE1]");
        ElementParameter param4 = createParameter("PARA3", false, "isShow[PARA1.VALUE2]");
        updateParameters(parameters, param1, param2, param3, param4);
        assertTrue(Expression.evaluate("isShow[PARA1.VALUE1]", parameters, param3));
        assertFalse(Expression.evaluate("isShow[PARA1.VALUE2]", parameters, param4));
    }

    private ElementParameter createParameter(String paraName, boolean isShow, String showIf) {
        ElementParameter param = mock(ElementParameter.class);
        IElement testElement = mock(IElement.class);
        when(param.getName()).thenReturn(paraName);
        if (isShow) {
            when(param.isShow(anyList())).thenReturn(isShow);
            when(param.isShow(anyString(), anyString(), anyList())).thenReturn(isShow);
        } else {
            when(param.getShowIf()).thenReturn(showIf);
        }
        when(param.getElement()).thenReturn(testElement);
        when(param.getElement().getElementName()).thenReturn("testComponent");

        return param;
    }

    private void updateParameters(List<IElementParameter> parameters, IElementParameter... params) {
        doReturn(params.length).when(parameters).size();
        for (int i = 0; i < params.length; i++) {
            doReturn(params[i]).when(parameters).get(i);
        }
    }

}
