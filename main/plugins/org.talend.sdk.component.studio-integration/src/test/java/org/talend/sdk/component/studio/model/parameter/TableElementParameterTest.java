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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.IActionParameter;

import java.util.*;

public class TableElementParameterTest {

    @Test
    public void testCreateActionParameter() {
        final List<Pair<String, String>> expected = new ArrayList<>();
        final Pair<String, String> p1 = new Pair("t[0].id", "id value 0");
        expected.add(p1);
        final Pair<String, String> p2 = new Pair("t[0].name", "name 0");
        expected.add(p2);
        final Pair<String, String> p3 = new Pair("t[0].number", "number 0");
        expected.add(p3);
        final Pair<String, String> p4 = new Pair("t[1].id", "id value 1");
        expected.add(p4);
        final Pair<String, String> p5 = new Pair("t[1].name", "name 1");
        expected.add(p5);
        final Pair<String, String> p6 = new Pair("t[1].number", "number 1");
        expected.add(p6);

        final List<Map<String, String>> value = new ArrayList<>();
        final Map<String, String> row1 = new LinkedHashMap<>();
        row1.put("conf.table[].id", "id value 0");
        row1.put("conf.table[].name", "name 0");
        row1.put("conf.table[].number", "number 0");
        value.add(row1);
        final Map<String, String> row2 = new LinkedHashMap<>();
        row2.put("conf.table[].id", "id value 1");
        row2.put("conf.table[].name", "name 1");
        row2.put("conf.table[].number", "number 1");
        value.add(row2);

        final TableElementParameter parameter = new TableElementParameter(null, Collections.emptyList());
        parameter.setName("conf.table");
        parameter.setValue(value);

        final IActionParameter actionParameter = parameter.createActionParameter("t");
        final Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void testCreateActionParameterNull() {
        final List<Pair<String, String>> expected = new ArrayList<>();

        final TableElementParameter parameter = new TableElementParameter(null, Collections.emptyList());
        parameter.setName("conf.table");

        final IActionParameter actionParameter = parameter.createActionParameter("t");
        final Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }
    @Test
    public void testCreateActionParameterEmpty() {
        final List<Pair<String, String>> expected = new ArrayList<>();

        final TableElementParameter parameter = new TableElementParameter(null, Collections.emptyList());
        parameter.setName("conf.table");
        parameter.setValue(new ArrayList<>());

        final IActionParameter actionParameter = parameter.createActionParameter("t");
        final Collection<Pair<String, String>> parameters = actionParameter.parameters();
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void testSetValueFixClosedList() {
        final List<Map<String, Object>> expectedValue = new ArrayList<>();
        final Map<String, Object> expectedRow1 = new LinkedHashMap<>();
        expectedRow1.put("conf.table[].check", "false");
        expectedRow1.put("conf.table[].enum", "GREATER");
        expectedValue.add(expectedRow1);

        final CheckElementParameter col1 = new CheckElementParameter(null);
        col1.setName("conf.table[].check");
        col1.setFieldType(EParameterFieldType.CHECK);

        final TaCoKitElementParameter col2 = new TaCoKitElementParameter(null);
        final Object[] col2PossibleValues = new String[]{"GREATER", "LESS", "EQUALS"};
        col2.setName("conf.table[].enum");
        col2.setListItemsValue(col2PossibleValues);
        col2.setFieldType(EParameterFieldType.CLOSED_LIST);

        final TableElementParameter table = new TableElementParameter(null, Collections.emptyList());
        final Object[] tableColumns = new Object[]{col1, col2};
        table.setListItemsValue(tableColumns);
        table.setName("conf.table");

        final List<Map<String, Object>> newValue = new ArrayList<>();
        final Map<String, Object> newRow1 = new LinkedHashMap<>();
        newRow1.put("conf.table[].check", "false");
        newRow1.put("conf.table[].enum", 0);
        newValue.add(newRow1);

        table.setValue(newValue);

        Assertions.assertEquals(expectedValue, table.getValue());
    }

    @Test
    public void testSetValueFromAction() {
        final List<Map<String, Object>> expectedValue = new ArrayList<Map<String, Object>>();
        final Map<String, Object> row1 = new HashMap<>();
        row1.put("conf.updatableConfig.table[].check", true);
        row1.put("conf.updatableConfig.table[].number", "1");
        row1.put("conf.updatableConfig.table[].operator", "GREATER");
        row1.put("conf.updatableConfig.table[].strColumn", "Talend");
        expectedValue.add(row1);
        final Map<String, Object> row2 = new HashMap<>();
        row2.put("conf.updatableConfig.table[].check", false);
        row2.put("conf.updatableConfig.table[].number", "2");
        row2.put("conf.updatableConfig.table[].operator", "LESS");
        row2.put("conf.updatableConfig.table[].strColumn", "The best");
        expectedValue.add(row2);

        final TableElementParameter table = new TableElementParameter(null, Collections.emptyList());
        table.setName("conf.updatableConfig.table");
        table.setFieldType(EParameterFieldType.TABLE);
        final TaCoKitElementParameter column1 = new TaCoKitElementParameter(null);
        column1.setFieldType(EParameterFieldType.CHECK);
        column1.setName("conf.updatableConfig.table[].check");
        final TaCoKitElementParameter column2 = new TaCoKitElementParameter(null);
        column2.setFieldType(EParameterFieldType.TEXT);
        column2.setName("conf.updatableConfig.table[].number");
        final TaCoKitElementParameter column3 = new TaCoKitElementParameter(null);
        column3.setFieldType(EParameterFieldType.CLOSED_LIST);
        column3.setName("conf.updatableConfig.table[].operator");
        final TaCoKitElementParameter column4 = new TaCoKitElementParameter(null);
        column4.setFieldType(EParameterFieldType.TEXT);
        column4.setName("conf.updatableConfig.table[].strColumn");
        table.setListItemsValue(new Object[] {column1, column2, column3, column4});

        final List<Object> tableValue = new ArrayList<>();
        final Map<String, Object> tableRow1 = new HashMap<>();
        tableRow1.put("check", true);
        tableRow1.put("number", 1);
        tableRow1.put("operator", "GREATER");
        tableRow1.put("strColumn", "Talend");
        tableValue.add(tableRow1);
        final Map<String, Object> tableRow2 = new HashMap<>();
        tableRow2.put("check", false);
        tableRow2.put("number", 2);
        tableRow2.put("operator", "LESS");
        tableRow2.put("strColumn", "The best");
        tableValue.add(tableRow2);

        table.setValueFromAction(tableValue);
        Assertions.assertEquals(expectedValue, table.getValue());
    }

}
