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
package org.talend.designer.webservice.ui;

import org.apache.commons.beanutils.BeanUtils;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WenServiceReflect {

    public void reflectFromClassName() {
        try {
            Object parm2 = Class.forName("com.ren.test.Parm2").newInstance();
            Class classType = parm2.getClass();

            String Field1value = BeanUtils.getProperty(parm2, "field1"); // 得到一般属性

            System.out.println("Field1value :" + Field1value);

            String[] Field3Value = BeanUtils.getArrayProperty(parm2, "field3"); // 得到数组

            System.out.println("Field3Value[1] :" + Field3Value[1]);

            BeanUtils.setProperty(parm2, "innerfield.field1", "efff");

            String field2_field1 = BeanUtils.getProperty(parm2, "innerfield.field1");

            System.out.println("field2_field1 :" + field2_field1);

            String field2_field2 = BeanUtils.getProperty(parm2, "innerfield.field2");

            System.out.println("field2_field2 :" + field2_field2);

            String[] Field4Value = BeanUtils.getArrayProperty(parm2, "field4");

            System.out.println("Field4Value[1] :" + Field4Value);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
