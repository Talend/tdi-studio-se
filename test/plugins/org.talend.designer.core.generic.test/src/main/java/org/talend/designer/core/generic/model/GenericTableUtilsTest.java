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
package org.talend.designer.core.generic.model;

import static org.junit.Assert.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * created by nrousseau on Apr 28, 2016 Detailled comment
 *
 */
public class GenericTableUtilsTest {

    @Test
    public void testSetTableValues() {
        IElement elem = new FakeElement("test");
        IElementParameter tableParam = new ElementParameter(elem);
        tableParam.setName("myTable");
        IElementParameter column1 = new ElementParameter(elem);
        column1.setName("column1");
        column1.setFieldType(EParameterFieldType.TEXT);
        IElementParameter column2 = new ElementParameter(elem);
        column2.setName("column2");
        column2.setFieldType(EParameterFieldType.CHECK);
        IElementParameter column3 = new ElementParameter(elem);
        column3.setName("column3");
        column3.setFieldType(EParameterFieldType.CLOSED_LIST);
        List<String> values = new ArrayList<>();
        values.add("Value1");
        values.add("Value2");
        values.add("Value3");
        column3.setListItemsValue(values.toArray());
        List<String> codeNames = new ArrayList<>();
        codeNames.add("column1");
        codeNames.add("column2");
        codeNames.add("column3");
        tableParam.setListItemsDisplayCodeName(codeNames.toArray(new String[0]));

        List<IElementParameter> childParams = new ArrayList<>();
        childParams.add(column1);
        childParams.add(column2);
        childParams.add(column3);
        tableParam.setListItemsValue(childParams.toArray());


        List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();
        Map<String, Object> line = new HashMap<String, Object>();
        line.put("column1", "Text1");
        line.put("column2", Boolean.TRUE);
        line.put("column3", "Value1");
        table.add(line);
        line = new HashMap<String, Object>();
        line.put("column1", "Text2");
        line.put("column2", Boolean.FALSE);
        line.put("column3", 2);
        table.add(line);

        MyTestTable tableProperties = new MyTestTable("tableProp");
        GenericTableUtils.setTableValues(tableProperties, table, tableParam);
        // call 2 times, to ensure the number of line is not added at each calls
        GenericTableUtils.setTableValues(tableProperties, table, tableParam);

        assertEquals(2, ((List) tableProperties.column1.getValue()).size());
        assertEquals(2, ((List) tableProperties.column2.getValue()).size());
        assertEquals(2, ((List) tableProperties.column3.getValue()).size());

        assertEquals("Text1", ((List) tableProperties.column1.getValue()).get(0));
        assertEquals("Text2", ((List) tableProperties.column1.getValue()).get(1));

        assertEquals("true", ((List) tableProperties.column2.getValue()).get(0));
        assertEquals("false", ((List) tableProperties.column2.getValue()).get(1));

        assertEquals("Value1", ((List) tableProperties.column3.getValue()).get(0));
        assertEquals("Value3", ((List) tableProperties.column3.getValue()).get(1));
    }

    @Test
    public void testGetTableValues() {
        MyTestTable tableProperties = new MyTestTable("tableProp");
        List<String> columnValues = new ArrayList<String>();
        columnValues.add("Text1");
        columnValues.add("Text2");
        tableProperties.column1.setValue(columnValues);
        columnValues = new ArrayList<String>();
        columnValues.add("true");
        columnValues.add("false");
        tableProperties.column2.setValue(columnValues);
        columnValues = new ArrayList<String>();
        columnValues.add("Value1");
        columnValues.add("Value3");
        tableProperties.column3.setValue(columnValues);

        IElement elem = new FakeElement("test");
        IElementParameter tableParam = new ElementParameter(elem);
        tableParam.setName("myTable");
        IElementParameter column1 = new ElementParameter(elem);
        column1.setName("column1");
        column1.setFieldType(EParameterFieldType.TEXT);
        IElementParameter column2 = new ElementParameter(elem);
        column2.setName("column2");
        column2.setFieldType(EParameterFieldType.CHECK);
        IElementParameter column3 = new ElementParameter(elem);
        column3.setName("column3");
        column3.setFieldType(EParameterFieldType.CLOSED_LIST);
        List<String> values = new ArrayList<>();
        values.add("Value1");
        values.add("Value2");
        values.add("Value3");
        column3.setListItemsValue(values.toArray());
        List<String> codeNames = new ArrayList<>();
        codeNames.add("column1");
        codeNames.add("column2");
        codeNames.add("column3");
        tableParam.setListItemsDisplayCodeName(codeNames.toArray(new String[0]));

        List<IElementParameter> childParams = new ArrayList<>();
        childParams.add(column1);
        childParams.add(column2);
        childParams.add(column3);
        tableParam.setListItemsValue(childParams.toArray());

        List<Map<String, Object>> table = GenericTableUtils.getTableValues(tableProperties, tableParam);

        assertEquals(2, table.size());

        Map<String, Object> line1 = table.get(0);
        Map<String, Object> line2 = table.get(1);

        assertEquals("Text1", line1.get("column1"));
        assertEquals(Boolean.TRUE, line1.get("column2"));
        assertEquals("Value1", line1.get("column3"));

        assertEquals("Text2", line2.get("column1"));
        assertEquals(Boolean.FALSE, line2.get("column2"));
        assertEquals("Value3", line2.get("column3"));
    }

    class MyTestTable extends ComponentPropertiesImpl {

        public Property column1 = newString("column1");

        public Property column2 = newString("column2");

        public Property column3 = newString("column3");

        /**
         * DOC nrousseau MyTestTable constructor comment.
         *
         * @param name
         */
        public MyTestTable(String name) {
            super(name);
        }

    }

    @Test
    public void testGetDriverJarsPath() {
        List<String> listString = new ArrayList<String>();
        listString.add("mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0");
        listString.add("mvn:org.talend.libraries/mysql-connector-java-5.1.40-bin/6.0.0");
        String jars = GenericTableUtils.getDriverJarPaths(listString);
        assertEquals(jars,
                "mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0;mvn:org.talend.libraries/mysql-connector-java-5.1.40-bin/6.0.0");

        listString = new ArrayList<String>();
        listString.add("mysql-connector-java-5.1.30-bin.jar");
        listString.add("mysql-connector-java-5.1.40-bin.jar");
        jars = GenericTableUtils.getDriverJarPaths(listString);
        assertEquals(jars, "mysql-connector-java-5.1.30-bin.jar;mysql-connector-java-5.1.40-bin.jar");

        listString = new ArrayList<String>();
        listString.add("context.jdbc1_drivers");
        jars = GenericTableUtils.getDriverJarPaths(listString);
        assertEquals(jars, "context.jdbc1_drivers");
    }

}
