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
package org.talend.sdk.component.studio.model.parameter;

import org.eclipse.jface.action.IAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.IActionParameter;

import java.util.*;

public class TableElementParameterTest {

    @Test
    public void testCreateActionParameter() {
        List<Pair<String, String>> expected = new ArrayList<>();
        Pair<String, String> p1 = new Pair("t[0].id", "id value 0");
        expected.add(p1);
        Pair<String, String> p2 = new Pair("t[0].name", "name 0");
        expected.add(p2);
        Pair<String, String> p3 = new Pair("t[0].number", "number 0");
        expected.add(p3);
        Pair<String, String> p4 = new Pair("t[1].id", "id value 1");
        expected.add(p4);
        Pair<String, String> p5 = new Pair("t[1].name", "name 1");
        expected.add(p5);
        Pair<String, String> p6 = new Pair("t[1].number", "number 1");
        expected.add(p6);

        List<Map<String, String>> value = new ArrayList<>();
        Map<String, String> row1 = new LinkedHashMap<>();
        row1.put("conf.table[].id", "id value 0");
        row1.put("conf.table[].name", "name 0");
        row1.put("conf.table[].number", "number 0");
        value.add(row1);
        Map<String, String> row2 = new LinkedHashMap<>();
        row2.put("conf.table[].id", "id value 1");
        row2.put("conf.table[].name", "name 1");
        row2.put("conf.table[].number", "number 1");
        value.add(row2);

        TableElementParameter parameter = new TableElementParameter(null);
        parameter.setName("conf.table");
        parameter.setValue(value);

        IActionParameter actionParameter = parameter.createActionParameter("t");
        Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void testCreateActionParameterNull() {
        List<Pair<String, String>> expected = new ArrayList<>();

        TableElementParameter parameter = new TableElementParameter(null);
        parameter.setName("conf.table");

        IActionParameter actionParameter = parameter.createActionParameter("t");
        Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }
    @Test
    public void testCreateActionParameterEmpty() {
        List<Pair<String, String>> expected = new ArrayList<>();

        TableElementParameter parameter = new TableElementParameter(null);
        parameter.setName("conf.table");
        parameter.setValue(new ArrayList<>());

        IActionParameter actionParameter = parameter.createActionParameter("t");
        Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }
}
