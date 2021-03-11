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
package org.talend.designer.core.ui.editor.process;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.ElementParameterDefaultValue;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;


/**
 * created by nrousseau on Aug 29, 2016
 * Detailled comment
 *
 */
public class ProcessTest {

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.process.Process#checkProcess()}.
     */
    @Test
    public void testCheckProcess() {

        Process p = new Process(new FakePropertyImpl()) {

            /* (non-Javadoc)
             * @see org.talend.designer.core.ui.editor.process.Process#checkProblems()
             */
            @Override
            protected void checkProblems() {
                assertThat(isDuplicate(), is(false));
                assertThat(isActivate(), is(true));
            }

        };
        p.setActivate(false);
        p.setDuplicate(false);
        p.checkProcess();
        p.setActivate(false);
        p.setDuplicate(true);
        p.checkProcess();
        p.setActivate(true);
        p.setDuplicate(false);
        p.checkProcess();
        p.setActivate(true);
        p.setDuplicate(true);
        p.checkProcess();
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.process.Process#checkProcess()}.
     */
    @Test
    public void testNoNeedSetValue() {
        Process p = new Process(new FakePropertyImpl());

        List<IElementParameterDefaultValue> defaultValues = null;

        IElement elem = new FakeElement("test");
        IElementParameter param = new ElementParameter(elem);

        IElementParameterDefaultValue defaultValue = new ElementParameterDefaultValue();
        defaultValues = new ArrayList<IElementParameterDefaultValue>();
        defaultValues.add(defaultValue);
        param.setDefaultValues(defaultValues);
        assertFalse(p.noNeedSetValue(param, "aa"));

        assertTrue(p.noNeedSetValue(param, null));

        defaultValue = new ElementParameterDefaultValue();
        defaultValue.setDefaultValue("aa");
        defaultValues = new ArrayList<IElementParameterDefaultValue>();
        defaultValues.add(defaultValue);
        param.setDefaultValues(defaultValues);
        assertTrue(p.noNeedSetValue(param, "aa"));

        defaultValue = new ElementParameterDefaultValue();
        defaultValues = new ArrayList<IElementParameterDefaultValue>();
        defaultValue.setDefaultValue("bb");
        defaultValues.add(defaultValue);
        param.setDefaultValues(defaultValues);
        assertFalse(p.noNeedSetValue(param, "aa"));
    }

}
