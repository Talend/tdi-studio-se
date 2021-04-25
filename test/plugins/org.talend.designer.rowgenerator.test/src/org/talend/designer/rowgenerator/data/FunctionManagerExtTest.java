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
package org.talend.designer.rowgenerator.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.i18n.Messages;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FunctionManagerExtTest {

    public static final String PURE_PERL_NAME = "..."; //$NON-NLS-1$

    public static final String PURE_PERL_DESC = Messages.getString("FunctionManager.PurePerl.Desc"); //$NON-NLS-1$

    public static final String PURE_PERL_PARAM = Messages.getString("FunctionManager.PurePerl.ParaName"); //$NON-NLS-1$

    private String value = "((String)globalMap.get(\"sTest\")).replaceAll(\"a\",\"e\").replaceAll(\"i\",\"o\")";

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * DOC Administrator Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * DOC Administrator Comment method "testGetAvailableFunFromValue". test private method getAvailableFunFromValue(,,)
     *
     * @throws Exception
     */
    @Test
    public void testGetAvailableFunFromValue() throws Exception {
        MetadataColumnExt baseConn = mock(MetadataColumnExt.class);
        when(baseConn.getTalendType()).thenReturn("id_String");

        AbstractFunctionParser parser = null;
        parser = new RoutineFunctionParser();
        parser.parse();
        List<TalendType> talendTypes = parser.getList();
        List<Function> funtions = new ArrayList<Function>();

        for (TalendType talendType : talendTypes) {
            if (talendType.getName().equals(baseConn.getTalendType())) {
                funtions.addAll(talendType.getFunctions());
            }
        }
        funtions.add(createCustomizeFunction());

        Method method = FunctionManagerExt.class.getDeclaredMethod("getAvailableFunFromValue", new Class[] {
                MetadataColumnExt.class, String.class, List.class });
        method.setAccessible(true);
        Function funtion = (Function) method.invoke(new FunctionManagerExt(), baseConn, value, funtions);
        Parameter parameter = null;
        for (Object stringParameter : funtion.getParameters()) {
            parameter = (Parameter) stringParameter;
        }
        assertEquals(value, parameter.getValue());
    }

    private Function createCustomizeFunction() {
        Function function = new Function();
        function.setName(PURE_PERL_NAME);
        function.setDescription(PURE_PERL_DESC);
        StringParameter param = new StringParameter();
        List<Parameter> params = new ArrayList<Parameter>();
        param.setName(PURE_PERL_PARAM);
        params.add(param);
        function.setParameters(params);
        return function;
    }

    @Test
    public void testGetFuntionFromColumn() {
        FunctionManagerExt functionManagerExt = new FunctionManagerExt();
        MetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel("newColumn"); //$NON-NLS-1$
        newColumn.setTalendType(JavaTypesManager.STRING.getId());
        MetadataColumnExt columnExt = new MetadataColumnExt(newColumn);
        List<Function> funcs = functionManagerExt.getFunctionsByType(newColumn.getTalendType());
        if (funcs.size() > 0) {
            Function func = funcs.get(0);
            columnExt.setFunction(func);
            Function function = functionManagerExt.getFunctionFromColumn(columnExt);
            assertEquals(func.getName(), function.getName());
            assertEquals(func.getParameters().size(), function.getParameters().size());
        }
    }

    @Test
    public void testGetOneColDataNull1() {
        FunctionManagerExt functionManagerExt = new FunctionManagerExt();
        assertNull(functionManagerExt.getOneColData(null));
        assertNull(functionManagerExt.getOneColData(null, true));
        assertNull(functionManagerExt.getOneColData(null, true, true));
    }

    @Test
    public void testGetOneColDataNull2() {
        // test function/function name as null or ""
        FunctionManagerExt functionManagerExt = new FunctionManagerExt();
        MetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel("newColumn"); //$NON-NLS-1$
        newColumn.setTalendType(JavaTypesManager.STRING.getId());
        MetadataColumnExt columnExt = new MetadataColumnExt(newColumn);
        assertNull(functionManagerExt.getOneColData(columnExt));

        Function function = new Function();
        columnExt.setFunction(function);
        String value = functionManagerExt.getOneColData(columnExt);
        assertEquals(value, "");//$NON-NLS-1$
    }

    @Test
    public void testGetOneColDataDefaultFunction() {
        FunctionManagerExt functionManagerExt = new FunctionManagerExt();
        MetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel("newColumn"); //$NON-NLS-1$
        newColumn.setTalendType(JavaTypesManager.STRING.getId());
        MetadataColumnExt columnExt = new MetadataColumnExt(newColumn);

        // Function name as "PURE_PERL_NAME"
        Function function = new Function();
        function.setName(PURE_PERL_NAME);
        function.setDescription(PURE_PERL_DESC);

        StringParameter param = new StringParameter();
        param.setName(PURE_PERL_PARAM);
        param.setValue("value1");//$NON-NLS-1$
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(param);
        function.setParameters(params);
        columnExt.setFunction(function);

        String value = functionManagerExt.getOneColData(columnExt);
        assertEquals(value, param.getValue());
    }

    @Test
    public void testGetOneColData() {
        FunctionManagerExt functionManagerExt = new FunctionManagerExt();
        MetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel("newColumn"); //$NON-NLS-1$
        newColumn.setTalendType(JavaTypesManager.STRING.getId());
        MetadataColumnExt columnExt = new MetadataColumnExt(newColumn);

        Function function = new Function();
        function.setName("UPCASE");//$NON-NLS-1$
        function.setDescription(PURE_PERL_DESC);
        function.setClassName("StringHandling");//$NON-NLS-1$

        StringParameter param = new StringParameter();
        param.setName("string");//$NON-NLS-1$
        param.setValue("\"Hello\"");//$NON-NLS-1$
        param.setType(JavaTypesManager.STRING.getId());
        param.setComment("String");//$NON-NLS-1$
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(param);
        function.setParameters(params);
        columnExt.setFunction(function);

        // replace as false
        String value = functionManagerExt.getOneColData(columnExt);
        assertEquals(value, "StringHandling.UPCASE(\"Hello\")");//$NON-NLS-1$

        value = functionManagerExt.getOneColData(columnExt, false);
        assertEquals(value, "StringHandling.UPCASE(\"Hello\")");//$NON-NLS-1$

        value = functionManagerExt.getOneColData(columnExt, false, false);
        assertEquals(value, "StringHandling.UPCASE(\"Hello\")");//$NON-NLS-1$

        // replace as true
        value = functionManagerExt.getOneColData(columnExt, true);
        assertEquals(value, "StringHandling.UPCASE(${0})");//$NON-NLS-1$

        value = functionManagerExt.getOneColData(columnExt, false, true);
        assertEquals(value, "StringHandling.UPCASE(${0})");//$NON-NLS-1$
    }
}
