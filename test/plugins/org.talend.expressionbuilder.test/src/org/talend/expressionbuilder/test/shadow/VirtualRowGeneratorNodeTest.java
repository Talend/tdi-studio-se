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
package org.talend.expressionbuilder.test.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.designer.rowgenerator.data.ParameterFactory;
import org.talend.expressionbuilder.model.Category;
import org.talend.expressionbuilder.model.CategoryManager;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class VirtualRowGeneratorNodeTest {

    private static Map<String, Set<Function>> categoryFunctionHM;

    private static List<Variable> variableList;

    private static String expression;

    @Before
    public void prepare() {
        // prepare function to found
        CategoryManager manager = new CategoryManager();
        List<Category> inputCategory = manager.getInputCategory("java");
        categoryFunctionHM = new HashMap<String, Set<Function>>();
        for (Category category : inputCategory) {
            String key = category.getName();
            if (categoryFunctionHM.get(key) == null) {
                categoryFunctionHM.put(key, new HashSet<Function>());
            }
            categoryFunctionHM.get(key).addAll(category.getFunctions());
        }

    }

    @Test
    public void testExpressionSplicing() {
        Set<Function> functions = categoryFunctionHM.get("TalendDate");
        Assert.assertTrue(functions.size() != 0);
        String[] params = new String[] { ParameterFactory.PARAMETER_TYPE_DATE, ParameterFactory.PARAMETER_TYPE_INT,
                ParameterFactory.PARAMETER_TYPE_STRING };
        Function function = getFunctionByName(functions, "addDate", params);

        // prepare variables and expression to test
        Variable var = new Variable("row1.mydate", "08-14-2019", "id_Date", true);
        variableList = new ArrayList<Variable>();
        variableList.add(var);
        expression = "TalendDate.addDate(row1.mydate,07,\"MM\")";

        // case1: when input "08-14-2019"
        TestVirtualRowGeneratorNode node = new TestVirtualRowGeneratorNode(function);
        String expressionFromNode = getExpressionFromNode(node);
        Assert.assertNotNull(expressionFromNode);
        // the return of org.talend.expressionbuilder.test.shadow.VirtualRowGeneratorNode.getArray()
        String expectExpression = "\"\"+(TalendDate.addDate( ParserUtils.parseTo_Date(08-14-2019, \"dd-MM-yyyy\"),07,\"MM\"))+\"\"";
        Assert.assertEquals(expectExpression, expressionFromNode);

        // case2: when input "null", null in the composite but "null" received
        Variable var2 = new Variable("row1.mydate", "null", "id_Date", true);
        variableList = new ArrayList<Variable>();
        variableList.add(var2);
        TestVirtualRowGeneratorNode node2 = new TestVirtualRowGeneratorNode(function);
        String expressionFromNode2 = getExpressionFromNode(node2);
        Assert.assertNotNull(expressionFromNode2);
        String expectExpression2 = "\"\"+(TalendDate.addDate(null,07,\"MM\"))+\"\"";
        Assert.assertEquals(expectExpression2, expressionFromNode2);

        // case3: when input empty
        Variable var3 = new Variable("row1.mydate", "", "id_Date", true);
        variableList = new ArrayList<Variable>();
        variableList.add(var3);
        TestVirtualRowGeneratorNode node3 = new TestVirtualRowGeneratorNode(function);
        String expressionFromNode3 = getExpressionFromNode(node3);
        Assert.assertNotNull(expressionFromNode3);
        String expectExpression3 = "\"\"+(TalendDate.addDate( ParserUtils.parseTo_Date(\"\", \"dd-MM-yyyy\"),07,\"MM\"))+\"\"";
        Assert.assertEquals(expectExpression3, expressionFromNode3);

    }

    private String getExpressionFromNode(TestVirtualRowGeneratorNode node) {
        String expressionValue = null;
        IElementParameter elementParameter = node.getElementParameter("VALUES");
        Object value = elementParameter.getValue();
        if (value instanceof List) {
            List valueList = (List) value;
            for (Object object : valueList) {
                if (object instanceof Map) {
                    Map elem = (Map) object;
                    Object val = elem.get("ARRAY");
                    if (val != null) {
                        expressionValue = val.toString();
                    }
                }
            }
        }
        return expressionValue;
    }

    private Function getFunctionByName(Set<Function> functionSet, String functionName, String[] paramType) {
        Function found = null;
        List<String> paramTypsList = Arrays.asList(paramType);
        for (Function function : functionSet) {
            if (!function.getName().equals(functionName)) {
                continue;
            }
            List parameters = function.getParameters();
            if (parameters.size() != paramType.length) {
                continue;
            }
            boolean notMatch = false;
            for (Object obj : parameters) {
                Parameter param = (Parameter) obj;
                if (!paramTypsList.contains(param.getType())) {
                    notMatch = true;
                }
            }
            if (!notMatch) {
                found = function;
                break;
            }
        }
        return found;
    }

    class TestVirtualRowGeneratorNode extends VirtualRowGeneratorNode {


        public TestVirtualRowGeneratorNode(Function function) {
            super(function);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.expressionbuilder.test.shadow.VirtualRowGeneratorNode#getVariableList()
         */
        @Override
        protected List<Variable> getVariableList() {
            return variableList;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.expressionbuilder.test.shadow.VirtualRowGeneratorNode#getExpression()
         */
        @Override
        protected String getExpression() {
            return expression;
        }

    }
}
