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
import org.talend.daikon.properties.property.Property;

/**
 *
 * created by ycbai on 2017年1月19日 Detailled comment
 *
 */
public class TestReferencedProperties extends PropertiesImpl {

    private static final long serialVersionUID = -7243591015517808899L;

    public static final String TEST_DEFINTION_NAME = "TestReferencedPropertiesDefintionName"; //$NON-NLS-1$

    public Property<String> aProp = newString("aProp"); //$NON-NLS-1$

    public TestReferencedProperties(String name) {
        super(name);
    }

}
