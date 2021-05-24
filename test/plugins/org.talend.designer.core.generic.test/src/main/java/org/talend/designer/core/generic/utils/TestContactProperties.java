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
package org.talend.designer.core.generic.utils;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * created by ycbai on 2016年6月6日 Detailled comment
 *
 */
public class TestContactProperties extends PropertiesImpl {

    public Property<String> mobile = newProperty("mobile").setRequired(); //$NON-NLS-1$

    public Property<String> email = newProperty("email"); //$NON-NLS-1$

    public TestContactProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(mobile);
        form.addRow(email);
    }

}
