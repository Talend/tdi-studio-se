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
package org.talend.designer.rowgenerator.ui.editor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.designer.rowgenerator.data.Function;

/**
 * created by ycbai on 2016年10月28日 Detailled comment
 *
 */
public class MetadataColumnExtTest {

    @Test
    public void testSetFunction() {
        MetadataColumnExt column = new MetadataColumnExt();
        Function func = new Function();
        func.setName("testFunc"); //$NON-NLS-1$
        column.setFunction(func);
        assertNotNull(column.getAdditionalField().get(MetadataColumnExt.FUNCTION_INFO));
    }

}
