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
package core.model.components;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * Test of the ElementParameter class with the Expression.
 *
 * $Id: ElementParameterTest.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class ElementParameterTest {

    private static boolean testParam(String expression, ElementParameter paramToTest, List<IElementParameter> listParams) {
        boolean returnValue;
        paramToTest.setShowIf(expression);
        System.out.println("Test OK: " + expression); //$NON-NLS-1$
        returnValue = paramToTest.isShow(listParams);
        return returnValue;
    }

    /**
     * DOC nrousseau Comment method "main".
     *
     * @param args
     */
    public static void main(String[] args) {
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(null);
        param.setName("VAR1"); //$NON-NLS-1$
        param.setValue("value1"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE1", "VALUE2", "VALUE3", "VALUE4", "VALUE5", "VALUE5_6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        param.setListItemsValue(new String[] { "value1", "value2", "value3", "value4", "value5", "value5;value6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR2"); //$NON-NLS-1$
        param.setValue("value2"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE1", "VALUE2", "VALUE3", "VALUE4", "VALUE5", "VALUE5_6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        param.setListItemsValue(new String[] { "value1", "value2", "value3", "value4", "value5", "value5;value6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR3"); //$NON-NLS-1$
        param.setValue("value3"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE1", "VALUE2", "VALUE3", "VALUE4", "VALUE5", "VALUE5_6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        param.setListItemsValue(new String[] { "value1", "value2", "value3", "value4", "value5", "value5;value6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR4"); //$NON-NLS-1$
        param.setValue("value4"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE1", "VALUE2", "VALUE3", "VALUE4", "VALUE5", "VALUE5_6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        param.setListItemsValue(new String[] { "value1", "value2", "value3", "value4", "value5", "value5;value6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR5"); //$NON-NLS-1$
        param.setValue("value5;value6"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE1", "VALUE2", "VALUE3", "VALUE4", "VALUE5", "VALUE5_6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        param.setListItemsValue(new String[] { "value1", "value2", "value3", "value4", "value5", "value5;value6" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR6"); //$NON-NLS-1$
        param.setValue(new Boolean(true));
        listParams.add(param);

        param = new ElementParameter(null);
        param.setName("VAR7"); //$NON-NLS-1$
        param.setValue("value1"); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "POSTGRESQL", "ORACLE" }); //$NON-NLS-1$ //$NON-NLS-2$
        param.setListItemsValue(new String[] { "value1", "value2" }); //$NON-NLS-1$ //$NON-NLS-2$
        listParams.add(param);

        ElementParameter paramToTest = new ElementParameter(null);
        paramToTest.setName("VAR_TEST"); //$NON-NLS-1$

        Assert.assertTrue(testParam(
                "((VAR1 == 'VALUE1' and VAR2 == 'VALUE2') or (VAR3 != 'VALUE3')) or (VAR4 == 'VALUE4')", paramToTest, //$NON-NLS-1$
                listParams));
        Assert.assertTrue(testParam(
                "((VAR1 == 'VALUE1' AND VAR2 == 'VALUE2') OR (VAR3 != 'VALUE3')) OR (VAR4 == 'VALUE4')", paramToTest, //$NON-NLS-1$
                listParams));
        Assert.assertTrue(testParam(
                "(VAR4 == 'VALUE4') or ((VAR1 == 'VALUE1' and VAR2 == 'VALUE2') or VAR3 != 'VALUE3')", paramToTest, //$NON-NLS-1$
                listParams));
        Assert.assertTrue(testParam(
                "((VAR1 == 'VALUE1' and VAR2 == 'VALUE2') and (VAR3 != 'VALUE3' or VAR4 == 'VALUE4'))", paramToTest, //$NON-NLS-1$
                listParams));
        Assert.assertFalse(testParam("VAR4 != 'VALUE4' and (VAR2 == 'VALUE2' or VAR1 == 'VALUE1')", paramToTest, //$NON-NLS-1$
                listParams));

        Assert.assertTrue(testParam("(VAR5 == 'VALUE5_6') and (VAR6 == 'true')", paramToTest, listParams)); //$NON-NLS-1$

        Assert.assertTrue(testParam("VAR6 == 'true'", paramToTest, listParams)); //$NON-NLS-1$

        Assert.assertTrue(testParam("(VAR7 == 'POSTGRESQL') or (VAR7 == 'ORACLE')", paramToTest, listParams)); //$NON-NLS-1$
    }

}
