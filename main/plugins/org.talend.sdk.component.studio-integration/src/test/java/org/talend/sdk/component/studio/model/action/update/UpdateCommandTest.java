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
package org.talend.sdk.component.studio.model.action.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;
import org.talend.sdk.component.studio.model.parameter.CheckElementParameter;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.TableElementParameter;
import org.talend.sdk.component.studio.model.parameter.TextElementParameter;

public class UpdateCommandTest {

    @Test
    public void testOnResultFireCalled() {
        final ButtonParameterMock buttonMock = new ButtonParameterMock(null);
        final UpdateCommand command = new UpdateCommand(null, "conf", Collections.emptyMap(), buttonMock);
        command.onResult(new HashMap<>());
        Assertions.assertTrue(buttonMock.firePropertyChangeCalled());
    }

    @Test
    public void testOnResultListOption() {
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

        final ButtonParameter button = new ButtonParameter(null);

        final List<IElementParameter> columns = new ArrayList<>();
        final TaCoKitElementParameter column1 = new TaCoKitElementParameter(null);
        column1.setFieldType(EParameterFieldType.CHECK);
        column1.setName("conf.updatableConfig.table[].check");
        columns.add(column1);
        final TaCoKitElementParameter column2 = new TaCoKitElementParameter(null);
        column2.setFieldType(EParameterFieldType.TEXT);
        column2.setName("conf.updatableConfig.table[].number");
        columns.add(column2);
        final TaCoKitElementParameter column3 = new TaCoKitElementParameter(null);
        column3.setFieldType(EParameterFieldType.CLOSED_LIST);
        column3.setName("conf.updatableConfig.table[].operator");
        columns.add(column3);
        final TaCoKitElementParameter column4 = new TaCoKitElementParameter(null);
        column4.setFieldType(EParameterFieldType.TEXT);
        column4.setName("conf.updatableConfig.table[].strColumn");
        columns.add(column4);

        final TableElementParameter table = new TableElementParameter(null, columns);
        table.setName("conf.updatableConfig.table");
        table.setFieldType(EParameterFieldType.TABLE);

        final UpdateCommand command = new UpdateCommand(null, "conf.updatableConfig", Collections.singletonMap(table.getName(), table), button);

        final Map<String, Object> actionResult = new HashMap<>();
        final List<Map<String, Object>> tableValue = new ArrayList<>();
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
        actionResult.put("table", tableValue);

        command.onResult(actionResult);

        Assertions.assertEquals(expectedValue, table.getValue());
    }

    @Test
    public void testOnResultIntegerOption() {
        final String expectedValue = "42";

        final ButtonParameter button = new ButtonParameter(null);
        final TextElementParameter text = new TextElementParameter(null);
        text.setName("conf.updatableConfig.number");
        text.setFieldType(EParameterFieldType.TEXT);

        final UpdateCommand command = new UpdateCommand(null, "conf.updatableConfig",
                Collections.singletonMap(text.getName(), text), button);

        final Map<String, Object> actionResult = new HashMap<>();
        actionResult.put("number", 42);

        command.onResult(actionResult);

        Assertions.assertEquals(expectedValue, text.getValue());
    }

    @Test
    public void testOnResultStringOption() {
        final String expectedValue = "value";

        final ButtonParameter button = new ButtonParameter(null);
        final TextElementParameter text = new TextElementParameter(null);
        text.setName("conf.updatableConfig.str");
        text.setFieldType(EParameterFieldType.TEXT);

        final UpdateCommand command = new UpdateCommand(null, "conf.updatableConfig",
                Collections.singletonMap(text.getName(), text), button);

        final Map<String, Object> actionResult = new HashMap<>();
        actionResult.put("str", "value");

        command.onResult(actionResult);

        Assertions.assertEquals(expectedValue, text.getValue());
    }

    @Test
    public void testOnResultBooleanOption() {
        final boolean expectedValue = false;

        final ButtonParameter button = new ButtonParameter(null);
        final CheckElementParameter check = new CheckElementParameter(null);
        check.setName("conf.updatableConfig.check");
        check.setFieldType(EParameterFieldType.TEXT);

        final UpdateCommand command = new UpdateCommand(null, "conf.updatableConfig",
                Collections.singletonMap(check.getName(), check), button);

        final Map<String, Object> actionResult = new HashMap<>();
        actionResult.put("check", false);

        command.onResult(actionResult);

        Assertions.assertEquals(expectedValue, check.getValue());
    }

    @Test
    @Disabled
    public void testOnResultSchemaOption() {
        final List<String> expectedValue = Arrays.asList("col1", "col2", "col3");

        final ButtonParameter button = new ButtonParameter(null);
        final TaCoKitElementParameter schema = new TaCoKitElementParameter(null);
        schema.setName("conf.updatableConfig.schema");
        schema.setFieldType(EParameterFieldType.SCHEMA_TYPE);

        final UpdateCommand command = new UpdateCommand(null, "conf.updatableConfig",
                Collections.singletonMap(schema.getName(), schema), button);

        final Map<String, Object> actionResult = new HashMap<>();
        final List<String> schemaValue = Arrays.asList("col1", "col2", "col3");
        actionResult.put("schema", schemaValue);

        command.onResult(actionResult);

        Assertions.assertEquals(expectedValue, schema.getValue());
    }

    private static class ButtonParameterMock extends ButtonParameter {

        private boolean fireCalled = false;

        public ButtonParameterMock(final IElement element) {
            super(element);
        }

        @Override
        public void firePropertyChange(final String name, final Object oldValue, final Object newValue) {
            this.fireCalled = true;
        }

        boolean firePropertyChangeCalled() {
            return fireCalled;
        }

    }
}
