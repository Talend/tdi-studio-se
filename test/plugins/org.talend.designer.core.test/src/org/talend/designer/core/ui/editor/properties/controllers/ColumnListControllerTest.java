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
package org.talend.designer.core.ui.editor.properties.controllers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * created by ycbai on 2014-1-10 Detailled comment
 *
 */

public class ColumnListControllerTest {

    private static ColumnListController instance;

    protected Node node;

    private IMetadataTable table;

    private IMetadataColumn column;

    public static final String NAME = "NAME"; //$NON-NLS-1$

    public static final String PARAMETERS = "PARAMETERS"; //$NON-NLS-1$

    public static final String PARAMETER_CLASS_NAME = "PARAMETER_CLASS_NAME"; //$NON-NLS-1$

    public static final String PARAMETER_NAME = "PARAMETER_NAME"; //$NON-NLS-1$

    public static final String PARAMETER_VALUE = "PARAMETER_VALUE"; //$NON-NLS-1$

    public static final String FUNCTION_CHECK_METHOD = "reUsedColumnFunctionArrayCheck"; //$NON-NLS-1$

    @Before
    public void before() {
        instance = mock(ColumnListController.class);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tRowGenerator", "DI");
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        Process process = new Process(property);
        node = new Node(component, process);
        table = node.getMetadataList().get(0);
        column = new MetadataColumn();
    }

    @Test
    public void testFilterColumns() throws Exception {
        String TEST_METHOD = "filterColumns"; //$NON-NLS-1$
        String[] TEST_COL_ARRAY = new String[] { "col1", "col2", "col3", "col4" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Object[] argVals = null;
        List<String[]> resCols = null;
        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, false, false }, "CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false }, "CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false }, "CUSTOM_COLUMNS:*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col4" }); //$NON-NLS-1$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false },
                "CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, false, false },
                "CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "NONE_CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, true }, "NONE_CUSTOM_COLUMNS:*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col4" }); //$NON-NLS-1$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, true, false },
                "NONE_CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertTrue(resCols.get(0).length == 0);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, true, false }, "REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));
    }

    private Object[] getFilterColumnsArgVals(String[] columnNames, Boolean[] isCustom, String filter) {
        Object[] args = new Object[4];

        IElementParameter param = mock(IElementParameter.class);
        when(param.getFilter()).thenReturn(filter);
        Map<String, Boolean> customColMap = new HashMap<String, Boolean>();
        String[] columnNameArray = new String[columnNames.length];
        String[] columnValueArray = new String[columnNameArray.length];
        for (int i = 0; i < columnNames.length; i++) {
            columnNameArray[i] = columnNames[i];
            columnValueArray[i] = columnNameArray[i];
            customColMap.put(columnNameArray[i], isCustom[i]);
        }
        args[0] = param;
        args[1] = columnNameArray;
        args[2] = columnValueArray;
        args[3] = customColMap;

        return args;
    }

    @Test
    public void testreUsedColumnFunctionArrayCheck1() throws Exception {
        column.setLabel("A"); //$NON-NLS-1$
        column.setTalendType(JavaTypesManager.STRING.getId());

        JSONObject functionInfoObj = new JSONObject();
        JSONArray parametersArr = new JSONArray();
        try {
            functionInfoObj.put(PARAMETER_CLASS_NAME, "StringHandling");
            functionInfoObj.put(NAME, "CHANGE");
            JSONObject parameterObj1 = new JSONObject();
            parameterObj1.put(PARAMETER_NAME, "oldStr");
            parameterObj1.put(PARAMETER_VALUE, "\"hello world!\" ");
            parametersArr.put(parameterObj1);
            JSONObject parameterObj2 = new JSONObject();
            parameterObj2.put(PARAMETER_NAME, "regex");
            parameterObj2.put(PARAMETER_VALUE, "\"world\" ");
            parametersArr.put(parameterObj2);
            JSONObject parameterObj3 = new JSONObject();
            parameterObj3.put(PARAMETER_NAME, "replacement");
            parameterObj3.put(PARAMETER_VALUE, "\"guy\" ");
            parametersArr.put(parameterObj3);
            functionInfoObj.put(PARAMETERS, parametersArr);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        column.getAdditionalField().put("FUNCTION_INFO", functionInfoObj.toString());
        table.getListColumns().add(column);
        String functioExpression = "StringHandling.CHANGE(\"hello world!\",\"world\",\"guy\")";
        testNewLineArrayFunction(functioExpression, column.getLabel());
    }

    @Test
    public void testreUsedColumnFunctionArrayCheck2() throws Exception {

        column.setLabel("B"); //$NON-NLS-1$
        column.setTalendType(JavaTypesManager.STRING.getId());

        JSONObject functionInfoObj = new JSONObject();
        JSONArray parametersArr = new JSONArray();
        try {
            functionInfoObj.put(PARAMETER_CLASS_NAME, "");
            functionInfoObj.put(NAME, "...");
            JSONObject parameterObj = new JSONObject();
            parameterObj.put(PARAMETER_NAME, "customize parameter");
            parameterObj.put(PARAMETER_VALUE, "");
            parametersArr.put(parameterObj);
            functionInfoObj.put(PARAMETERS, parametersArr);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        column.getAdditionalField().put("FUNCTION_INFO", functionInfoObj.toString());
        table.getListColumns().add(column);
        String functioExpression = "";
        testNewLineArrayFunction(functioExpression, column.getLabel());
    }

    private void testNewLineArrayFunction(String functioExpression, String label) throws Exception {
        Map<String, Object> newLine = new HashMap<>();
        newLine.put("SCHEMA_COLUMN", label);
        newLine.put("ARRAY", "");
        String[] codes = { "SCHEMA_COLUMN", "ARRAY" };
        Whitebox.invokeMethod(instance, FUNCTION_CHECK_METHOD, new Object[] { newLine, node, codes });
        assertEquals(functioExpression, newLine.get("ARRAY"));
    }

}
